libraryDependencies ++= Seq(
  lib.kafka.client,
  lib.newtype,
) ++ Seq(
  lib.kafka.server % "it,test"
)
