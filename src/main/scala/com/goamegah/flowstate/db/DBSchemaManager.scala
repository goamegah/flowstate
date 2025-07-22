package com.goamegah.flowstate.db

import java.nio.file.{Files, Paths}
import java.sql.{Connection, DriverManager}
import scala.io.Source
import java.io.File
import com.goamegah.flowstate.config.AppConfig
import org.slf4j.LoggerFactory

object DBSchemaManager {
    private val logger = LoggerFactory.getLogger(this.getClass)

    // Forcer le chargement du driver PostgreSQL
    Class.forName("org.postgresql.Driver")

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

            // Vues
            val viewDir = new File(s"$ddlDir/views")
            if (viewDir.exists && viewDir.isDirectory) {
                val viewFiles = viewDir.listFiles().filter(_.getName.endsWith(".sql")).sorted
                viewFiles.foreach(view => executeSqlFile(view.getPath))
            } else {
                logger.warn(s"/!\\ Dossier 'views/' introuvable dans $ddlDir")
            }

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
            executeSqlFile(s"${AppConfig.Postgres.ddlDir}/cleanup/drop_all.sql")
            logger.info("[ OK ] Schéma PostgreSQL nettoyé.")
        } finally {
            conn.close()
        }
    }
}
