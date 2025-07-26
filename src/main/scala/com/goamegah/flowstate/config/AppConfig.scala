package com.goamegah.flowstate.config

import com.typesafe.config.{Config, ConfigFactory, ConfigException}

import scala.util.{Try, Success, Failure}

object AppConfig {
    private val config: Config = {
        println("Loading configuration...")
        val localConfig = Try(ConfigFactory.parseResources("local.conf"))
        localConfig match {
            case Success(conf) =>
                println("local.conf loaded successfully")
                conf.withFallback(ConfigFactory.load())
            case Failure(ex) =>
                println(s"Warning: Could not load local.conf: ${ex.getMessage}")
                ConfigFactory.load()
        }
    }

    private val where = "container"

    // Utility function to get a string with a fallback
    // This will print a warning if the configuration is not found
    // and return a default value
    private def getStringWithFallback(path: String, fallback: String): String = {
        Try(config.getString(path)).getOrElse {
            println(s"Configuration not found for $path, using fallback: $fallback")
            fallback
        }
    }

    object API {
        val endpoint: String = config.getString("api.API_ENDPOINT")
        val baseUrl: String = config.getString("api.API_URL")
    }

    object AWS {
        val accessKey: String = config.getString("aws.iam.IAM_ACCESS_KEY_ID")
        val secretKey: String = config.getString("aws.iam.IAM_SECRET_ACCESS_KEY")
        val region: String = config.getString("aws.REGION")
        val rawBucket: String = config.getString("aws.s3.S3_RAW_BUCKET_URI")
    }

    object Postgres {
        val host: String = getStringWithFallback(s"$where.postgres.DWH_POSTGRES_HOST", "localhost")
        val port: Int = Try(config.getInt(s"$where.postgres.DWH_POSTGRES_PORT")).getOrElse(5432)
        val db: String = getStringWithFallback(s"$where.postgres.DWH_POSTGRES_DB", "dwh_postgres_db")
        val user: String = getStringWithFallback(s"$where.postgres.DWH_POSTGRES_USR", "dwh_postgres_user")
        val password: String = getStringWithFallback(s"$where.postgres.DWH_POSTGRES_PWD", "dwh_postgres_password")
        val ddlDir: String = getStringWithFallback(s"$where.postgres.DWH_POSTGRES_DDL_DIR", "../database/ddl")

        def jdbcUrl: String = s"jdbc:postgresql://$host:$port/$db"
    }

    object Airflow {
        val dataDir: String = getStringWithFallback(s"$where.airflow.AIRFLOW_DATA_DIR", "/opt/airflow/data")
        val rawDir: String = getStringWithFallback(s"$where.airflow.AIRFLOW_RAW_DIR", "/opt/airflow/data/raw")
        val transientDir: String = getStringWithFallback(s"$where.airflow.AIRFLOW_TRANSIENT_DIR", "/opt/airflow/data/transient")
    }

    object Spark {
        val checkpointDir: String = getStringWithFallback(s"$where.spark.SPARK_CHECKPOINT_DIR", "output/checkpoint")
        val rawDir: String = getStringWithFallback(s"$where.spark.SPARK_RAW_DIR", "output/data/raw")
        val sinkDir: String = getStringWithFallback(s"$where.spark.SPARK_SINK_DIR", "output/data/sink")
        val dataDir: String = getStringWithFallback(s"$where.spark.SPARK_DATA_DIR", "output/data")
        val appName: String = getStringWithFallback(s"$where.spark.SPARK_APP_NAME", "RennesMetropoleTrafficData")
        val master: String = getStringWithFallback(s"$where.spark.SPARK_MASTER", "local[*]")
        val slidingWindowDuration: String = getStringWithFallback(s"$where.spark.SPARK_SLIDING_WINDOW_DURATION", "5 minutes")
        val slidingWindowSlide: String = getStringWithFallback(s"$where.spark.SPARK_SLIDING_WINDOW_SLIDE", "1 minute")
        val triggerInterval: String = getStringWithFallback(s"$where.spark.SPARK_TRIGGER_INTERVAL", "60 seconds")
        val enableSlidingWindow: Boolean = Try(config.getBoolean(s"$where.spark.SPARK_ENABLE_SLIDING_WINDOW")).getOrElse(false)
        val enableHourlyAggregation: Boolean = Try(config.getBoolean(s"$where.spark.SPARK_ENABLE_HOURLY_AGGREGATION")).getOrElse(true)
        val enableMinuteAggregation: Boolean = Try(config.getBoolean(s"$where.spark.SPARK_ENABLE_MINUTE_AGGREGATION")).getOrElse(true)
        val watermark: String = getStringWithFallback(s"$where.spark.SPARK_WATERMARK", "1 minutes")
    }
}