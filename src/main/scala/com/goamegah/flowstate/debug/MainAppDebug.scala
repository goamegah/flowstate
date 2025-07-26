package com.goamegah.flowstate.debug

import com.goamegah.flowstate.transform.TrafficTransformer
import org.apache.spark.sql.SparkSession

object MainAppDebug {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder()
            .appName("DebugTrafficRead")
            .master("local[*]")
            .getOrCreate()

        import spark.implicits._

        val rawPath = "shared/data/raw"  // adapte si besoin

        val df = spark.read
            .schema(TrafficTransformer.schema)
            .json(rawPath)

        val transformed = df
            .withColumn("timestamp", org.apache.spark.sql.functions.to_timestamp($"datetime"))
            .withColumn("period", org.apache.spark.sql.functions.window($"timestamp", "1 minute").getField("start"))

        println("=== Schema read ===")
        transformed.printSchema()

        println("=== Sample data ===")
        transformed.show(10, truncate = false)

        spark.stop()
    }
}
