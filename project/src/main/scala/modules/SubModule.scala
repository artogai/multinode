package multinode.build.modules

import sbt._
import Keys._
import multinode.build.Dependencies

object SubModule extends AutoPlugin {
  override def requires: Plugins      = RootModule
  override def trigger: PluginTrigger = noTrigger

  import RootModule.autoImport._
  override lazy val projectSettings = Seq(
    addCompilerPlugin(Dependencies.plugins.kindProjector),
    addCompilerPlugin(Dependencies.plugins.betterMonadicFor),
    excludeDependencies ++= lib.exclude,
  )
}
