name := """events-fetcher"""
organization := "Vitaliy Kuznetsov & Lesia Mirchenko"
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
    val scalamock = "3.2"
    val macwireV = "2.2.2"

    Seq(
        "org.apache.hbase" % "hbase" % hbaseV,
        "org.apache.hbase" % "hbase-client" % hbaseV,
        "org.apache.hbase" % "hbase-server" % hbaseV excludeAll ExclusionRule(organization = "org.mortbay.jetty"),
        "org.apache.hbase" % "hbase-common" % hbaseV,

        "org.apache.hadoop" % "hadoop-common" % hadoopV excludeAll ExclusionRule(organization = "javax.servlet"),
        "org.apache.hadoop" % "hadoop-client" % hadoopV excludeAll ExclusionRule(organization = "javax.servlet") exclude("com.google.guava", "guava"),

        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpV,

        "org.jodd" % "jodd-lagarto" % joddV,
        "org.jodd" % "jodd-core" % joddV,
        "org.jodd" % "jodd-log" % joddV,

        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,

        "com.softwaremill.macwire" %% "macros" % macwireV % "provided",
        "com.softwaremill.macwire" %% "util" % macwireV,
        "com.softwaremill.macwire" %% "proxy" % macwireV,

        "org.scalatest" %% "scalatest" % scalaTestV % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % scalamock % "test"
    )
}
