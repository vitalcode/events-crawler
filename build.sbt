import com.typesafe.sbt.packager.docker._

name := """events-crawler"""
organization := "vitalcode"
version := "0.0.1"
scalaVersion := "2.11.7"

resolvers ++= Seq(
    "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/",
    "Cloudera" at "https://repository.cloudera.com/artifactory/public/",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies ++= {
    val joddV = "3.6.7"
    val clouderaV = "cdh5.4.7"
    val zooV = s"3.4.5-$clouderaV"
    val hadoopV = s"2.6.0-$clouderaV"
    val hbaseV = s"1.0.0-$clouderaV"
    val scalaTestV = "2.2.5"
    val scalaLoggingV = "3.1.0"
    val akkaV = "2.3.12"
    val akkaHttpV = "1.0"
    val scalamockV = "3.2"
    val macwireV = "2.2.2"
    val configV = "1.3.0"

    val eventsModelV = "0.0.1"

    Seq(
        "org.apache.hbase" % "hbase" % hbaseV,
        "org.apache.hbase" % "hbase-client" % hbaseV,
        "org.apache.hbase" % "hbase-server" % hbaseV excludeAll ExclusionRule(organization = "org.mortbay.jetty"),
        "org.apache.hbase" % "hbase-common" % hbaseV,

        // TODO not required for running in cluster
        "org.apache.zookeeper" % "zookeeper" % zooV,
        "org.apache.hbase" % "hbase-protocol" % hbaseV,
        "org.apache.hbase" % "hbase-hadoop-compat" % hbaseV,
        "org.apache.htrace" % "htrace-core" % "3.1.0-incubating",
        "com.google.guava" % "guava" % "12.0.1",

        "org.apache.hadoop" % "hadoop-common" % hadoopV excludeAll ExclusionRule(organization = "javax.servlet"),
        "org.apache.hadoop" % "hadoop-client" % hadoopV excludeAll ExclusionRule(organization = "javax.servlet") exclude("com.google.guava", "guava"),

        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
        "com.typesafe.akka" %% "akka-slf4j" % akkaV,
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpV,

        "org.jodd" % "jodd-lagarto" % joddV,
        "org.jodd" % "jodd-core" % joddV,
        "org.jodd" % "jodd-log" % joddV,

        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,
        "com.typesafe" % "config" % configV,

        "com.softwaremill.macwire" %% "macros" % macwireV % "provided",
        "com.softwaremill.macwire" %% "util" % macwireV,
        "com.softwaremill.macwire" %% "proxy" % macwireV,

        "org.scalatest" %% "scalatest" % scalaTestV % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % scalamockV % "test",

        "vitalcode" %% "events-model" % eventsModelV,

        //"org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.53.0" exclude("com.google.guava", "guava")
        //"com.machinepublishers" % "jbrowserdriver" % "0.14.7" exclude("com.google.guava", "guava"),
        "com.github.detro.ghostdriver" % "phantomjsdriver" % "1.1.0"

    )
}

enablePlugins(JavaAppPackaging)

assemblyMergeStrategy in assembly := {
    case PathList("reference.conf") => MergeStrategy.concat
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case "META-INF/jersey-module-version" => MergeStrategy.first
    case _ => MergeStrategy.first
}

parallelExecution in Test := false
assemblyJarName in assembly := "crawler.jar"
mainClass in assembly := Some("uk.vitalcode.events.crawler.Client")

dockerCommands ++= Seq(
    ExecCmd("RUN", "wget", "http://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2"
    ),
    ExecCmd("RUN", "tar", "jxvf", "phantomjs-2.1.1-linux-x86_64.tar.bz2")
)
