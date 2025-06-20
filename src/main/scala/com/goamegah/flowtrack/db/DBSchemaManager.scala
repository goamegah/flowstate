package com.goamegah.flowtrack.db

import java.nio.file.{Files, Paths}
import java.sql.{Connection, DriverManager}
import scala.io.Source
import java.io.File
import com.goamegah.flowtrack.config.AppConfig
import com.goamegah.flowtrack.load.PostgresLoader.getClass
import org.slf4j.LoggerFactory

object DBSchemaManager {
    private val logger = LoggerFactory.getLogger(getClass)

    private def getConnection(): Connection = {
        DriverManager.getConnection(
            AppConfig.Postgres.jdbcUrl,
            AppConfig.Postgres.user,
            AppConfig.Postgres.password
        )
    }

    /**
     * Exécute un fichier SQL sur la base de données PostgreSQL.
     *
     * @param path Le chemin du fichier SQL à exécuter.
     * @param conn La connexion à la base de données.
     */
    private def executeSqlFile(path: String)(implicit conn: Connection): Unit = {
        val file = new File(path)
        if (!file.exists()) {
            logger.warn(s"/!\\ Le fichier $path n’existe pas. Ignoré.")
            return
        }

        val sql = Source.fromFile(file).getLines().mkString("\n")
        try {
            val stmt = conn.createStatement()
            stmt.execute(sql)
            logger.info(s"[OK] Script exécuté avec succès : ${file.getName}")
        } catch {
            case ex: Exception =>
                logger.warn(s"/!\\ Erreur lors de l'exécution du script ${file.getName} : ${ex.getMessage}")
        }
    }

    def init(): Unit = {
        logger.info("[...] Initialisation du schéma PostgreSQL")
        // Implicitly make accessible the variable for other function without explicitly passing the values
        implicit val conn: Connection = getConnection()

        try {
            // Exécution des tables
            executeSqlFile("database/ddl/tables/create_road_traffic_stats_minute.sql")
            executeSqlFile("database/ddl/tables/create_road_traffic_stats_hour.sql")
            executeSqlFile("database/ddl/tables/create_road_traffic_stats_sliding_window.sql")
            executeSqlFile("database/ddl/tables/create_avg_speed_by_max_speed_and_status.sql")
            // Exécution des vues
            val viewDir = new File("database/ddl/views/")
            if (viewDir.exists && viewDir.isDirectory) {
                val viewFiles = viewDir.listFiles().filter(_.getName.endsWith(".sql")).sorted
                viewFiles.foreach(view => executeSqlFile(view.getPath))
            } else {
                logger.warn("/!\\ Le dossier 'ddl/views/' est introuvable.")
            }

            logger.info("[ OK ] Schéma PostgreSQL initialisé.")
        } finally {
            conn.close()
        }
    }

    /**
     * Supprime toutes les tables et vues du schéma PostgreSQL.
     */
    def cleanup(): Unit = {
        logger.info("[...] Nettoyage du schéma PostgreSQL")
        implicit val conn: Connection = getConnection()
        try {
            executeSqlFile("database/ddl/cleanup/drop_all.sql")
            logger.info("[ OK ] Schéma PostgreSQL nettoyé.")
        } finally {
            conn.close()
        }
    }
}
