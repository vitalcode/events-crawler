package uk.vitalcode.events.crawler.actormodel

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import jodd.jerry.Jerry
import jodd.jerry.Jerry._
import uk.vitalcode.events.crawler.UserModule
import uk.vitalcode.events.crawler.model.Page
import uk.vitalcode.events.crawler.services.{HBaseService, HttpClient}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class FetchPage(page: Page)

case class PagesToFetch(pages: Set[Page])

trait RequesterModule {
    this: UserModule =>

    lazy val requesterRef: ActorRef = system.actorOf(Props(wire[Requester]))

    class Requester(httpClient: HttpClient, hBaseService: HBaseService) extends Actor
    with ImplicitMaterializer with ActorLogging {

        def receive = {
            case FetchPage(page) =>
                val send = sender
                val timeout = 3000.millis
                log.info(s"Fetching page: ${page.id} ...")

                require(page.url != null)
                httpClient.makeRequest(page.url).map(response => response.status match {
                    case OK =>
                        response.entity.toStrict(timeout).map(entity => {
                            // persist page
                            val pageBody = entity.data.utf8String
                            val dom: Jerry = jerry(pageBody)
                            hBaseService.saveData(page.url, pageBody)

                            // get child pages with urls
                            var childPages = Set.empty[Page]

                            page.pages.foreach(childPage => {
                                log.info(s"child css:${childPage.link}")

                                val childUrl = dom.$(childPage.link).attr("href")
                                log.info(s"child url:$childUrl")

                                val newChildPage = Page(childPage.id, childUrl, childPage.link, childPage.props, childPage.pages, childPage.isRow)
                                childPages += newChildPage
                            })

                            send ! PagesToFetch(childPages)
                        })
                    case _ =>
                        send ! false
                })
            case msg: Any =>
                log.warning(s"Message not delivered: $msg")
                sender ! false
        }
    }

}
