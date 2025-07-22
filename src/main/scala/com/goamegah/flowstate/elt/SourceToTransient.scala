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



////package com.goamegah.flowstate.extract
////
////import org.apache.spark.sql.SparkSession
////import org.apache.spark.sql.functions._
////import scala.io.Source
////import java.time.format.DateTimeFormatter
////import java.time.{ZoneOffset, ZonedDateTime}
////import com.goamegah.flowstate.config.AppConfig
////import java.io.{File, PrintWriter}
////
////object FetchAndStoreTrafficData {
////
////    def main(args: Array[String]): Unit = {
////        val spark = SparkSession.builder()
////          .appName("FetchAndStoreTrafficData")
////          .master("local[*]")
////          .getOrCreate()
////
////        import spark.implicits._
////        val apiUrl = AppConfig.API.endpoint
////        val airflowDataDir = AppConfig.Airflow.dataDir
////        val outputDir = s"$airflowDataDir/raw"
////
////        try {
////            println(s"[INFO] Récupération des données depuis: $apiUrl")
////
////            // Récupérer la réponse JSON complète
////            val response = Source.fromURL(apiUrl, "UTF-8").mkString
////
////            println(s"[INFO] Données récupérées, taille: ${response.length} caractères")
////
////            // Écrire dans un fichier temporaire
////            val tempJsonFile = s"/tmp/temp_traffic_${System.currentTimeMillis()}.json"
////            val writer = new PrintWriter(new File(tempJsonFile))
////            writer.write(response)
////            writer.close()
////
////            println(s"[INFO] Fichier temporaire créé: $tempJsonFile")
////
////            // Lire le fichier JSON directement
////            val fullDf = spark.read.json(tempJsonFile)
////
////            println("[INFO] DataFrame créé")
////
////            try {
////                // Essayer d'extraire les résultats directement
////                val resultsDf = fullDf.select(explode(col("results")).as("record"))
////
////                val count = resultsDf.count()
////                println(s"[INFO] Nombre de records trouvés: $count")
////
////                if (count > 0) {
////                    val timestamp = ZonedDateTime.now(ZoneOffset.UTC)
////                      .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
////                    val outputPath = s"$outputDir/traffic_$timestamp.json"
////
////                    // Créer le répertoire de sortie s'il n'existe pas
////                    new File(outputDir).mkdirs()
////
////                    // Sauvegarder les résultats
////                    resultsDf.write.mode("overwrite").json(outputPath)
////                    println(s"[INFO] Données sauvegardées : $outputPath")
////                } else {
////                    println("[INFO] Aucun résultat à enregistrer.")
////                }
////
////            } catch {
////                case ex: Exception =>
////                    println(s"[ERREUR] Problème lors de l'extraction des résultats: ${ex.getMessage}")
////
////                    // Sauvegarder les données brutes en cas d'erreur
////                    val timestamp = ZonedDateTime.now(ZoneOffset.UTC)
////                      .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
////                    val outputPath = s"$outputDir/raw_data_$timestamp.json"
////
////                    new File(outputDir).mkdirs()
////                    fullDf.write.mode("overwrite").json(outputPath)
////                    println(s"[INFO] Données brutes sauvegardées pour debug : $outputPath")
////            }
////
////            // Nettoyer le fichier temporaire
////            new File(tempJsonFile).delete()
////
////        } catch {
////            case e: Exception =>
////                System.err.println(s"[ERREUR] ${e.getMessage}")
////                e.printStackTrace()
////        } finally {
////            spark.stop()
////        }
////    }
////}
//
//
//
//package com.goamegah.flowstate.elt
//
//import com.goamegah.flowstate.config.AppConfig
//import org.apache.spark.sql.{SparkSession, DataFrame}
//import org.apache.spark.sql.functions._
//import org.slf4j.LoggerFactory
//
//import java.io.{File, PrintWriter}
//import java.nio.file.{Files, Paths}
//import java.time.ZonedDateTime
//import java.time.format.DateTimeFormatter
//import java.time.ZoneOffset
//import scala.io.Source
//import scala.util.{Failure, Success, Try}
//
//object SourceToTransient {
//
//    private val logger = LoggerFactory.getLogger(getClass)
//
//    def main(args: Array[String]): Unit = {
//        val spark = SparkSession.builder()
//            .appName("FetchAndStoreTrafficData")
//            .master("local[*]")
//            .getOrCreate()
//
//        import spark.implicits._
//
//        val apiUrl = AppConfig.API.endpoint
//        val outputDir = s"${AppConfig.Airflow.dataDir}/raw"
//
//        Try {
//            logger.info(s"Fetching traffic data from API: $apiUrl")
//
//            val jsonRaw = Source.fromURL(apiUrl, "UTF-8").mkString
//            val tempFile = Files.createTempFile("traffic_raw_", ".json").toFile
//
//            new PrintWriter(tempFile) {
//                write(jsonRaw)
//                close()
//            }
//
//            logger.info(s"Temporary file written: ${tempFile.getAbsolutePath}")
//
//            val fullDf = spark.read.option("multiLine", true).json(tempFile.getAbsolutePath)
//
//            val extractedDf = fullDf.select(explode(col("results")).as("record"))
//
//            if (!extractedDf.isEmpty) {
//                val timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
//                val outputPath = s"$outputDir/traffic_$timestamp.json"
//
//                extractedDf
//                    .select("record.*")
//                    .coalesce(1) // optionnel : éviter les sous-dossiers
//                    .write
//                    .mode("overwrite")
//                    .json(outputPath)
//
//                logger.info(s"Traffic data saved to $outputPath")
//            } else {
//                logger.warn("No records found in the API response.")
//            }
//
//            tempFile.delete()
//
//        } match {
//            case Failure(exception) =>
//                logger.error(s"Failed to fetch or process traffic data: ${exception.getMessage}", exception)
//            case Success(_) =>
//                logger.info("Traffic data processing complete.")
//        }
//
//        spark.stop()
//    }
//}
