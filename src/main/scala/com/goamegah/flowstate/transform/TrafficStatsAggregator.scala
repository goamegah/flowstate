package com.goamegah.flowstate.transform

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame}

object TrafficStatsAggregator {

    /**
     * Sliding aggregation with a 30-minute window (updated every 10 minutes),
     * used for dynamic evolution (real-time metrics).
     */
    def aggregateBySlidingWindow(df: DataFrame): DataFrame = {
        df
            .withWatermark("timestamp", "1 minutes")
            .groupBy(
                window(col("timestamp"), "30 minutes", "10 minutes").getField("start").alias("period"),
                col("trafficstatus")
            )
            .agg(
                avg("averagevehiclespeed").alias("avg_speed"),
                count("*").alias("count")
            )
    }

    /**
     * Aggregation by period (minute or hour) and by road segment + status.
     * Allows displaying the evolution of traffic on each road.
     */
    def aggregateByPeriodAndRoadName(df: DataFrame, resolution: String): DataFrame = {
        val windowDuration = resolution match {
            case "minute" => "1 minute"
            case "hour"   => "1 hour"
            case _        => throw new IllegalArgumentException("Unknown resolution")
        }

        val latencySeconds = unix_timestamp(current_timestamp()) - unix_timestamp(col("timestamp"))

        df
            .withWatermark("timestamp", "2 minutes")
            .groupBy(
                window(col("timestamp"), windowDuration).getField("start").alias("period"),
                col("denomination")
            )
            .agg(
                avg("averagevehiclespeed").alias("avg_speed"),
                min("averagevehiclespeed").alias("min_speed"),
                max("averagevehiclespeed").alias("max_speed"),
                stddev("averagevehiclespeed").alias("stddev_speed"),

                avg("traveltime").alias("avg_travel_time"),
                avg("traveltimereliability").alias("avg_reliability"),

                count("*").alias("count"),
                avg(latencySeconds).alias("avg_latency_sec")
            )
    }

    /**
     * Simple aggregation: average speed by status (congested, fluid, etc.)
     * To display average speeds by traffic category.
     */
    def avgSpeedByStatus(df: DataFrame): DataFrame = {
        df.groupBy("trafficstatus")
            .agg(
                avg("averagevehiclespeed").alias("avg_speed"),
                count("*").alias("count")
            )
    }
}
