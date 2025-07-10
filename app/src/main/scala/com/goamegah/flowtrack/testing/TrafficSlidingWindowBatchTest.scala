package com.goamegah.flowtrack.testing

import com.goamegah.flowtrack.db.DBSchemaManager.getClass
import com.goamegah.flowtrack.streaming.TrafficTransformer
import com.goamegah.flowtrack.transform.TrafficStatsAggregator
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object TrafficSlidingWindowBatchTest {
    private val logger = LoggerFactory.getLogger(getClass)
    def main(args: Array[String]): Unit = {
        implicit val spark: SparkSession = SparkSession.builder()
            .appName("TrafficSlidingWindowBatchTest")
            .master("local[*]")
            .getOrCreate()

        import spark.implicits._

        // Charger un fichier JSON en mode batch
        val inputPath = "services/orchestrator/data/raw/20250409215402119045.json"
        val rawDF = spark.read
            .option("multiLine", true)
            .json(inputPath)

        logger.info("[INFO] Fichier JSON chargé en batch.")
        rawDF.printSchema()
        rawDF.show(2, truncate = false)

        // Appliquer la transformation
        val transformedDF = TrafficTransformer.transform(rawDF)(spark)
        logger.debug("[DEBUG] Résultat final après transformation:")
        transformedDF.printSchema()
        transformedDF.show(10, truncate = false)

        // Appeler l'agrégation par sliding window (l'implicit spark est désormais disponible)
        val slidingDF = TrafficStatsAggregator.aggregateBySlidingWindow(transformedDF)
        logger.debug("[DEBUG] Résultat de l'agrégation par sliding window:")
        slidingDF.printSchema()
        slidingDF.show(10, truncate = false)

        spark.stop()
    }
}
