package uk.vitalcode.events.crawler.actormodel

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import jodd.jerry.Jerry._
import jodd.jerry.{Jerry, JerryNodeFunction}
import jodd.lagarto.dom.Node
import uk.vitalcode.events.crawler.common.AppModule
import uk.vitalcode.events.crawler.model.Page
import uk.vitalcode.events.crawler.services.{HBaseService, HttpClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class FetchPage(page: Page)

case class PagesToFetch(pages: Set[Page])

trait RequesterModule {
    this: AppModule =>

    lazy val requesterRef: ActorRef = system.actorOf(Props(wire[Requester]))

    class Requester(httpClient: HttpClient, hBaseService: HBaseService) extends Actor
    with ImplicitMaterializer with ActorLogging {

        def receive = {
            case FetchPage(page) =>
                log.info(s"Fetching page [${page.id}] ...")

                val send = sender
                val timeout = 3000.millis
                require(page.url != null)
                log.info(logMessage(s"Fetching data from url [${page.url}]", page))

                httpClient.makeRequest(page.url).map(response => response.status match {
                    case OK =>
                        response.entity.toStrict(timeout).map(entity => {
                            // persist page
                            val pageBody = entity.data.utf8String
                            val dom: Jerry = jerry(pageBody)
                            log.info(logMessage(s"Saving fetched data to the database", page))
                            hBaseService.saveData(page.url, pageBody)

                            // get child pages or child of the parent if ref is specified
                            var childPages = Set.empty[Page]
                            val pages = if (page.ref == null) {
                                log.info(logMessage(s"Got [${page.pages.size}] child pages of the current page", page))
                                page.pages
                            } else {
                                val parentPage = getParent(page, page.ref)
                                log.info(logMessage(s"Got[${parentPage.pages.size}] child pages of the parent ${parentPage}", page))
                                parentPage.pages
                            }

                            pages.foreach(childPage => {
                                log.info(logMessage(s"Found child css link [${childPage.link}]", page))

                                val childLink = dom.$(childPage.link)

                                childLink.each(new JerryNodeFunction {
                                    override def onNode(node: Node, index: Int): Boolean = {
                                        val baseUri = new URI(page.url)
                                        val childLinkUrl = node.getAttribute("href")
                                        val childImageUrl = node.getAttribute("src")
                                        val childUri = if (childLinkUrl != null) new URI(childLinkUrl) else new URI(childImageUrl)
                                        val resolvedUri = baseUri.resolve(childUri).toString
                                        val newChildPage = Page(childPage.id, childPage.ref, resolvedUri, childPage.link, childPage.props, childPage.pages, childPage.parent, childPage.isRow)
                                        log.info(logMessage(s"Adding child page [$newChildPage]", page))
                                        childPages += newChildPage
                                        true
                                    }
                                })
                            })
                            if (childPages.nonEmpty) {
                                log.info(logMessage(s"Sending fetching request for ${childPages.size} child pages", page))
                                send ! PagesToFetch(childPages)
                            }
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

        def logMessage(message: String, page: Page): String = {
            s"[${page.id}] $message ([$page])"
        }
    }

}
