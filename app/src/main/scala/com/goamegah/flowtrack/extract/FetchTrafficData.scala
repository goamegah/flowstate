package com.goamegah.flowtrack.extract

import com.goamegah.flowtrack.common.LoggerHelper
import com.goamegah.flowtrack.config.AppConfig
import scalaj.http._

import scala.util.{Failure, Success, Try}

/** Client API pour récupérer les données de trafic */
object FetchTrafficData {

    val logger = LoggerHelper.getLogger("FetchTrafficData")

    /** Récupère les données JSON depuis un endpoint.
     * Si aucun endpoint n'est fourni, utilise celui de la config.
     *
     * @param endpoint URL à appeler (optionnel)
     * @return Option[String] contenant le JSON ou None en cas d'échec
     */
    def fetchData(endpoint: Option[String] = None): Option[String] = {
        val url = endpoint.getOrElse(AppConfig.API.endpoint)

        logger.info(s"[INFO] Appel API vers : $url")

        Try(Http(url).timeout(5000, 10000).asString) match {
            case Success(response) if response.is2xx =>
                logger.info(s"[ OK ] Données reçues (${response.body.length} caractères)")
                Some(response.body)

            case Success(response) =>
                logger.info(s"/!\\ Échec API (${response.code}) : ${response.body}")
                None

            case Failure(ex) =>
                logger.info(s"/!\\ Exception lors de l’appel API : ${ex.getMessage}")
                None
        }
    }
}
