package org.greenrd.sbt.autodeps

import sbt._
import Keys._

object AutoDepsPlugin extends Plugin {
  
  object AutoDepsKeys {

    val genIndexTask = TaskKey[Unit]("autodeps-index")

  }

  import AutoDepsKeys._

  private def genIndex(report: UpdateReport) = {
      for (c <- report.configurations; m <- c.modules; artifactPair <- m.artifacts) {
        println(m.module + " contains " + artifactPair)
      }
  }

  val newSettings = Seq(
    genIndexTask <<= update map genIndex
  )

}
