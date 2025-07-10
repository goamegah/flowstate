package com.goamegah.flowtrack.transform

import org.apache.spark.sql.{DataFrame, SparkSession, functions => F}
import com.goamegah.flowtrack.config.AppConfig
import org.apache.spark.sql.functions._

object TrafficStatsAggregator {

    def aggregate(df: DataFrame): DataFrame = {
        df
            .filter(F.col("location_id").isNotNull)
            .groupBy("location_id")
            .agg(
                F.count("*").alias("nb_mesures"),
                F.avg("average_speed").alias("vitesse_moyenne"),
                F.avg("travel_time").alias("temps_trajet_moyen"),
                F.first("road_name", ignoreNulls = true).alias("denomination"),
                F.first("hierarchie", ignoreNulls = true).alias("hierarchie"),
                F.first("max_speed", ignoreNulls = true).alias("vitesse_maxi"),
                F.first("lat", ignoreNulls = true).alias("lat"),
                F.first("lon", ignoreNulls = true).alias("lon"),
                F.first("traffic_status", ignoreNulls = true).alias("statut_trafic")
            )
    }

    def aggregateByMinute(df: DataFrame): DataFrame = {
        df
            .withColumn("timestamp", F.to_timestamp(F.col("datetime")))
            .withColumn("minute", F.date_trunc("minute", F.col("timestamp")))
            .filter(F.col("location_id").isNotNull)
            .groupBy("minute", "location_id")
            .agg(
                F.count("*").alias("measure_count"),
                F.avg("average_speed").alias("average_speed"),
                F.avg("travel_time").alias("average_travel_time"),
                F.first("road_name", ignoreNulls = true).alias("road_name"),
                F.first("road_category", ignoreNulls = true).alias("road_category"),
                F.first("max_speed", ignoreNulls = true).alias("max_speed"),
                F.first("lat", ignoreNulls = true).alias("lat"),
                F.first("lon", ignoreNulls = true).alias("lon"),
                F.first("traffic_status", ignoreNulls = true).alias("traffic_status"),
                F.first("geometry_linestring", ignoreNulls = true).alias("geometry_linestring")
            )

    }

    /** Agrégation par période (minute, hour, day, etc.) */
    def aggregateByPeriod(df: DataFrame, period: String): DataFrame = {
        df
            .withColumn("timestamp", F.to_timestamp(F.col("datetime")))
            .withColumn("period", F.date_trunc(period, F.col("timestamp")))
            .filter(F.col("location_id").isNotNull)
            .groupBy("period", "location_id")
            .agg(
                F.count("*").alias("measure_count"),
                F.avg("average_speed").alias("average_speed"),
                F.avg("travel_time").alias("average_travel_time"),
                F.first("road_name", ignoreNulls = true).alias("road_name"),
                F.first("road_category", ignoreNulls = true).alias("road_category"),
                F.first("max_speed", ignoreNulls = true).alias("max_speed"),
                F.first("lat", ignoreNulls = true).alias("lat"),
                F.first("lon", ignoreNulls = true).alias("lon"),
                F.first("traffic_status", ignoreNulls = true).alias("traffic_status"),
                F.first("geometry_linestring", ignoreNulls = true).alias("geometry_linestring")
            )
    }

    def aggregateBySlidingWindow(df: DataFrame)(implicit spark: SparkSession): DataFrame = {
        val windowDuration = AppConfig.Streaming.slidingWindowDuration
        val slideDuration = AppConfig.Streaming.slidingWindowSlide

        df
            .withColumn("timestamp", F.to_timestamp(F.col("datetime")))
            .groupBy(
                F.window(F.col("timestamp"), windowDuration, slideDuration),
                F.col("location_id")
            )
            .agg(
                F.count("*").alias("num_troncon"),
                F.sum("vehicle_probe").alias("total_vehicle_probe"),
                F.avg("average_speed").alias("average_speed"),
                F.avg("travel_time").alias("average_travel_time"),
                F.avg("travel_time_reliability").alias("average_travel_time_reliability"),
                F.max("max_speed").alias("max_speed"),
                F.collect_list("traffic_status").alias("traffic_status_list"),
            )
            .withColumn("window_start", F.col("window.start"))
            .withColumn("window_end", F.col("window.end"))
            .drop("window")
    }

    def aggregateByPeriodAndRoadName(df: DataFrame, period: String): DataFrame = {
        df
            .withColumn("timestamp", F.to_timestamp(F.col("datetime")))
            .withColumn("period", F.date_trunc(period, F.col("timestamp")))
            .filter(F.col("location_id").isNotNull)
            .groupBy("period", "road_name")
            .agg(
                F.count("*").alias("num_troncon"),
                F.sum("vehicle_probe").alias("total_vehicle_probe"),
                F.avg("average_speed").alias("average_speed"),
                F.avg("travel_time").alias("average_travel_time"),
                F.avg("travel_time_reliability").alias("average_travel_time_reliability"),
                F.max("max_speed").alias("max_speed"),
                F.collect_list("traffic_status").alias("traffic_status_list"),
            )
    }

    def avgSpeedByStatus(df: DataFrame): DataFrame = {
        df
            .groupBy("traffic_status")
            .agg(
                F.avg("average_speed").alias("avg_speed"),
                F.count(F.lit(1)).alias("record_count")
            )
            .orderBy("traffic_status")
    }
}
