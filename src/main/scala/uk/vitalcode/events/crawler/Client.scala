package uk.vitalcode.events.crawler

import akka.actor._
import uk.vitalcode.events.crawler.actormodel.{ManagerModule, RequesterModule}
import uk.vitalcode.events.crawler.common.AppModule
import uk.vitalcode.events.crawler.model._

object Client {

    def main(args: Array[String]) {
        if (args.length > 0) {
            args(0) match {
                case "scrawl" =>
                    runScrawler()
                case _ => info()
            }
        } else {
            info()
        }
    }

    private def info(): Unit = {
        println("arguments: [scrawl]")
    }

    private def runScrawler(): Unit = {
        val managerModule = new AppModule with ManagerModule with RequesterModule {
            override lazy val system = ActorSystem("ScrawlerSystem")
            override lazy val page: Page = PageBuilder()
                .setId("list")
                .setUrl("http://www.cambridgesciencecentre.org/whats-on/list/")
                .addPage(PageBuilder()
                    .isRow(true)
                    .setId("description")
                    .setLink("div.main_wrapper > section > article > ul > li > h2 > a")
                    .addPage(PageBuilder()
                        .setId("image")
                        .setLink("section.event_detail > div.page_content > article > img")
                    )
                )
                .addPage(PageBuilder()
                    .setRef("list")
                    .setId("pagination")
                    .setLink("div.pagination > div.omega > a")
                )
                .build()
        }
        managerModule.managerRef ! 1
    }
}
