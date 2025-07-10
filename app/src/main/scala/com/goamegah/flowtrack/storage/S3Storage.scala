package com.goamegah.flowtrack.storage

import com.goamegah.flowtrack.common.LoggerHelper
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import com.goamegah.flowtrack.config.AppConfig

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
//import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.collection.JavaConverters._

object S3Storage extends StorageBackend {

    val logger: LoggerHelper.ColoredLogger = LoggerHelper.getLogger("S3Storage")

    private val s3Client: S3Client = S3Client.builder()
        .region(Region.of(AppConfig.AWS.region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(AppConfig.AWS.accessKey, AppConfig.AWS.secretKey)
            )
        ).build()

    private def generateFileName(): String = {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
        s"traffic_$timestamp.json"
    }

    /** Stocke les données dans un sous-dossier S3 configurable (ex: raw/, curated/, etc.)
     *
     * @param data   Le contenu JSON à stocker
     * @param prefix Le chemin dans le bucket (par défaut : raw)
     */
    def store(data: String, prefix: String = "raw"): Unit = {
        val fileName = generateFileName()
        val bucket = AppConfig.AWS.rawBucket.replace("s3a://", "")
        val key = s"$prefix/$fileName"

        try {
            val request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()

            val body = ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))
            s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromByteBuffer(body))

            logger.info(s"[ OK ] Données stockées sur S3 : s3://$bucket/$key")
        } catch {
            case ex: Exception =>
                logger.info(s"/!\\ Erreur lors du stockage S3 : ${ex.getMessage}")
        }
    }

    /** Récupère les données depuis S3
     *
     * @param prefix Le chemin dans le bucket (par défaut : raw)
     */
    def retrieve(fileName: String, prefix: String = "raw"): Unit = {
        val bucket = AppConfig.AWS.rawBucket.replace("s3a://", "")
        val key = s"$prefix/$fileName"

        try {
            val request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()

            val response = s3Client.getObject(request)
            val data = new String(response.readAllBytes(), StandardCharsets.UTF_8)
            logger.info(s"[ OK ] Données récupérées depuis S3 : s3://$bucket/$key")
            logger.info(s"[DEBUG] Contenu : $data")
        } catch {
            case ex: NoSuchKeyException =>
                println(s"/!\\ Fichier non trouvé : s3://$bucket/$key")
            case ex: Exception =>
                println(s"/!\\ Erreur lors de la récupération S3 : ${ex.getMessage}")
        }
    }

    def list(prefix: String = "raw"): Unit = {
        val bucket = AppConfig.AWS.rawBucket.replace("s3a://", "")
        val request = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix(prefix + "/")
            .build()
        val result = s3Client.listObjectsV2(request)
        val files = result.contents().asScala.map(_.key())
        logger.info(s"[DEBUG] Fichiers dans S3 ($prefix):")
        files.foreach(k => println(s" - $k"))
    }

}
