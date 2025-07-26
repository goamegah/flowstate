package com.goamegah.flowstate.common

import org.apache.spark.sql.SparkSession

object SparkSessionProvider {

  lazy val spark: SparkSession = SparkSession.builder()
    .appName("Realtime Traffic Monitor")
    .master("local[*]") // Adapt if needed
    .config("spark.sql.shuffle.partitions", "4")
    .getOrCreate()

}
