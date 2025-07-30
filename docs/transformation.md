# Spark Structured Streaming Transformation

The transformation component of the FlowState project is responsible for processing the raw traffic data from the Rennes Metropole API and transforming it into a format suitable for analysis and visualization. This is achieved using Apache Spark Structured Streaming, which allows for real-time processing of streaming data.

## Data Schema

The raw data from the Rennes Metropole API is in JSON format with the following schema:

```scala
val schema: StructType = StructType(Seq(
    StructField("averagevehiclespeed", IntegerType),
    StructField("datetime", StringType), // will be converted to timestamp later
    StructField("denomination", StringType),
    StructField("geo_point_2d", StructType(Seq(
        StructField("lat", DoubleType),
        StructField("lon", DoubleType)
    ))),
    StructField("geo_shape", StructType(Seq(
        StructField("geometry", StructType(Seq(
            StructField("coordinates", ArrayType(ArrayType(DoubleType))), // 2D array because LineString contains [lon, lat] coordinates
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
```

This schema defines the structure of the traffic data, including:
- `averagevehiclespeed`: The average speed of vehicles on the road segment
- `datetime`: The timestamp of the data point
- `denomination`: The name of the road segment
- `geo_point_2d`: The geographical coordinates (latitude and longitude) of the road segment
- `geo_shape`: The geographical shape of the road segment, including coordinates and type
- `id_rva_troncon_fcd_v1_1`: The unique identifier of the road segment
- `trafficstatus`: The status of traffic on the road segment (e.g., freeFlow, heavy, congested)
- `traveltime`: The travel time on the road segment
- `traveltimereliability`: The reliability of the travel time estimate
- And other metadata fields

## Basic Transformation

The basic transformation process involves several steps:

1. **Timestamp Conversion**: Converting the `datetime` string to a timestamp for easier temporal analysis
2. **Period Addition**: Adding a `period` column that represents a 1-minute window, facilitating temporal aggregations
3. **Column Renaming**: Renaming `id_rva_troncon_fcd_v1_1` to `segment_id` for better readability
4. **Coordinate Conversion**: Converting the `geo_shape.geometry.coordinates` to JSON format for easier handling
5. **Traffic Speed Categorization**: Adding a `traffic_speed_category` column that categorizes the average vehicle speed into low, medium, and high

```scala
def transform(df: DataFrame): DataFrame = {
    df
        .withColumn("timestamp", to_timestamp(col("datetime"))) // Explicit conversion
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
```

## Streaming Process

The streaming process in FlowState uses Spark Structured Streaming to read data from the raw data directory, apply the transformations, and write the results to various sinks:

1. **Reading Data**: Reading JSON files from the raw data directory using the defined schema
2. **Applying Transformations**: Applying the basic transformations to the data
3. **Windowed Aggregations**: Performing windowed aggregations to calculate statistics over time
4. **Writing to Sinks**: Writing the transformed data to various sinks, including:
   - PostgreSQL database for visualization and analysis
   - Checkpoint directory for fault tolerance and exactly-once processing

## Configuration

The transformation process is configured through the `AppConfig` class, which provides various parameters for the Spark Structured Streaming job:

- `SPARK_SLIDING_WINDOW_DURATION`: The duration of the sliding window for aggregations (default: "5 minutes")
- `SPARK_SLIDING_WINDOW_SLIDE`: The slide interval for the sliding window (default: "1 minute")
- `SPARK_TRIGGER_INTERVAL`: The trigger interval for the Spark streaming job (default: "60 seconds")
- `SPARK_ENABLE_SLIDING_WINDOW`: Whether to enable sliding window aggregation (default: false)
- `SPARK_ENABLE_HOURLY_AGGREGATION`: Whether to enable hourly aggregation (default: true)
- `SPARK_ENABLE_MINUTE_AGGREGATION`: Whether to enable minute aggregation (default: true)
- `SPARK_WATERMARK`: The watermark duration for late data handling (default: "1 minutes")

These configuration parameters allow for fine-tuning the transformation process to meet specific requirements.

[Data Ingestion](ingestion.md) <- -> [Data Warehouse](datawarehouse.md)