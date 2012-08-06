package org.greenrd.sbt.autodeps

import java.io.{BufferedInputStream, BufferedOutputStream, FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.zip.{ZipEntry,ZipFile}
import org.apache.commons.collections.trie.{PatriciaTrie,StringKeyAnalyzer}
import sbt._
import sbt.Keys._
import scala.collection.JavaConversions._

object AutoDepsPlugin extends Plugin {

  type Index = PatriciaTrie[String,ModuleSpec]
  
  object AutoDepsKeys {

    val genIndexTask = TaskKey[Index]("autodeps-index")
    val indexFile = SettingKey[File]("autodeps-index-file", "The file to contain the jar file index for dependency suggestions")
    val suggestNewDepsTask = InputKey[Unit]("autodeps-suggest", "Suggest a dependency to add to fix the given missing import")

  }

  import AutoDepsKeys._
  import resource._

  private def deserializeIfExists[T <: java.io.Serializable](f: File, log: Logger): Option[T] = try {
    for(_ <- Some(f) if f.exists()) yield managed(new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)))).acquireAndGet(_.readObject().asInstanceOf[T])
    } catch {
      case(e: Exception) => {
        log.info("Unable to deserialize " + f + " due to " + e)
        None 
      }
    }

  private def serialize[T <: java.io.Serializable](f: File, x: T): T = {
    managed(new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)))).foreach(_.writeObject(x))
    x
  }

  private def entries(jarFile: File): Set[ZipEntry] = {
    managed(new ZipFile(jarFile)).acquireAndGet(_.entries().toSet)
  }

  private def recordPackages(trie: Index, moduleSpec: ModuleSpec, jarFile: File) = {
    for(e <- entries(jarFile) if !e.isDirectory) {
      Option(new File(e.getName).getParent).foreach(trie.put(_, moduleSpec))
    }
  }

  private def readIndex(s: TaskStreams, indexFile: File) = {
    deserializeIfExists(indexFile, s.log).getOrElse (new PatriciaTrie[String,ModuleSpec](StringKeyAnalyzer.INSTANCE))
  }

  private def genIndex(s: TaskStreams, indexFile: File, report: UpdateReport) = {
    val trie = readIndex(s, indexFile)
    for (c <- report.configurations; m <- c.modules) {
      val moduleSpec = ModuleSpec(m.module)
      for ((artifact, file) <- m.artifacts) {
        recordPackages(trie, moduleSpec, file)
      }
    }
    serialize(indexFile, trie)
  }

  private def packageOf(missingImport: String): String = {
    val i = missingImport.lastIndexOf('.')
    missingImport.substring(0, i)
  }

  private def suggestNewDeps(s: TaskStreams, indexFile: File, missingImport: String): Set[ModuleSpec] = {
    val trie = mapAsScalaMap(readIndex(s, indexFile))
    // XXX: At the moment, the trie can only record at most one suggestion per package
    val suggestions = trie.get(packageOf(missingImport)).toSet
    s.log.info(suggestions.mkString(" OR "))
    suggestions
  }

  val newSettings = Seq(
    genIndexTask <<= (streams, indexFile, update) map genIndex,
    indexFile := BuildPaths.defaultGlobalBase / "autodeps-index",
    suggestNewDepsTask <<= inputTask { (argTask: TaskKey[Seq[String]]) =>
      (argTask, streams, indexFile) map { (args, s, i) => args foreach (suggestNewDeps(s, i, _)) }
                                    }
  )

}
