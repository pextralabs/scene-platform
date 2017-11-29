import play.sbt.PlayJava
import play.sbt.routes.RoutesKeys._
import sbt.Keys.{publishTo, _}

//scalaVersion in Global := "2.11.7"

organization := "br.ufes.inf.lprm"
name := SceneBuild.ProjectPrefix + "platform"
version := "1.0.0"

def ProjectDef(name: String, v: String): Project =  Project(name, file(name)).settings(version := v)

resolvers in Global ++= Seq(Resolver.mavenLocal,
                            "jboss" at "http://repository.jboss.org/nexus/content/groups/public/" ,
                            "repo1" at "http://repo1.maven.org/maven2/" )

lazy val model = ProjectDef("situation-model", "0.10.2").
                    settings(Common.settings: _*).
                    settings(
                      autoScalaLibrary := false,
                      libraryDependencies ++= Dependencies.modelDependencies,
                      publishTo := Common.mavenRepo
                    ).dependsOn()

lazy val core = ProjectDef("scene-core", "0.10.8-rc1")
                        .settings(Common.settings: _*)
                        .settings(
                          autoScalaLibrary := false,
                          libraryDependencies ++= Dependencies.coreDependencies,
                          isSnapshot := true,
                          publishTo := Common.mavenRepo
                        ).dependsOn(model)

lazy val examples = ProjectDef("scene-examples", "0.10.1")
                      .settings(Common.settings: _*)
                      .dependsOn(core)

/*lazy val server = ProjectDef("scene-server", "0.1.0")
  .enablePlugins(PlayJava)
  .disablePlugins(PlayLogback)
  .settings(Common.settings: _*)
  .settings(
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(javaCore, filters),
    routesGenerator := InjectedRoutesGenerator
  ).dependsOn(core)*/
