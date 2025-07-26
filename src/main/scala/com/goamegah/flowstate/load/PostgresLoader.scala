package com.goamegah.flowstate.load

import com.goamegah.flowstate.config.AppConfig
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.slf4j.LoggerFactory

object PostgresLoader {

    private val logger = LoggerFactory.getLogger(getClass)

    /** Loads a DataFrame into a PostgreSQL table
     *
     * @param df        The DataFrame to load
     * @param tableName Target table name
     * @param mode      Save mode: "append", "overwrite", etc.
     */
    def load(df: DataFrame, tableName: String, mode: SaveMode = SaveMode.Append)(implicit spark: SparkSession): Unit = {
        // Force loading of the PostgreSQL driver
        Class.forName("org.postgresql.Driver")

        val jdbcUrl = AppConfig.Postgres.jdbcUrl
        val properties = new java.util.Properties()
        properties.setProperty("user", AppConfig.Postgres.user)
        properties.setProperty("password", AppConfig.Postgres.password)
        properties.setProperty("driver", "org.postgresql.Driver")

        if (df.isEmpty) {
            logger.warn(s"/!\\ No records to insert into '$tableName'")
            return
        }

        try {
            logger.info(s"[ OK ] Inserting into '$tableName' with mode $mode...")

            df.write
              .mode(mode)
              .jdbc(jdbcUrl, tableName, properties)

            logger.info(s"[ OK ] Successfully inserted into '$tableName' (${df.count()} rows)")

        } catch {
            case e: Exception =>
                logger.error(s"/!\\ Error inserting into '$tableName': ${e.getMessage}", e)
        }
    }

    /** Loads a DataFrame into a PostgreSQL table, overwriting existing data
     *
     * @param df        The DataFrame to load
     * @param tableName Target table name
     */
    def overwriteLoad(df: DataFrame, tableName: String)(implicit spark: SparkSession): Unit = {
        val jdbcUrl = AppConfig.Postgres.jdbcUrl
        // Force loading of the PostgreSQL driver
        Class.forName("org.postgresql.Driver")
        val conn = java.sql.DriverManager.getConnection(jdbcUrl, AppConfig.Postgres.user, AppConfig.Postgres.password)

        try {
            val stmt = conn.createStatement()
            stmt.execute(s"TRUNCATE TABLE $tableName")
            logger.info(s"[OK] Table '$tableName' successfully truncated (TRUNCATE)")
        } catch {
            case e: Exception =>
                logger.error(s"/!\\ Error truncating '$tableName': ${e.getMessage}")
        } finally {
            conn.close()
        }

        // Then append
        load(df, tableName, SaveMode.Append)
    }

}