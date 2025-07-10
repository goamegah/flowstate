package com.goamegah.flowtrack.load

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.spark.sql.SaveMode
import com.goamegah.flowtrack.common.SparkSessionProvider.spark
import org.apache.spark.sql.SparkSession

class PostgresLoaderSpec extends AnyFlatSpec with Matchers {

  // Déclaration implicite de la SparkSession partagée
  implicit val sparkSession: SparkSession = spark
  import spark.implicits._

  "PostgresLoader" should "write a DataFrame to PostgreSQL successfully" in {
    val df = Seq(
      ("Rennes", 120),
      ("Paris", 300)
    ).toDF("city", "traffic_level")

    noException should be thrownBy {
      PostgresLoader.load(df, "traffic_test", SaveMode.Overwrite) // spark est injecté implicitement
    }
  }
}
