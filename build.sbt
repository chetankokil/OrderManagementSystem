name := "OrderManagement"

version := "0.1"

scalaVersion := "2.12.13"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"      % "0.12.1",
  "org.tpolecat" %% "doobie-h2"        % "0.12.1",
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
)