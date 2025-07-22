ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
    .settings(
        name := "flowstate",
        libraryDependencies ++= Seq(
            "org.apache.spark" %% "spark-core" % "4.0.0",
            "org.apache.spark" %% "spark-sql" % "4.0.0",
            "org.apache.hadoop" % "hadoop-aws" % "3.4.1",
            "org.apache.hadoop" % "hadoop-common" % "3.4.1",
            "com.typesafe" % "config" % "1.4.4",
            "org.scalaj" %% "scalaj-http" % "2.4.2",
            "org.postgresql" % "postgresql" % "42.7.7",
            "org.scalatest" %% "scalatest" % "3.2.19" % Test,
            "org.apache.logging.log4j" % "log4j-core" % "2.25.1",
            "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.25.1",

        ),
        assembly / assemblyJarName := "flowstate-assembly.jar",
        assembly / mainClass := Some("com.goamegah.flowstate.MainApp"),
        assembly / assemblyMergeStrategy := {
            case PathList("META-INF", _*) => MergeStrategy.discard
            case "reference.conf"         => MergeStrategy.concat
            case _                        => MergeStrategy.first
        }
    )
enablePlugins(AssemblyPlugin)