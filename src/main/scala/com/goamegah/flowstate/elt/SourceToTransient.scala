package com.goamegah.flowstate.elt

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}
import java.io.{File, PrintWriter}
import scala.io.Source
import com.goamegah.flowstate.config.AppConfig

object SourceToTransient {
    private val logger = LoggerFactory.getLogger(this.getClass)

    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder()
            .appName("SourceToTransient")
            .master("local[*]")
            .getOrCreate()

        import spark.implicits._

        val apiUrl = AppConfig.API.endpoint
        val transientDir = s"${AppConfig.Airflow.dataDir}/transient"

        try {
            logger.info(s"Fetching traffic data from API: $apiUrl")

            val response = Source.fromURL(apiUrl, "UTF-8").mkString
            logger.info(s"Received response of length: ${response.length}")

            val tmpFile = File.createTempFile("traffic", ".json")
            val writer = new PrintWriter(tmpFile)
            writer.write(response)
            writer.close()

            val fullDf = spark.read.json(tmpFile.getAbsolutePath)

            val resultsDf = fullDf.select(explode(col("results")).as("record"))

            val count = resultsDf.count()
            logger.info(s"Number of records extracted: $count")

            if (count > 0) {
                val timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                val outputPath = s"$transientDir/traffic_$timestamp.json"

                resultsDf
                    .select("record.*")
                    .write
                    .mode("overwrite")
                    .json(outputPath)

                logger.info(s"Successfully wrote transient data to: $outputPath")
            } else {
                logger.warn("No results to save.")
            }

            tmpFile.delete()

        } catch {
            case ex: Exception =>
                logger.error("Failed to fetch or save traffic data", ex)
        } finally {
            spark.stop()
        }
    }
}
