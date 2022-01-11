libraryDependencies ++= Seq(
  lib.zio.zio,
  lib.zio.streams,
  lib.kafka.zioKafka,
  lib.logging.zioLogging,
) ++ lib.logging.all

testFrameworks := Seq(ztestFramework)
