package com.goamegah.flowtrack.streaming

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import com.goamegah.flowtrack.config.AppConfig
import com.goamegah.flowtrack.load.PostgresLoader
import com.goamegah.flowtrack.common.SparkSessionProvider.spark
import com.goamegah.flowtrack.transform.{TrafficFeatsSelector, TrafficStatsAggregator}
import com.goamegah.flowtrack.common.LoggerHelper

object TrafficStreamProcessor {

    implicit val sparkSession: SparkSession = spark
    import spark.implicits._

    val logger = LoggerHelper.getLogger("TrafficStreamProcessor")

    val trafficSchema: StructType = new StructType()
      .add("results", ArrayType(new StructType()
        .add("datetime", StringType)
        .add("predefinedlocationreference", StringType)
        .add("averagevehiclespeed", IntegerType)
        .add("traveltime", IntegerType)
        .add("traveltimereliability", IntegerType)
        .add("trafficstatus", StringType)
        .add("vehicleprobemeasurement", IntegerType)
        .add("geo_point_2d", new StructType()
          .add("lat", DoubleType)
          .add("lon", DoubleType)
        )
        .add("geo_shape", new StructType()
          .add("geometry", new StructType()
            .add("coordinates", ArrayType(ArrayType(DoubleType)))
            .add("type", StringType)
          )
        )
        .add("denomination", StringType)
        .add("vitesse_maxi", IntegerType)
        .add("hierarchie", StringType)
      ))

    // Helper function to reduce code duplication
    private def processBatch(batchDF: DataFrame, batchId: Long, batchType: String): Unit = {
        val count = batchDF.count()
        logger.info(s"[${batchType.toUpperCase}] Batch $batchId - $count lignes")

        if (count == 0) {
            logger.warn(s"[${batchType.toUpperCase}] Batch $batchId vide - rien à insérer.")
        } else {
            logger.info(s"[${batchType.toUpperCase}] Échantillon du batch $batchId :")
            batchDF.show(5, truncate = false)
            batchDF.printSchema()
        }
    }

    def start(): Unit = {
        logger.info("[OK] Démarrage du streaming...")

        import java.nio.file.{Files, Paths}
        val rawPath = Paths.get(AppConfig.Local.rawDir)
        if (!Files.exists(rawPath)) {
            Files.createDirectories(rawPath)
            logger.info(s"[OK] Création du répertoire raw : ${rawPath.toAbsolutePath}")
        }

        val rawStream: DataFrame = spark.readStream
          .schema(trafficSchema)
          .option("maxFilesPerTrigger", 1)
          .option("multiLine", value = true)
          .json(AppConfig.Local.rawDir)


        val transformed = TrafficTransformer.transform(rawStream)
        // Apply Watermark on transformed data (Accepts delayed data up to 1 minute after their production)
        val watermarkedDF = transformed
          .withWatermark("datetime", AppConfig.Streaming.watermarkThreshold)

        val triggerInterval = AppConfig.Streaming.triggerInterval
        val checkpointPath = AppConfig.Streaming.checkpointDir
        val enableSlidingWindow = AppConfig.Streaming.enableSlidingWindow
        val enableMinuteAggregation = AppConfig.Streaming.enableMinuteAggregation
        val enableHourlyAggregation = AppConfig.Streaming.enableHourlyAggregation

        val mapsQuery = watermarkedDF.writeStream
          .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
              processBatch(batchDF, batchId, "maps")

              if (batchDF.count() > 0) {
                  try {
                      val trafficMapDF = TrafficFeatsSelector.selectMapsFeatures(batchDF)
                      PostgresLoader.load(trafficMapDF, "road_traffic_feats_map")
                      logger.info(s"[MAPS] Batch $batchId traité avec succès")
                  } catch {
                      case e: Exception =>
                          logger.error(s"[MAPS] Erreur lors du traitement du batch $batchId : $e")
                  }
              }
          }
          .outputMode("update")
          .trigger(Trigger.ProcessingTime(triggerInterval))
          .option("checkpointLocation", s"${checkpointPath}_maps")
          .start()

        val statsQuery = watermarkedDF.writeStream
          .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
              processBatch(batchDF, batchId, "stats")

              if (batchDF.count() > 0) {
                  try {
                      val avgSpeedByMaxSpeedAndStatus = TrafficStatsAggregator.avgSpeedByMaxSpeedAndStatus(batchDF)

                      PostgresLoader.load(avgSpeedByMaxSpeedAndStatus, "avg_speed_by_max_speed_and_status")

                      // Aggrégation par minute
                      if (enableMinuteAggregation) {
                          val aggMinute = TrafficStatsAggregator.aggregateByPeriodAndRoadName(batchDF, "minute")
                          PostgresLoader.load(aggMinute, "road_traffic_stats_minute")
                      }

                      // Agrégation par heure
                      if (enableHourlyAggregation) {
                          val aggHour = TrafficStatsAggregator.aggregateByPeriodAndRoadName(batchDF, "hour")
                          PostgresLoader.load(aggHour, "road_traffic_stats_hour")
                      }

                      // Sliding window
                      if (enableSlidingWindow) {
                          val sliding = TrafficStatsAggregator.aggregateBySlidingWindow(batchDF)
                          PostgresLoader.load(sliding, "road_traffic_stats_sliding_window")
                      }

                      logger.info(s"[STATS] Batch $batchId traité avec succès")
                  } catch {
                      case e: Exception =>
                          logger.error(s"[MAPS] Erreur lors du traitement du batch $batchId : $e")
                  }
              }
          }
          .outputMode("update")
          .trigger(Trigger.ProcessingTime(triggerInterval))
          .option("checkpointLocation", s"${checkpointPath}_stats")
          .start()

        // Wait for any stream to terminate
        spark.streams.awaitAnyTermination()
    }

    def stop(): Unit = {
        logger.info("[STOP] Arrêt du streaming...")
        spark.streams.active.foreach(_.stop())
        spark.stop()
    }
}