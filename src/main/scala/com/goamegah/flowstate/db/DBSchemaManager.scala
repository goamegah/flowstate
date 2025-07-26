package com.goamegah.flowstate.db

import java.nio.file.{Files, Paths}
import java.sql.{Connection, DriverManager}
import scala.io.Source
import java.io.File
import com.goamegah.flowstate.config.AppConfig
import org.slf4j.LoggerFactory

object DBSchemaManager {
    private val logger = LoggerFactory.getLogger(this.getClass)

    // Force loading of the PostgreSQL driver
    Class.forName("org.postgresql.Driver")

    private def getConnection(): Connection = {
        DriverManager.getConnection(
            AppConfig.Postgres.jdbcUrl,
            AppConfig.Postgres.user,
            AppConfig.Postgres.password
        )
    }

    /**
     * Execute a SQL file to create tables or views in PostgreSQL.
     * @param path Path to the SQL file.
     *             The file should contain valid SQL statements.
     *             If the file does not exist, a warning is logged and the method returns without executing anything.
     *             If an error occurs during execution, a warning is logged.
     */
    private def executeSqlFile(path: String)(implicit conn: Connection): Unit = {
        val file = new File(path)
        if (!file.exists()) {
            logger.warn(s"/!\\ File $path does not exist. Ignored.")
            return
        }

        val sql = Source.fromFile(file).getLines().mkString("\n")
        try {
            val stmt = conn.createStatement()
            stmt.execute(sql)
            logger.info(s"[OK] Script executed successfully: ${file.getName}")
        } catch {
            case ex: Exception =>
                logger.warn(s"/!\\ Error while executing script ${file.getName}: ${ex.getMessage}")
        }
    }

    def init(): Unit = {
        logger.info("[...] Initializing PostgreSQL schema")

        implicit val conn: Connection = getConnection()

        try {
            val ddlDir = AppConfig.Postgres.ddlDir

            // Tables
            val tables = Seq(
                "create_road_traffic_feats_map.sql",
                "create_road_traffic_stats_minute.sql",
                "create_road_traffic_stats_hour.sql",
                "create_road_traffic_stats_sliding_window.sql",
                "create_traffic_status_avg_speed.sql"
            )

            tables.foreach { file =>
                executeSqlFile(s"$ddlDir/tables/$file")
            }

            // Views
            val viewDir = new File(s"$ddlDir/views")
            if (viewDir.exists && viewDir.isDirectory) {
                val viewFiles = viewDir.listFiles().filter(_.getName.endsWith(".sql")).sorted
                viewFiles.foreach(view => executeSqlFile(view.getPath))
            } else {
                logger.warn(s"/!\\ Folder 'views/' not found in $ddlDir")
            }

        } finally {
            conn.close()
        }
    }

    /**
     * Deletes all tables and views from the PostgreSQL schema.
     */
    def cleanup(): Unit = {
        logger.info("[...] Cleaning up PostgreSQL schema")
        implicit val conn: Connection = getConnection()
        try {
            executeSqlFile(s"${AppConfig.Postgres.ddlDir}/cleanup/drop_all.sql")
            logger.info("[ OK ] PostgreSQL schema cleaned up.")
        } finally {
            conn.close()
        }
    }
}
