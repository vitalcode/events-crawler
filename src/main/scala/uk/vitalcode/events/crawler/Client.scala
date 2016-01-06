package uk.vitalcode.events.crawler


import akka.actor.{ActorSystem, Props}
import uk.vitalcode.events.crawler.model._

trait UserModule {

    import com.softwaremill.macwire._

    lazy val httpClient: HttpClient = wire[TestHttpClient]
    lazy val hBaseService: HBaseService = wire[TestHBaseService]
    lazy val requester: Requester = wire[Requester]
}

object Client extends UserModule {

//    override lazy val httpClient: HttpClient = new TestHttpClient()


    def main(args: Array[String]) {


        //        val conf: Configuration = HBaseConfiguration.create()
        //        conf.clear()
        //        conf.set("hbase.zookeeper.quorum", "robot1.vk,robot2.vk,robot3.vk")
        //
        //        val connection: Connection = ConnectionFactory.createConnection(conf)

        // todo refactor connction into other service that DefaultHBaseService will depend on
        //val connection: Connection = null

        runScrawler()
    }


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

        // val requester = system.actorOf(Props(new Requester(connection, new HttpClient(), new DefaultHBaseService())))
        val requesterRef = system.actorOf(Props(requester))
        val manager = system.actorOf(Props(new Manager(requesterRef, page)))

        manager ! 1

        // TODO close on finish
        // connection.close()
    }

}
