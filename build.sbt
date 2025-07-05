ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "realtime-traffic-monitor"
  )

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.5"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.5"
// libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.4.1" // for s3
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.4.1" // for hadoop configuration
libraryDependencies += "com.typesafe" % "config" % "1.4.3"  // for reading application.conf
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.17.37" % Test
libraryDependencies += "org.postgresql" % "postgresql" % "42.7.5"
libraryDependencies += "software.amazon.awssdk" % "s3" % "2.31.16" // for s3 sdk v2. No need hadoop-aws
libraryDependencies += "software.amazon.awssdk" % "core" % "2.31.16"
libraryDependencies += "software.amazon.awssdk" % "auth" % "2.31.16"
libraryDependencies += "software.amazon.awssdk" % "regions" % "2.31.16"

Compile / run / fork := true

Compile / run / javaOptions += "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED"

enablePlugins(AssemblyPlugin)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _                            => MergeStrategy.first
}