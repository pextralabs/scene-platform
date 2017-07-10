import sbt._
import Keys._

object Dependencies {

  val `junit-version`       = "4.8.1"
  val `slf4j-version`       = "1.7.22"
  val `drools-version`      = "6.5.0.Final"
  val `reflections-version` = "0.9.10"
  val `joda-time-version`   = "2.3"
  val `gson-version`        = "2.7"
  val `javassist-version`   = "3.21.0-GA"

  val junit             = "junit" % "junit" % `junit-version`
  val `slf4j-log4j12`   = "org.slf4j" % "slf4j-log4j12" % `slf4j-version`
  val `slf4j-api`       = "org.slf4j" % "slf4j-api" % `slf4j-version`
  val `drools-compiler` = "org.drools" % "drools-compiler" % `drools-version`
  val `drools-core`     = "org.drools" % "drools-core" % `drools-version`
  val `kie-api`         = "org.kie" % "kie-api" % `drools-version`
  val `reflections`     = "org.reflections" % "reflections" % `reflections-version`
  val gson              = "com.google.code.gson" % "gson" % `gson-version`
  val `joda-time`       = "joda-time" % "joda-time" % `joda-time-version`
  val `javassist`       = "org.javassist" % "javassist" % `javassist-version`

  val commonDependencies: Seq[ModuleID] = Seq(
    junit,
    `slf4j-log4j12`
  )

  val droolsDependencies: Seq[ModuleID] = Seq(
    `drools-compiler`,
    `drools-core`,
    `kie-api`
  )

  val modelDependencies: Seq[ModuleID] = commonDependencies ++ Seq(
    `joda-time`
  )

  val coreDependencies: Seq[ModuleID] = commonDependencies ++ droolsDependencies ++ Seq(
    gson,
    reflections,
    javassist
  )

}
