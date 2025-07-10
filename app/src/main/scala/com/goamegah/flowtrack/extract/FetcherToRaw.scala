package com.goamegah.flowtrack.extract

import com.goamegah.flowtrack.common.LoggerHelper
import com.goamegah.flowtrack.storage.LocalStorage

object FetcherToRaw {

    def main(args: Array[String]): Unit = {
        val logger = LoggerHelper.getLogger("FetcherToRaw")

        logger.info("[INFO] Lancement de la collecte manuelle vers raw/ ...")

        val dataOpt = FetchTrafficData.fetchData()

        dataOpt match {
            case Some(json) =>
                // On stocke dans le dossier "raw" (pour être lu par readStream)
                LocalStorage.store(json, prefix = "raw")
                logger.info("[ OK ] Données stockées dans raw/")
            case None =>
                logger.info("/!\\ Aucune donnée reçue de l'API.")
        }
    }
}
