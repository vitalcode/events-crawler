name := """events-fetcher"""
organization := "Vitaliy Kuznetsov & Lesia Mirchenko"
version := "0.0.1"
scalaVersion := "2.10.6"

resolvers ++= Seq(
    "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/",
    "Cloudera" at "https://repository.cloudera.com/artifactory/public/"
)

libraryDependencies ++= {
    val joddV = "3.6.7"
    val clouderaV = "cdh5.4.7"
    val zooV = s"3.4.5-$clouderaV"
    val hadoopV = s"2.6.0-$clouderaV"
    val hbaseV = s"1.0.0-$clouderaV"


    Seq(
        "org.apache.hbase" % "hbase" % hbaseV,
        "org.apache.hbase" % "hbase-client" % hbaseV,
        "org.apache.hbase" % "hbase-server" % hbaseV excludeAll ExclusionRule(organization = "org.mortbay.jetty"),
        "org.apache.hbase" % "hbase-common" % hbaseV,

        "org.apache.hadoop" % "hadoop-common" % hadoopV excludeAll ExclusionRule(organization = "javax.servlet"),
        "org.apache.hadoop" % "hadoop-client" % hadoopV excludeAll ExclusionRule(organization = "javax.servlet") exclude("com.google.guava", "guava"),

        "com.typesafe.akka" %% "akka-actor" % "2.3.12",
        "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
        "com.typesafe.akka" %% "akka-http-experimental" % "1.0",

        "org.jodd" % "jodd-lagarto" % joddV,
        "org.jodd" % "jodd-core" % joddV,
        "org.jodd" % "jodd-log" % joddV
    )
}
