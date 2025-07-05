package com.goamegah.flowtrack.extract

import org.apache.spark.sql.SparkSession
import scala.util.{Try, Success, Failure}
import scalaj.http.Http

object FetchAndStoreTrafficData {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("FetchTrafficData")
      .master("local[*]") 
      .getOrCreate()

    def fetchDataFromAPI(): String = {
      val apiUrl = "https://data.rennesmetropole.fr/api/explore/v2.1/catalog/datasets/etat-du-trafic-en-temps-reel/records?select=*&limit=100&lang=fr"
      val response = Try(Http(apiUrl).asString.body)
      response match {
        case Success(body) => body
        case Failure(exception) =>
          println(s"Error fetching data: ${exception.getMessage}")
          "{}"
      }
    }

    val jsonString = fetchDataFromAPI()

    import spark.implicits._
    val rdd = spark.sparkContext.parallelize(Seq(jsonString))
    val df = spark.read.option("multiline", "true").json(rdd)

    if (df.columns.contains("results")) {
      val result = df.selectExpr("explode(results) as row").select("row.*")
      val timestamp = java.time.LocalDateTime.now().toString.replaceAll("[:.]", "_")
      val outputPath = s"/opt/airflow/data/raw/$timestamp"
      result.write.mode("overwrite").json(outputPath)
      println(s"✅ Données enregistrées dans $outputPath")
    } else {
      println("⚠️ Format inattendu. Colonne 'results' manquante.")
    }

    spark.stop()
  }
}
