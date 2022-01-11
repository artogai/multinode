package multinode.build.modules

import sbt._
import Keys._
import sbtprotoc.ProtocPlugin
import multinode.build.Dependencies
import org.apache.tools.ant.taskdefs.optional.depend.Depend
import sbt.plugins.DependencyTreeSettings
import xsbti.compile.CompileAnalysis

object ZioGrpcModule extends AutoPlugin {
  override def requires: Plugins      = GrpcModule
  override def trigger: PluginTrigger = noTrigger

  import ProtocPlugin.autoImport._
  import GrpcModule.autoImport._

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      Compile / PB.protoSources ++=
        (Compile / PB.protoSources).value ++ depsProtoSources.value,
      Compile / PB.targets +=
        scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb",
    )

}
