ThisBuild / organization := "work.ogai"
ThisBuild / scalaVersion := "2.13.7"
name                     := "multinode"

lazy val `producer` = project.in(file("producer"))
lazy val `consumer` = project.in(file("consumer"))
