package com.goamegah.flowtrack.storage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class S3StorageSpec extends AnyFlatSpec with Matchers {

  "S3Storage" should "upload to the S3 curated folder" in {
    // println("[+++] Début du test d'envoi vers S3")
    val testJson =
      s"""{
         |  "test": "s3-storage",
         |  "source": "unit-test"
         |}""".stripMargin

    S3Storage.store(testJson, "curated")  // <= cette ligne doit déclencher les logs
    // Vérifiez que le fichier a été créé dans le dossier S3
    // Note: Vous devrez peut-être utiliser un client S3 pour vérifier cela

  }
}


