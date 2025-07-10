package com.goamegah.flowtrack.storage

/** Trait représentant un backend de stockage de données (local, S3, etc.) */
trait StorageBackend {
  def store(data: String, prefix: String): Unit
  def retrieve(fileName: String, prefix: String = "raw"): Unit
}

