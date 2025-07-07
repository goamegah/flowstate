error id: `<none>`.
file://<WORKSPACE>/src/main/scala/com/goamegah/flowtrack/MainApp.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -com/goamegah/flowtrack/streaming/TrafficStreamProcessor.start.
	 -com/goamegah/flowtrack/streaming/TrafficStreamProcessor.start#
	 -com/goamegah/flowtrack/streaming/TrafficStreamProcessor.start().
	 -TrafficStreamProcessor.start.
	 -TrafficStreamProcessor.start#
	 -TrafficStreamProcessor.start().
	 -scala/Predef.TrafficStreamProcessor.start.
	 -scala/Predef.TrafficStreamProcessor.start#
	 -scala/Predef.TrafficStreamProcessor.start().
offset: 354
uri: file://<WORKSPACE>/src/main/scala/com/goamegah/flowtrack/MainApp.scala
text:
```scala
package com.goamegah.flowtrack

import com.goamegah.flowtrack.db.DBSchemaManager
import com.goamegah.flowtrack.streaming.TrafficStreamProcessor

object MainApp {
    def main(args: Array[String]): Unit = {

        // Initialisation du schéma PostgreSQL
        DBSchemaManager.init()

        // Démarrage du streaming
        TrafficStreamProcessor.sta@@rt()

        // Arrêt du streaming après 5 minutes
        // Thread.sleep(5 * 60 * 1000) // 5 minutes

        // Arrêt du streaming
        // TrafficStreamProcessor.stop()

        // Nettoyage du schéma PostgreSQL
        // DBSchemaManager.cleanup()

        // Arrêt de l'application
        // spark.stop()

    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.