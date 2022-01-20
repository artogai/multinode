package multinode.build.modules

import sbt._
import Keys._
import sbtprotoc.ProtocPlugin
import multinode.build.Dependencies
import org.apache.tools.ant.taskdefs.optional.depend.Depend
import scalapb.GeneratorOption

object GrpcModule extends AutoPlugin {
  override def requires: Plugins      = ProtoModule
  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    val depsProtoSources = settingKey[Seq[File]]("Proto sources from module dependencies")
  }

  import ProtocPlugin.autoImport._
  import ProtoModule.autoImport._
  import autoImport._
  override def projectSettings: Seq[Setting[_]] =
    Seq(
      libraryDependencies ++= Seq(
        Dependencies.grpc.netty,
        Dependencies.scalapb.`runtime-grpc`,
      ),
      scalapbGenOptions += GeneratorOption.Grpc,
      depsProtoSources := Def.settingDyn {
        val refs   = thisProject.value.dependencies.map(_.project)
        val filter = ScopeFilter(inProjects(refs: _*))
        Def.setting {
          sourceDirectory.all(filter).value.map(_ / "main" / "protobuf")
        }
      }.value,
      Compile / PB.targets := Seq(
        scalapb.gen(scalapbGenOptions.value) -> (Compile / sourceManaged).value / "scalapb"
      ),
    )
}
