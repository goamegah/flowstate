package com.goamegah.flowtrack.storage

import com.goamegah.flowtrack.common.LoggerHelper

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}
import com.goamegah.flowtrack.config.AppConfig

object LocalStorage extends StorageBackend {
    val logger: LoggerHelper.ColoredLogger = LoggerHelper.getLogger("LocalStorage")

    /** Stocke les données dans un fichier local
     *
     * @param data   Les données à stocker
     * @param prefix Le chemin dans le bucket (par défaut : raw)
     */
    override def store(data: String, prefix: String="checkpoint"): Unit = {
        val dir = AppConfig.Local.dataDir
        val fileName = s"$dir/$prefix/traffic_${System.currentTimeMillis()}.json"
        Files.createDirectories(Paths.get(dir, prefix))

        val localStorageWriter = new PrintWriter(new File(fileName))
        localStorageWriter.write(data)
        localStorageWriter.close()
        logger.info(s"[ OK ] Données stockées localement dans : $fileName")
    }

    /** Récupère les données depuis le stockage local
     *
     * @param fileName Le nom du fichier à récupérer
     * @param prefix   Le chemin dans le bucket (par défaut : raw)
     */
    override def retrieve(fileName: String, prefix: String): Unit = {
        val dir = AppConfig.Local.dataDir
        val filePath = s"$dir/$prefix/$fileName"

        if (Files.exists(Paths.get(filePath))) {
            logger.info(s"[ OK ] Fichier récupéré : $filePath")
        } else {
            println(s"/!\\ Fichier non trouvé : $filePath")
        }
    }

    def listFiles(prefix: String): Seq[File] = {
        val dir = new File(s"${AppConfig.Local.dataDir}/$prefix")
        if (dir.exists && dir.isDirectory) {
            dir.listFiles().toSeq
        } else Seq.empty
    }

}

