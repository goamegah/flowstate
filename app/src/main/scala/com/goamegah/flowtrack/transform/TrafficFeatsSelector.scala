package com.goamegah.flowtrack.transform

import org.apache.spark.sql.{DataFrame, functions => F}
import org.apache.spark.sql.functions._

object TrafficFeatsSelector {

    def selectMapsFeatures(df: DataFrame): DataFrame = {
        df
            .withColumn("timestamp", to_timestamp(col("datetime")))
            .withColumn("period", date_trunc("minute", col("timestamp")))
            .select(
                col("period"),                            // utilisé pour les filtres temporels dans la carte
                col("location_id"),                       // identifiant unique du tronçon
                col("road_name"),                         // nom de la route
                col("road_category"),                     // hiérarchie (catégorie)
                col("traffic_status"),                    // statut de trafic (freeFlow, congested...)
                col("geometry_linestring")                // géométrie LineString au format GeoJSON (string)
            )
            .filter(
                col("road_name").isNotNull &&
                    col("geometry_linestring").isNotNull &&
                    col("traffic_status").isNotNull
            )
    }
}
