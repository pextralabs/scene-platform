import sbt._
import Keys._

object Common {

  val mavenRepo = Some("Maven" at "https://mymavenrepo.com/repo/PkZASKyCrrcPaIvQ1RtQ/")

  val settings: Seq[Def.Setting[_]] = Seq(
    organization := "br.ufes.inf.lprm",
    crossPaths := false,
    autoScalaLibrary := false,
    scalaVersion := "2.11.7"
  )
}
