package uk.vitalcode.events.crawler.actormodel

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import jodd.jerry.{JerryNodeFunction, Jerry}
import jodd.jerry.Jerry._
import jodd.lagarto.dom.Node
import uk.vitalcode.events.crawler.UserModule
import uk.vitalcode.events.crawler.model.Page
import uk.vitalcode.events.crawler.services.{HBaseService, HttpClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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
//                val timeout = 3000.millis
                val timeout = 3000.hours
                log.info(s"Fetching page: ${page.id} ...")
                println(s"Fetching page: ${page.id} ...")

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
                            //var childPages = page.pages


                            val pages = if (page.ref == null) page.pages else getParent(page, page.ref).pages

                            pages.foreach(childPage => {
                                log.info(s"child css:${childPage.link}")
                                println(s"child css:${childPage.link}")

                                // TODO need to iterate through each css link
                                val childLink = dom.$(childPage.link)

                                childLink.each(new JerryNodeFunction {
                                    override def onNode(node: Node, index: Int): Boolean = {
                                        val childUrl = node.getAttribute("href")
                                        log.info(s"child url:$childUrl")
                                        println(s"child url:$childUrl")

                                        val newChildPage = Page(childPage.id, childPage.ref, childUrl, childPage.link, childPage.props, childPage.pages, childPage.parent, childPage.isRow)
                                        childPages += newChildPage
                                        true
                                    }
                                })
                            })

                            println(childPages)
                            send ! PagesToFetch(childPages)
                        })
                    case _ =>
                        send ! false
                })
            case msg: Any =>
                log.warning(s"Message not delivered: $msg")
                sender ! false
        }

        def getParent(page: Page, ref: String): Page = {
            if (page.id.equals(ref)) page else getParent(page.parent, ref)
        }
    }

}
