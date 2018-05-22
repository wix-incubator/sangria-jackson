name := "sangria-jackson"
organization := "com.wix"
version := "0.1.0-SNAPSHOT"

description := "Sangria jackson marshalling"
licenses := Seq("Apache License, ASL Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.8", "2.12.1")

val jacksonVersion = "2.9.5"

scalacOptions ++= Seq("-deprecation", "-feature")

scalacOptions ++= {
  if (scalaVersion.value startsWith "2.12")
    Seq.empty
  else
    Seq("-target:jvm-1.7")
}

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion % "test",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion % "test",
  "org.sangria-graphql" %% "sangria-marshalling-testkit" % "1.0.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

git.remoteRepo := "git@github.com:wix-incubator/sangria-jackson.git"

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := (_ => false)
publishTo := Some(
  if (version.value.trim.endsWith("SNAPSHOT"))
    "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")

site.settings
site.includeScaladoc()
ghpages.settings

startYear := Some(2018)
organizationHomepage := Some(url("https://github.com/wix-incubator"))
developers := Developer("laurynasl-wix", "Laurynas Lubys", "", url("https://github.com/laurynasl-wix")) :: Nil
