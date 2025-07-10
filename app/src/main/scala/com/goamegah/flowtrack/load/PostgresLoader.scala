package com.goamegah.flowtrack.load

import com.goamegah.flowtrack.config.AppConfig
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.slf4j.LoggerFactory

object PostgresLoader {

    private val logger = LoggerFactory.getLogger(getClass)

    /** Charge un DataFrame dans une table PostgreSQL
     *
     * @param df        Le DataFrame à charger
     * @param tableName Nom de la table cible
     * @param mode      Mode de sauvegarde : "append", "overwrite", etc.
     */
    def load(df: DataFrame, tableName: String, mode: SaveMode = SaveMode.Append)(implicit spark: SparkSession): Unit = {
        val jdbcUrl = AppConfig.Postgres.jdbcUrl
        val properties = new java.util.Properties()
        properties.setProperty("user", AppConfig.Postgres.user)
        properties.setProperty("password", AppConfig.Postgres.password)
        properties.setProperty("driver", "org.postgresql.Driver")

        if (df.isEmpty) {
            logger.warn(s"/!\\ Aucun enregistrement à insérer dans '$tableName'")
            return
        }

        try {
            logger.info(s"[ OK ] Insertion dans '$tableName' en mode $mode...")

            df.write
                .mode(mode)
                .jdbc(jdbcUrl, tableName, properties)

            logger.info(s"[ OK ] Insertion réussie dans '$tableName' (${df.count()} lignes)")

        } catch {
            case e: Exception =>
                logger.error(s"/!\\ Erreur lors de l'insertion dans '$tableName' : ${e.getMessage}", e)
        }
    }

    /** Charge un DataFrame dans une table PostgreSQL en écrasant les données existantes
     *
     * @param df        Le DataFrame à charger
     * @param tableName Nom de la table cible
     */
    def overwriteLoad(df: DataFrame, tableName: String)(implicit spark: SparkSession): Unit = {
        val jdbcUrl = AppConfig.Postgres.jdbcUrl
        val conn = java.sql.DriverManager.getConnection(jdbcUrl, AppConfig.Postgres.user, AppConfig.Postgres.password)

        try {
            val stmt = conn.createStatement()
            stmt.execute(s"TRUNCATE TABLE $tableName")
            logger.info(s"[OK] Table '$tableName' vidée avec succès (TRUNCATE)")
        } catch {
            case e: Exception =>
                logger.error(s"/!\\ Erreur lors du TRUNCATE de '$tableName' : ${e.getMessage}")
        } finally {
            conn.close()
        }

        // Puis append
        load(df, tableName, SaveMode.Append)
    }

}
