ThisBuild / organization := "dev.ogai"
name                     := "multinode"
ThisBuild / scalaVersion := "2.13.7"

lazy val model  = protoModule("model")
lazy val loader = module("loader").dependsOnWithTests(model)
lazy val api    = zioGrpcModule("api").dependsOnWithTests(model)
