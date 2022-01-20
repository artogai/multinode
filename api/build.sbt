libraryDependencies ++=
  Seq(
    lib.zio.zio,
    lib.zio.streams,
    lib.zio.kafka,
    lib.zio.logging,
  ) ++
    Seq(
      lib.zio.test,
      lib.zio.testSbt,
    ).map(_ % "it,test")

testFrameworks := Seq(ztestFramework)
