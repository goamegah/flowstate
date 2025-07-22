package com.goamegah.flowstate

import com.goamegah.flowstate.db.DBSchemaManager
import com.goamegah.flowstate.streaming.TrafficStreamProcessor

object MainApp {
    def main(args: Array[String]): Unit = {

        // Initialisation du schéma PostgreSQL
        DBSchemaManager.init()

        // Démarrage du streaming
        TrafficStreamProcessor.start()
    }
}