package com.goamegah.flowstate.transform

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame}

object TrafficStatsAggregator {

    /**
     * Agrégation glissante avec fenêtre de 30 minutes (actualisée toutes les 10 min),
     * utilisée pour l’évolution dynamique (métriques en temps réel).
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
     * Agrégation par période (minute ou heure) et par segment routier + statut.
     * Permet d'afficher l’évolution du trafic sur chaque route.
     */
    def aggregateByPeriodAndRoadName(df: DataFrame, resolution: String): DataFrame = {
        val windowDuration = resolution match {
            case "minute" => "1 minute"
            case "hour"   => "1 hour"
            case _        => throw new IllegalArgumentException("Résolution inconnue")
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
     * Agrégation simple : vitesse moyenne par statut (bouché, fluide, etc.)
     * Pour afficher les vitesses moyennes par catégorie de trafic.
     */
    def avgSpeedByStatus(df: DataFrame): DataFrame = {
        df.groupBy("trafficstatus")
            .agg(
                avg("averagevehiclespeed").alias("avg_speed"),
                count("*").alias("count")
            )
    }
}
