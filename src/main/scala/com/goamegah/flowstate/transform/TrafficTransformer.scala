package com.goamegah.flowstate.transform

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.DataFrame

object TrafficTransformer {

    // Schéma JSON des fichiers récupérés depuis l’API
    val schema: StructType = StructType(Seq(
        StructField("averagevehiclespeed", IntegerType),
        StructField("datetime", StringType), // on convertira en timestamp ensuite
        StructField("denomination", StringType),
        StructField("geo_point_2d", StructType(Seq(
            StructField("lat", DoubleType),
            StructField("lon", DoubleType)
        ))),
        StructField("geo_shape", StructType(Seq(
            StructField("geometry", StructType(Seq(
                StructField("coordinates", ArrayType(ArrayType(DoubleType))), // 2D array car LineString contient des coordonnées [lon, lat]
                StructField("type", StringType)
            ))),
            StructField("type", StringType)
        ))),
        StructField("gml_id", StringType),
        StructField("hierarchie", StringType),
        StructField("hierarchie_dv", StringType),
        StructField("id_rva_troncon_fcd_v1_1", IntegerType),
        StructField("insee", IntegerType),
        StructField("predefinedlocationreference", StringType),
        StructField("trafficstatus", StringType),
        StructField("traveltime", IntegerType),
        StructField("traveltimereliability", IntegerType),
        StructField("vehicleprobemeasurement", IntegerType),
        StructField("vitesse_maxi", IntegerType)
    ))

    /**
     * Transformation de base : ajoute une colonne `period` par minute.
     * Cela facilite les agrégations temporelles à différents niveaux.
     */
    def transform(df: DataFrame): DataFrame = {
        df
            .withColumn("timestamp", to_timestamp(col("datetime"))) // Conversion explicite
            .withColumn("period", window(col("timestamp"), "1 minute").getField("start"))
            .withColumnRenamed("id_rva_troncon_fcd_v1_1", "segment_id")
            .withColumn("coordinates", to_json(col("geo_shape.geometry.coordinates")))
            .withColumn(
                "traffic_speed_category",
                when(col("averagevehiclespeed") < 30, "low")
                    .when(col("averagevehiclespeed") < 70, "medium")
                    .otherwise("high")
            )
    }

}
