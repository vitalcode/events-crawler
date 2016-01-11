package uk.vitalcode.events.crawler

import akka.actor._
import com.softwaremill.macwire._
import uk.vitalcode.events.crawler.actormodel.{RequesterModule, ManagerModule}
import uk.vitalcode.events.crawler.model._
import uk.vitalcode.events.crawler.services.{TestHBaseService, HBaseService, TestHttpClient, HttpClient}


trait AppModule {

    def page: Page
    def system: ActorSystem

    lazy val httpClient: HttpClient = wire[TestHttpClient]
    lazy val hBaseService: HBaseService = wire[TestHBaseService]
}

object Client {

    def main(args: Array[String]) {
        runScrawler()
    }

    def runScrawler(): Unit = {

        val managerModule = new AppModule with ManagerModule with RequesterModule {
            override lazy val system = ActorSystem("ScrawlerSystem")
            override lazy val page: Page = PageBuilder()
                .setId("pageA")
                .setUrl("https://tika.apache.org/download.html")
                .addProp(PropBuilder()
                    .setName("Title")
                    .setCss("p.title")
                    .setKind(PropType.Text)
                )
                .addPage(PageBuilder()
                    .setId("PageB")
                    .setLink(".section p > a:nth-child(2)")
                )
                .build()
        }

        managerModule.managerRef ! 1

        // TODO close on finish
        // connection.close()
    }

}
