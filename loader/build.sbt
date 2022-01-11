libraryDependencies ++= Seq(
  lib.cats.effect,
  lib.fs2.core,
  lib.kafka.fs2Kafka,
  lib.http4s.client,
  lib.logging.log4cats,
  lib.test.utest,
) ++ lib.circe.all ++ lib.logging.all

testFrameworks := Seq(utestFramework)
