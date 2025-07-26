package com.goamegah.flowstate.transform

import org.apache.spark.sql.functions._
import org.apache.spark.sql.DataFrame

object TrafficFeatsSelector {

    /**
     * Selects relevant columns for map display.
     * Coordinates are converted to JSON to ensure compatibility with PostgreSQL (JSONB).
     */
    def selectMapsFeatures(df: DataFrame): DataFrame = {
        df.select(
            col("segment_id"),
            col("denomination"),
            col("geo_point_2d.lat").alias("lat"),
            col("geo_point_2d.lon").alias("lon"),
            to_json(col("geo_shape.geometry.coordinates")).alias("coordinates"), // Explicit JSON conversion
            col("geo_shape.geometry.type").alias("shape_type"),
            col("trafficstatus"),
            col("averagevehiclespeed"),
            col("traveltime"),
            col("timestamp"),
            col("traffic_speed_category")
        )
    }
}
