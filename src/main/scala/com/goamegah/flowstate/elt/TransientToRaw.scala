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

        println(s"[INFO] Déplacement des fichiers de ${transientDir.getAbsolutePath} vers ${rawDir.getAbsolutePath}")

        val subDirs = Option(transientDir.listFiles())
            .getOrElse(Array.empty)
            .filter(_.isDirectory)

        subDirs.foreach { dir =>
            // Rechercher uniquement les fichiers JSON principaux (ignore les .crc, _SUCCESS, etc.)
            val jsonParts = Option(dir.listFiles())
                .getOrElse(Array.empty)
                .filter(f => f.getName.endsWith(".json") && !f.getName.startsWith("."))

            jsonParts.foreach { file =>
                // Nom du fichier destination = nom du dossier + .json (pas .json.json)
                val baseName = dir.getName.stripSuffix(".json") // si jamais déjà suffixé
                val newFileName = s"$baseName.json"
                val destPath = Paths.get(rawDir.getAbsolutePath, newFileName)

                Try {
                    Files.move(file.toPath, destPath, StandardCopyOption.REPLACE_EXISTING)
                } match {
                    case Success(_) =>
                        println(s"[OK] Déplacé : ${file.getName} → ${destPath.getFileName}")
                    case Failure(e) =>
                        println(s"[ERREUR] Échec du déplacement de ${file.getName} : ${e.getMessage}")
                }
            }

            // Nettoyage du dossier une fois tous les fichiers déplacés
            val deleted = Try {
                Option(dir.listFiles()).foreach(_.foreach(_.delete()))
                dir.delete()
            }

            deleted match {
                case Success(_) =>
                    println(s"[CLEANUP] Dossier supprimé : ${dir.getAbsolutePath}")
                case Failure(e) =>
                    println(s"[WARN] Impossible de supprimer ${dir.getName} : ${e.getMessage}")
            }
        }

        println("[INFO] Traitement terminé.")
    }
}
