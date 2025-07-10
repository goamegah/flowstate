package com.goamegah.flowtrack.batch

import com.goamegah.flowtrack.streaming.TrafficTransformer
import org.apache.spark.sql.SparkSession

object TrafficTransformerBatchTest {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("TrafficTransformerBatchTest")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Charger un fichier JSON en mode batch (ex: extrait d'un fichier de ton dossier raw)
    val df = spark.read
      .option("multiLine", true)
      .json("services/orchestrator/data/raw/20250409111502085522.json")

    println("[✅] Fichier JSON chargé en batch.")
    df.printSchema()
    df.show(2, truncate = false)

    // Appliquer ta logique de transformation
    val transformed = TrafficTransformer.transform(df)(spark)

    println("[🎯] Résultat final après transformation :")
    transformed.show(10, truncate = false)

    spark.stop()
  }
}
