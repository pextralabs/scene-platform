import play.sbt.PlayJava
import play.sbt.routes.RoutesKeys._
import sbt.Keys._

scalaVersion in Global := "2.11.6"

def ProjectName(name: String,path:String): Project =  Project(name, file(path))

organization := "br.ufes.inf.lprm"
name := "scene-platform"
version := "2.0.0"

libraryDependencies in Global ++= Seq()

resolvers in Global ++= Seq(Resolver.mavenLocal,
                            "jboss" at "http://repository.jboss.org/nexus/content/groups/public/" ,
                            "repo1" at "http://repo1.maven.org/maven2/" )

val `com.google.code.gson_gson` = "com.google.code.gson" % "gson" % "2.7"
val `joda-time_joda-time` = "joda-time" % "joda-time" % "2.3"
val `junit_junit` = "junit" % "junit" % "4.8.1"
val `org.drools_drools-compiler` = "org.drools" % "drools-compiler" % "6.5.0.Final"
val `org.drools_drools-core` = "org.drools" % "drools-core" % "6.5.0.Final"
val `org.kie_kie-api` = "org.kie" % "kie-api" % "6.5.0.Final"
val `org.slf4j_slf4j-log4j12` = "org.slf4j" % "slf4j-log4j12" % "1.7.21"
val `org.slf4j_slf4j-api` = "org.slf4j" % "slf4j-api" % "1.7.21"

lazy val `situation-model` = ProjectName("situation-model","situation-model").settings(
  libraryDependencies ++= Seq(`joda-time_joda-time`),
    name := "situation-model",
    version := "2.0.0",
    organization := "br.ufes.inf.lprm"
).settings().dependsOn()

lazy val `scene-examples` = ProjectName("scene-examples","scene-examples").settings(
  libraryDependencies ++= Seq(),
    name := "scene-examples",
    version := "2.0.0",
    organization := "br.ufes.inf.lprm"
).settings().dependsOn(`scene-core`)

lazy val `scene-core` = ProjectName("scene-core","scene-core").settings(
  libraryDependencies ++= Seq(`org.slf4j_slf4j-log4j12`,
   `org.slf4j_slf4j-api`,
   `org.kie_kie-api`,
   `org.drools_drools-core`,
   `junit_junit`,
   `org.drools_drools-compiler`,
    `joda-time_joda-time`,
    `com.google.code.gson_gson`),
    name := "scene-core",
    version := "2.0.0",
    organization := "br.ufes.inf.lprm"
).settings().dependsOn(`situation-model`)

lazy val `scene-play` = ProjectName("scene-play","scene-play").enablePlugins(PlayJava).settings(
  libraryDependencies ++= Seq(`org.slf4j_slf4j-log4j12`,
    `org.slf4j_slf4j-api`,
    `org.kie_kie-api`,
    `org.drools_drools-core`,
    `org.drools_drools-compiler`),
  name := "scene-play",
  version := "2.0.0",
  organization := "br.ufes.inf.lprm",
  routesGenerator := InjectedRoutesGenerator
).settings().dependsOn(`scene-core`)

