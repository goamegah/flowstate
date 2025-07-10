package com.goamegah.flowtrack.storage


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.nio.file.{Files, Paths}
import com.goamegah.flowtrack.config.AppConfig

class LocalStorageSpec extends AnyFlatSpec with Matchers {

  "LocalStorage" should "store data in a local file" in {
    val testJson = """{ "test": "ok" }"""
    val dirPath = Paths.get(AppConfig.Local.dataDir, "raw")

    // Crée le dossier s'il n'existe pas
    Files.createDirectories(dirPath)

    val before = Option(dirPath.toFile.listFiles()).map(_.map(_.getName).toSet).getOrElse(Set.empty)

    // Appel de la méthode
    LocalStorage.store(testJson, "raw")
    val after = Option(dirPath.toFile.listFiles()).map(_.map(_.getName).toSet).getOrElse(Set.empty)
    val newFiles = after.diff(before)
    newFiles should not be empty
  }
}
