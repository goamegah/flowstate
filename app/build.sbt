ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
      name := "realtime-traffic-monitor",

      libraryDependencies ++= Seq(
          // Spark en scope provided pour ne pas embarquer les librairies Spark
          "org.apache.spark" %% "spark-core" % "3.5.5" % "provided" exclude("org.slf4j", "slf4j-log4j12"),
          "org.apache.spark" %% "spark-sql"  % "3.5.5" % "provided" exclude("org.slf4j", "slf4j-log4j12"),

          // Hadoop (S3A, FS, etc.)
          "org.apache.hadoop"       % "hadoop-common"      % "3.4.1",

          // Config parser
          "com.typesafe"            % "config"             % "1.4.3",

          // HTTP client
          "org.scalaj"             %% "scalaj-http"        % "2.4.2",

          // PostgreSQL JDBC
          "org.postgresql"          % "postgresql"         % "42.7.5",

          // AWS SDK v2 (S3)
          "software.amazon.awssdk"  % "s3"                 % "2.31.16",
          "software.amazon.awssdk"  % "core"               % "2.31.16",
          "software.amazon.awssdk"  % "auth"               % "2.31.16",
          "software.amazon.awssdk"  % "regions"            % "2.31.16",

          // Log4j2 + bridge SLF4J
          "org.apache.logging.log4j" % "log4j-api"         % "2.17.1",
          "org.apache.logging.log4j" % "log4j-core"        % "2.17.1",
          "org.apache.logging.log4j" % "log4j-slf4j-impl"  % "2.17.1",

          // Tests
          "org.scalatest"           %% "scalatest"          % "3.2.19" % Test,
          "org.mockito"             %% "mockito-scala"      % "1.17.37" % Test
      ),

      // fork pour sbt run
      Compile / run / fork := true,
      Compile / run / javaOptions += "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",

      // Assembly (fat-jar) : on jette les signature files et on concatène les services
      assembly / assemblyMergeStrategy := {
          case PathList("META-INF", "MANIFEST.MF")                                    => MergeStrategy.discard
          case PathList("META-INF", xs @ _*) if xs.exists(n => n.endsWith(".SF") || n.endsWith(".RSA") || n.endsWith(".DSA"))
          => MergeStrategy.discard
          case PathList("META-INF", "services", _*)                                   => MergeStrategy.concat
          case PathList("META-INF", _ @ _*)                                           => MergeStrategy.first
          case _                                                                       => MergeStrategy.first
      },

      // Classe main
      assembly / mainClass := Some("com.goamegah.flowtrack.MainApp")
  )

enablePlugins(AssemblyPlugin)
