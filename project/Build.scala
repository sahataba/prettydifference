import sbt.Keys._
import sbt._

object BuildSettings {
  val buildOrganization = "squads.com"
  val buildVersion = "0.1"
  val buildScalaVersion = "2.11.8"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-target:jvm-1.7", "-unchecked", "-deprecation", "-encoding", "utf8", "-Xlint", "-Xfatal-warnings", "-Ywarn-unused", "-Ywarn-unused-import", "-feature"),
    javaOptions += "-Xmx1G",
    shellPrompt := ShellPrompt.buildShellPrompt
  )
}

object Resolvers {
  val typesafe = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"
  val repositories = Seq(typesafe)
}


object Dependencies {
  val dependencies = Seq("com.chuusai" %% "shapeless" % "2.3.0")
}

object ThisBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import Resolvers._

  lazy val switchOffInConsole = Set("-Xfatal-warnings", "-Ywarn-unused", "-Ywarn-unused-import")

  lazy val api = Project(
    "prettydifference", file("."),
    settings = buildSettings
      ++ Seq(resolvers := repositories, libraryDependencies ++= dependencies)
  ).configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .settings(parallelExecution in Test := false)
    .settings(scalaSource in IntegrationTest <<= baseDirectory / "src/test/scala")
    .settings(resourceDirectory in IntegrationTest <<= baseDirectory / "src/test/resources")
    .settings(testOptions in IntegrationTest := Seq(Tests.Filter(s => s.endsWith("IT"))))
    .settings( scalacOptions in (Compile, console) ~= (_ filterNot (opt => switchOffInConsole contains opt)))
    .settings( scalacOptions in (Test, console) ~= (_ filterNot (opt => switchOffInConsole contains opt)))
    .settings(mainClass in (Compile, run) := Some("Boot"))
}

// Shell prompt which show the current project,
// git branch and build version
object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: => String) {}

    def error(s: => String) {}

    def buffer[T](f: => T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
    )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract(state).currentProject.id
      "%s:%s:%s> ".format(
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}
