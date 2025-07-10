package com.goamegah.flowtrack.streaming

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object TrafficTransformer {

  def transform(inputDF: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    println("[#] Étape 1 - Données brutes (streaming)")
    inputDF.printSchema()

    // Étape 2 - Explosion du champ `results`
    val explodedDF = inputDF
      .filter(col("results").isNotNull && size(col("results")) > 0)
      .withColumn("result", explode(col("results")))

    println("[#] Étape 2 - Après explosion de 'results'")
    explodedDF.printSchema()

    // Étape 3 - Sélection des colonnes utiles
    val selectedDF = explodedDF.selectExpr("result.*")

    println("[#] Étape 3 - Après sélection des champs")
    selectedDF.printSchema()

    // Étape 4 - Transformation finale
    val transformedDF = selectedDF
      .withColumn("lat", col("geo_point_2d.lat"))
      .withColumn("lon", col("geo_point_2d.lon"))
      .withColumn("geometry_linestring", to_json(col("geo_shape.geometry")).cast("string"))
      .drop("geo_point_2d")
      .drop("geo_shape")
      .withColumn("datetime", to_timestamp(col("datetime")))
      .withColumnRenamed("predefinedlocationreference", "location_id")
      .withColumnRenamed("averagevehiclespeed", "average_speed")
      .withColumnRenamed("traveltime", "travel_time")
      .withColumnRenamed("traveltimereliability", "travel_time_reliability")
      .withColumnRenamed("trafficstatus", "traffic_status")
      .withColumnRenamed("vehicleprobemeasurement", "vehicle_probe")
      .withColumnRenamed("denomination", "road_name")
      .withColumnRenamed("vitesse_maxi", "max_speed")
      .withColumnRenamed("hierarchie", "road_category")
      .filter(col("datetime").isNotNull)


    println("[#] Étape 4 - Après nettoyage, cast et renommage")
    transformedDF.printSchema()

    transformedDF
  }
}
