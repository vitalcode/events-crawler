package uk.vitalcode.events.crawler


import akka.actor._
import com.softwaremill.macwire._
import uk.vitalcode.events.crawler.model._


//trait AppModule {
//
//}

trait UserModule {

    import com.softwaremill.macwire._

    lazy val httpClient: HttpClient = wire[TestHttpClient]
    lazy val hBaseService: HBaseService = wire[TestHBaseService]
    lazy val requester: Requester = wire[Requester]

    //def requesterFactory: Requester = wire[Requester]

    def requesterFactory: Requester = wire[Requester]

    //def produce(): Actor = requester
}


trait DIimp extends Extension {

    import com.softwaremill.macwire._

    lazy val httpClient: HttpClient = wire[TestHttpClient]
    lazy val hBaseService: HBaseService = wire[TestHBaseService]
    def requester: Requester = wire[Requester]

    //This is the operation this Extension provides
    //def ctx[T]: () => T = _
}

object DI extends ExtensionId[DIimp]
with ExtensionIdProvider {


    //The lookup method is required by ExtensionIdProvider,
    // so we return ourselves here, this allows us
    // to configure our extension to be loaded when
    // the ActorSystem starts up
    override def lookup = DI

    //This method will be called by Akka
    // to instantiate our Extension
    override def createExtension(system: ExtendedActorSystem) = new DIimp{}

    /**
      * Java API: retrieve the Count extension for the given system.
      */
    override def get(system: ActorSystem): DIimp = super.get(system)
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

    //    class DependencyInjector extends IndirectActorProducer {
    //
    //        override def produce(): Actor = requester
    //
    //        override def actorClass: Class[_ <: Actor] = classOf[Requester]
    //    }


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

        //DI with  (system) with  .ctx = () => requesterFactory //new Requester(new TestHttpClient(), new TestHBaseService())

        // val requester = system.actorOf(Props(new Requester(connection, new HttpClient(), new DefaultHBaseService())))
        val requesterRef = system.actorOf(Props(requester))
        val manager = system.actorOf(Props(classOf[Manager], requesterRef, page, () => requesterFactory))
        //val manager = system.actorOf(Props(new Manager(requesterRef, page, requesterFactory)))

        manager ! 1

        // TODO close on finish
        // connection.close()
    }

}
