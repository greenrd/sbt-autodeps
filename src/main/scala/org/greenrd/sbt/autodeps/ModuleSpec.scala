package org.greenrd.sbt.autodeps

import sbt._

case class ModuleSpec(organization: String, name: String, revision: String, crossVersion: Boolean) {
  
  /** Converts to Scala code. */
  override def toString() = {
    '"' + organization + "\" %" + (if(crossVersion) "%" else "") + " \"" + name + "\" % \"" + revision + '"'
  }

}

object ModuleSpec {

  /** Removes any Scala version suffix from module name. */
  def removeCrossVersion(moduleName: String) = {
    val i = moduleName.lastIndexOf('_')
    if(i == -1) moduleName else moduleName.substring(0, i)
  }

  /** Reverse engineers a module spec from a ModuleID. */
  def apply(moduleID: ModuleID): ModuleSpec = {
    ModuleSpec(organization = moduleID.organization,
               name = removeCrossVersion(moduleID.name),
               revision = moduleID.revision,
               crossVersion = moduleID.name.contains('_'))
  }

}
