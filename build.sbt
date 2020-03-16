organization in ThisBuild := "be.reaktika"
version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.13.1"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `foodorderingdemo-scala-lagom` = (project in file("."))
  .aggregate(`foodordering-api`, `foodordering-impl`)

lazy val `foodordering-api` = (project in file("foodordering-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `foodordering-impl` = (project in file("foodordering-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`foodordering-api`)
