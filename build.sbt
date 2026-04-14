name := "Projet_Critique_2026"

version := "1.0"

scalaVersion := "3.3.1"

// Utilisation d'une version stable et accessible sans configuration complexe
val AkkaVersion = "2.6.21" 

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"       % AkkaVersion,
  "ch.qos.logback"    %  "logback-classic"   % "1.4.11"
)