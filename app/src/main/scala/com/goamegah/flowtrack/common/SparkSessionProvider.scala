package com.goamegah.flowtrack.common

import org.apache.spark.sql.SparkSession

object SparkSessionProvider {

  lazy val spark: SparkSession = SparkSession.builder()
    .appName("Realtime Traffic Monitor")
    .master("local[*]") // Peut être externalisé via config plus tard
    .config("spark.sql.shuffle.partitions", "4")
    .getOrCreate()

}
