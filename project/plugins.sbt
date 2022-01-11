addDependencyTreePlugin

addSbtPlugin("com.timushev.sbt"  % "sbt-updates"  % "0.5.3")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt" % "2.4.5")
addSbtPlugin("com.timushev.sbt"  % "sbt-rewarn"   % "0.1.3")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix" % "0.9.33")
addSbtPlugin("com.eed3si9n"      % "sbt-assembly" % "1.1.0")
addSbtPlugin("com.dwijnand"      % "sbt-dynver"   % "4.1.1")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker"   % "1.8.2")
addSbtPlugin("com.thesamet"      % "sbt-protoc"   % "1.0.3")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb"          %% "compilerplugin"   % "0.11.1",
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.5.1",
)
