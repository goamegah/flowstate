package com.goamegah.flowtrack.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.Duration

object AppConfig {
  private val config: Config = ConfigFactory
    .parseResources("local.conf") // surcharge locale (si présente)
    .withFallback(ConfigFactory.load()) // application.conf

  object API {
    val endpoint: String = config.getString("api.API_ENDPOINT")
    val baseUrl: String = config.getString("api.API_URL")
  }

  object AWS {
    val accessKey: String = config.getString("aws.iam.IAM_ACCESS_KEY_ID")
    val secretKey: String = config.getString("aws.iam.IAM_SECRET_ACCESS_KEY")
    val region: String = config.getString("aws.REGION")
    val rawBucket: String = config.getString("aws.s3.S3_RAW_BUCKET_URI")
    //val curatedBucket: String = config.getString("aws.S3_CURATED_BUCKET_URI")
    //val checkpointBucket: String = config.getString("aws.S3_CHECKPOINT_BUCKET_URI")
  }

  object Postgres {
    val host: String = config.getString("postgres.DWH_POSTGRES_HOST")
    val port: Int = config.getInt("postgres.DWH_POSTGRES_PORT")
    val db: String = config.getString("postgres.DWH_POSTGRES_DB")
    val user: String = config.getString("postgres.DWH_POSTGRES_USR")
    val password: String = config.getString("postgres.DWH_POSTGRES_PWD")

    def jdbcUrl: String =
      s"jdbc:postgresql://$host:$port/$db"
  }

  object Local {
    val checkpointDir: String = config.getString("local.LOCAL_STORAGE_CHECKPOINT_DIR")
    val rawDir: String = config.getString("local.LOCAL_STORAGE_RAW_DIR")
    val dataDir: String = config.getString("local.LOCAL_STORAGE_DIR")
  }

  object Streaming {
    val slidingWindowDuration: String = config.getString("streaming.slidingWindow.duration")
    val slidingWindowSlide: String = config.getString("streaming.slidingWindow.slide")
    val slidingWindowDurationMillis: Long = Duration(slidingWindowDuration).toMillis
    val slidingWindowSlideMillis: Long = Duration(slidingWindowSlide).toMillis
    val checkpointDir: String = config.getString("streaming.checkpointDir")
    val triggerInterval: String = config.getString("streaming.triggerInterval")
    val enableSlidingWindow: Boolean = config.getBoolean("streaming.enableSlidingWindow")
    val enableHourlyAggregation: Boolean = config.getBoolean("streaming.enableHourlyAggregation")
    val enableMinuteAggregation: Boolean = config.getBoolean("streaming.enableMinuteAggregation")

  }
}
