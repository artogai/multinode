libraryDependencies ++=
  Seq(
    lib.cats.effect,
    lib.fs2.core,
    lib.fs2.kafka,
    lib.http4s.client,
    lib.cats.logging,
  ) ++
    lib.circe.all ++
    Seq(
      lib.utest
    ).map(_ % "it,test")

testFrameworks := Seq(utestFramework)
