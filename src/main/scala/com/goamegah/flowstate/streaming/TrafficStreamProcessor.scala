package com.goamegah.flowstate.streaming

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory

import com.goamegah.flowstate.config.AppConfig
import com.goamegah.flowstate.load.PostgresLoader
import com.goamegah.flowstate.common.SparkSessionProvider.spark
import com.goamegah.flowstate.transform.{TrafficTransformer, TrafficStatsAggregator, TrafficFeatsSelector}

object TrafficStreamProcessor {

    // Implicit retrieval of the Spark session configuration
    implicit val sparkSession: SparkSession = spark
    import spark.implicits._

    private val logger = LoggerFactory.getLogger(this.getClass)

    def start(): Unit = {
        logger.info("[OK] Starting streaming...")

        import java.nio.file.{Files, Paths}
        val rawPath = Paths.get(AppConfig.Spark.rawDir)
        if (!Files.exists(rawPath)) {
            Files.createDirectories(rawPath)
            logger.info(s"[OK] Created raw directory: ${rawPath.toAbsolutePath}")
        }

        println(s"[INFO] Raw data directory: ${AppConfig.Spark.rawDir}")
        // Load JSON files in streaming mode
        val rawStream: DataFrame = spark.readStream
            .schema(TrafficTransformer.schema)
            .option("maxFilesPerTrigger", 1)
            .option("multiLine", value = false)
            //.option("multiLine", value = true)
            //.option("recursiveFileLookup", value = true)
            .json(s"${AppConfig.Spark.rawDir}")

        println("[DEBUG] Displaying raw schema:")
        rawStream.printSchema()

        println("[DEBUG] Example of raw data (static for debug):")
        val staticSample = spark.read
            .schema(TrafficTransformer.schema)
            .json(s"${AppConfig.Spark.rawDir}")
            .limit(5)

        staticSample.show(truncate = false)

        val transformed = TrafficTransformer.transform(rawStream)

        val triggerInterval = AppConfig.Spark.triggerInterval
        val checkpointPath = AppConfig.Spark.checkpointDir
        val enableMinuteAggregation = AppConfig.Spark.enableMinuteAggregation
        val enableHourlyAggregation = AppConfig.Spark.enableHourlyAggregation

        // ========== 1. Mapping: data for the interactive map ==========
        val mapsQuery = transformed.writeStream
            .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
                val count = batchDF.count()
                logger.info(s" Batch $batchId - $count rows (mapping)")

                if (count > 0) {
                    try {
                        val trafficMapDF = TrafficFeatsSelector.selectMapsFeatures(batchDF)
                        PostgresLoader.load(trafficMapDF, "road_traffic_feats_map")
                        logger.info(s"[OK] Batch $batchId map loaded.")
                    } catch {
                        case e: Exception =>
                            logger.error(s"/!\\ Error Maps batch $batchId: ${e.getMessage}", e)
                    }
                } else {
                    logger.warn(s"/!\\ Batch $batchId empty (map) - nothing to insert.")
                }
            }
            .outputMode("update")
            .trigger(Trigger.ProcessingTime(triggerInterval))
            .start()

        // ========== 2. Statistical aggregations per minute ==========
        val statsQuery = transformed.writeStream
            .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
                val count = batchDF.count()
                logger.info(s"Batch $batchId - $count rows (statistics)")

                if (count > 0 && enableMinuteAggregation) {
                    try {
                        val aggMinute = TrafficStatsAggregator.aggregateByPeriodAndRoadName(batchDF, "minute")
                        PostgresLoader.load(aggMinute, "road_traffic_stats_minute")
                        logger.info(s"[OK] Batch $batchId minute stats loaded.")
                    } catch {
                        case e: Exception =>
                            logger.error(s"/!\\ Error Stats batch $batchId: ${e.getMessage}", e)
                    }
                }
                if (count > 0 && enableHourlyAggregation) {
                    try {
                        val aggHour = TrafficStatsAggregator.aggregateByPeriodAndRoadName(batchDF, "hour")
                        PostgresLoader.load(aggHour, "road_traffic_stats_hour")
                        logger.info(s"[OK] Batch $batchId hour stats loaded.")
                    } catch {
                        case e: Exception =>
                            logger.error(s"/!\\ Error Stats batch $batchId: ${e.getMessage}", e)
                    }
                }
            }
            .outputMode("update")
            .trigger(Trigger.ProcessingTime(triggerInterval))
            .option("checkpointLocation", checkpointPath)
            .start()

        // ========== 3. Await ==========
        mapsQuery.awaitTermination()
        statsQuery.awaitTermination()
    }

    def stop(): Unit = {
        logger.info("[STOP] Stopping streaming...")
        spark.stop()
    }
}
