package com.goamegah.flowstate

import com.goamegah.flowstate.db.DBSchemaManager
import com.goamegah.flowstate.streaming.TrafficStreamProcessor

object MainApp {
    def main(args: Array[String]): Unit = {

        // Initialize the PostgreSQL schema
        DBSchemaManager.init()

        // Start streaming
        TrafficStreamProcessor.start()

        // Stop streaming after 5 minutes
        // Thread.sleep(5 * 60 * 1000) // 5 minutes

        // Stop streaming
        // TrafficStreamProcessor.stop()

        // Clean up the PostgreSQL schema
        // DBSchemaManager.cleanup()

        // Stop the application
        // spark.stop()

    }
}