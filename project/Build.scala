import sbt._

object MyBuild extends Build {

    lazy val root = Project("root", file(".")) dependsOn(eventsModelProject)
    lazy val eventsModelProject = RootProject(uri("https://vitalcode:vital'CHik163101@gitlab.com/vitalcode/events-model.git"))

}