package multinode.build

import sbt._

object Dependencies {

  object version {
    val cats       = "2.7.0"
    val catsEffect = "3.3.4"
    val fs2        = "3.2.4"
    val zio        = "1.0.13"
    val kafka      = "2.8.1"
    val fs2Kafka   = "2.3.0"
    val zioKafka   = "0.17.3"
    val scalaPb    = "0.11.8"
    val grpc       = "1.43.2"
    val http4s     = "0.23.7"
    val protobuf   = "3.12.0"
    val newtype    = "0.4.4"
    val circe      = "0.14.1"
    val zioLogging = "0.5.14"
    val log4cats   = "2.1.1"
    val slf4j      = "1.7.33"
    val logback    = "1.2.10"
    val utest      = "0.7.10"

    val organizeImports  = "0.6.0"
    val kindProjector    = "0.13.2"
    val betterMonadicFor = "0.3.1"
  }

  object cats {
    def core         = "org.typelevel" %% "cats-core"          % version.cats
    def kernel       = "org.typelevel" %% "cats-kernel"        % version.cats
    def effect       = "org.typelevel" %% "cats-effect"        % version.catsEffect
    def effectKernel = "org.typelevel" %% "cats-effect-kernel" % version.catsEffect
    def logging      = "org.typelevel" %% "log4cats-slf4j"     % version.log4cats
  }

  object fs2 {
    def core  = "co.fs2"          %% "fs2-core"  % version.fs2
    def io    = "co.fs2"          %% "fs2-io"    % version.fs2
    def kafka = "com.github.fd4s" %% "fs2-kafka" % version.fs2Kafka
  }

  object zio {
    def zio     = "dev.zio" %% "zio"               % version.zio
    def streams = "dev.zio" %% "zio-streams"       % version.zio
    def kafka   = "dev.zio" %% "zio-kafka"         % version.zioKafka
    def logging = "dev.zio" %% "zio-logging-slf4j" % version.zioLogging
    def test    = "dev.zio" %% "zio-test"          % version.zio
    def testSbt = "dev.zio" %% "zio-test-sbt"      % version.zio
  }

  object kafka {
    def client = "org.apache.kafka"  % "kafka-clients" % version.kafka
    def server = "org.apache.kafka" %% "kafka"         % version.kafka
  }

  object grpc {
    def netty = "io.grpc" % "grpc-netty" % version.grpc
  }

  object scalapb {
    def runtime        = "com.thesamet.scalapb" %% "scalapb-runtime"      % version.scalaPb % "protobuf"
    def `runtime-grpc` = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % version.scalaPb
  }

  object http4s {
    def client = "org.http4s" %% "http4s-blaze-client" % version.http4s
  }

  object circe {
    def core    = "io.circe" %% "circe-core"    % version.circe
    def generic = "io.circe" %% "circe-generic" % version.circe
    def parser  = "io.circe" %% "circe-parser"  % version.circe

    def all = Seq(core, generic, parser)
  }

  def newtype = "io.estatico" %% "newtype" % version.newtype

  object logging {

    // sink
    def slf4j = "org.slf4j" % "slf4j-api" % version.slf4j

    // bridges
    def jcl   = "org.slf4j" % "jcl-over-slf4j"   % version.slf4j
    def log4j = "org.slf4j" % "log4j-over-slf4j" % version.slf4j
    def jul   = "org.slf4j" % "jul-to-slf4j"     % version.slf4j

    // impl
    def logback = "ch.qos.logback" % "logback-classic" % version.logback

    def default = Seq(slf4j, jcl, log4j, jul, logback)

    def exclude = Seq(
      ExclusionRule("commons-logging", "commons-logging"),
      ExclusionRule("log4j", "log4j"),
    )
  }

  def utest = "com.lihaoyi" %% "utest" % version.utest

  object plugins {
    def organizeImports = "com.github.liancheng" %% "organize-imports" % version.organizeImports
    def kindProjector   = "org.typelevel"         % "kind-projector"   % version.kindProjector cross CrossVersion.full
    def betterMonadicFor = "com.olegpy" %% "better-monadic-for" % version.betterMonadicFor
  }

  def exclude = logging.exclude

}
