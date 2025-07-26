package com.goamegah.flowstate.elt

import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}
import scala.util.{Try, Success, Failure}
import com.goamegah.flowstate.config.AppConfig

object TransientToRaw {

    def main(args: Array[String]): Unit = {
        val transientDir = new File(AppConfig.Airflow.transientDir)
        val rawDir = new File(AppConfig.Airflow.rawDir)

        if (!rawDir.exists()) rawDir.mkdirs()

        println(s"[INFO] Moving files from ${transientDir.getAbsolutePath} to ${rawDir.getAbsolutePath}")

        val subDirs = Option(transientDir.listFiles())
            .getOrElse(Array.empty)
            .filter(_.isDirectory)

        subDirs.foreach { dir =>
            // Only look for main JSON files (ignore .crc, _SUCCESS, etc.)
            val jsonParts = Option(dir.listFiles())
                .getOrElse(Array.empty)
                .filter(f => f.getName.endsWith(".json") && !f.getName.startsWith("."))

            jsonParts.foreach { file =>
                // Destination file name = folder name + .json (not .json.json)
                val baseName = dir.getName.stripSuffix(".json") // in case already suffixed
                val newFileName = s"$baseName.json"
                val destPath = Paths.get(rawDir.getAbsolutePath, newFileName)

                Try {
                    Files.move(file.toPath, destPath, StandardCopyOption.REPLACE_EXISTING)
                } match {
                    case Success(_) =>
                        println(s"[OK] Moved: ${file.getName} → ${destPath.getFileName}")
                    case Failure(e) =>
                        println(s"[ERROR] Failed to move ${file.getName}: ${e.getMessage}")
                }
            }

            // Clean up the folder after all files have been moved
            val deleted = Try {
                Option(dir.listFiles()).foreach(_.foreach(_.delete()))
                dir.delete()
            }

            deleted match {
                case Success(_) =>
                    println(s"[CLEANUP] Folder deleted: ${dir.getAbsolutePath}")
                case Failure(e) =>
                    println(s"[WARN] Unable to delete ${dir.getName}: ${e.getMessage}")
            }
        }

        println("[INFO] Processing complete.")
    }
}
