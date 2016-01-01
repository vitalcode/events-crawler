package uk.vitalcode.events.crawler

import akka.actor.{ActorSystem, Props}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}

object Client {

    def main(args: Array[String]) {

        val conf: Configuration = HBaseConfiguration.create()
        conf.clear()
        conf.set("hbase.zookeeper.quorum", "robot1.vk,robot2.vk,robot3.vk")

        val connection: Connection = ConnectionFactory.createConnection(conf)

        runScrawler()

        def runScrawler(): Unit = {

            val system = ActorSystem("ScrawlerSystem")

            val linkB = Link("linkB", null, ".section p > a:nth-child(2)", null)
            val linkA = Link("linkA", "https://tika.apache.org/download.html", null, Set(linkB))

            val requester = system.actorOf(Props(new Requester(connection)))
            val manager = system.actorOf(Props(new Manager(requester, linkA)))

            manager ! 1

            // TODO close on finish
            // connection.close()
        }
    }
}
