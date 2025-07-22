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

    implicit val sparkSession: SparkSession = spark
    import spark.implicits._

    private val logger = LoggerFactory.getLogger(this.getClass)

    def start(): Unit = {
        logger.info("[OK] ▶️ Démarrage du streaming...")

        import java.nio.file.{Files, Paths}
        val rawPath = Paths.get(AppConfig.Spark.rawDir)
        if (!Files.exists(rawPath)) {
            Files.createDirectories(rawPath)
            logger.info(s"[OK] Création du répertoire raw : ${rawPath.toAbsolutePath}")
        }

        println(s"[INFO] Répertoire de données brutes : ${AppConfig.Spark.rawDir}")
        // Chargement des JSONs en streaming
        val rawStream: DataFrame = spark.readStream
            .schema(TrafficTransformer.schema)
            .option("maxFilesPerTrigger", 1)
            .option("multiLine", value = false)
            //.option("multiLine", value = true)
            //.option("recursiveFileLookup", value = true)
            .json(s"${AppConfig.Spark.rawDir}")

        println("[DEBUG] ✅ Affichage du schéma brut :")
        rawStream.printSchema()

        println("[DEBUG] ✅ Exemple de données brutes (statique pour debug) :")
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

        // ========== 1. Cartographie : données pour la carte interactive ==========
        val mapsQuery = transformed.writeStream
            .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
                val count = batchDF.count()
                logger.info(s"🗺️  Batch $batchId - $count lignes (cartographie)")

                if (count > 0) {
                    try {
                        val trafficMapDF = TrafficFeatsSelector.selectMapsFeatures(batchDF)
                        PostgresLoader.load(trafficMapDF, "road_traffic_feats_map")
                        logger.info(s"[OK] Batch $batchId carto chargé.")
                    } catch {
                        case e: Exception =>
                            logger.error(s"/!\\ Erreur Maps batch $batchId : ${e.getMessage}", e)
                    }
                } else {
                    logger.warn(s"/!\\ Batch $batchId vide (carte) - rien à insérer.")
                }
            }
            .outputMode("update")
            .trigger(Trigger.ProcessingTime(triggerInterval))
            .start()

        // ========== 2. Agrégations statistiques par minute ==========
        val statsQuery = transformed.writeStream
            .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
                val count = batchDF.count()
                logger.info(s"📊 Batch $batchId - $count lignes (statistiques)")

                if (count > 0 && enableMinuteAggregation) {
                    try {
                        val aggMinute = TrafficStatsAggregator.aggregateByPeriodAndRoadName(batchDF, "minute")
                        PostgresLoader.load(aggMinute, "road_traffic_stats_minute")
                        logger.info(s"[OK] Batch $batchId stats minute chargé.")
                    } catch {
                        case e: Exception =>
                            logger.error(s"/!\\ Erreur Stats batch $batchId : ${e.getMessage}", e)
                    }
                }
                if (count > 0 && enableHourlyAggregation) {
                    try {
                        val aggHour = TrafficStatsAggregator.aggregateByPeriodAndRoadName(batchDF, "hour")
                        PostgresLoader.load(aggHour, "road_traffic_stats_hour")
                        logger.info(s"[OK] Batch $batchId stats heure chargé.")
                    } catch {
                        case e: Exception =>
                            logger.error(s"/!\\ Erreur Stats batch $batchId : ${e.getMessage}", e)
                    }
                }
            }
            .outputMode("update")
            .trigger(Trigger.ProcessingTime(triggerInterval))
            .option("checkpointLocation", checkpointPath)
            .start()

        // ========== 3. Attente ==========
        mapsQuery.awaitTermination()
        statsQuery.awaitTermination()
    }

    def stop(): Unit = {
        logger.info("[STOP] ⏹️  Arrêt du streaming...")
        spark.stop()
    }
}
