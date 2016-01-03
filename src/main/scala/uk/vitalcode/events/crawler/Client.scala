package uk.vitalcode.events.crawler

import akka.actor.{ActorSystem, Props}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import uk.vitalcode.events.crawler.model._

object Client {

    def main(args: Array[String]) {

//        val conf: Configuration = HBaseConfiguration.create()
//        conf.clear()
//        conf.set("hbase.zookeeper.quorum", "robot1.vk,robot2.vk,robot3.vk")
//
//        val connection: Connection = ConnectionFactory.createConnection(conf)

        val connection: Connection = null

        runScrawler()

        def runScrawler(): Unit = {

            val system = ActorSystem("ScrawlerSystem")


            val page: Page = PageBuilder()
                .setId("pageA")
                .setUrl("https://tika.apache.org/download.html")
                .addProp(PropBuilder()
                    .setName("Title")
                    .setCss("p.title")
                    .setKind(PropType.Text)
                )
                .addPage(PageBuilder()
                    .setId("PageB")
                    .setLink(".section p > a:nth-child(2)") // .section p:nth-child(6) > a
                )
                .build()

            val requester = system.actorOf(Props(new Requester(connection) with DefaultHBaseService))
            val manager = system.actorOf(Props(new Manager(requester, page)))

            manager ! 1

            // TODO close on finish
            // connection.close()
        }
    }
}
