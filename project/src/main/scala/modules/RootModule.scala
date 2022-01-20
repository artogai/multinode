package multinode.build.modules

import sbt._
import sbt.plugins.JvmPlugin
import sbtdynver.DynVerPlugin
import sbtprotoc.ProtocPlugin

object RootModule extends AutoPlugin {

  override def requires: Plugins      = JvmPlugin && DynVerPlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val lib            = multinode.build.Dependencies
    val utestFramework = new TestFramework("utest.runner.Framework")
    val ztestFramework = new TestFramework("zio.test.sbt.ZTestFramework")

    def module(id: String) =
      Project(id, file(id)).enablePlugins(SubModule)

    def protoModule(id: String) =
      module(id).enablePlugins(ProtoModule)

    def grpcModule(id: String) =
      protoModule(id).enablePlugins(GrpcModule)

    def zioGrpcModule(id: String) =
      grpcModule(id).enablePlugins(ZioGrpcModule)

    implicit class ProjectExt(val proj: Project) extends AnyVal {
      def dependsOnWithTests(dep: Project): Project =
        proj.dependsOn(dep % "compile->compile;test->test")
    }

  }

}
