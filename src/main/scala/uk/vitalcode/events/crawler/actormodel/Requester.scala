package uk.vitalcode.events.crawler.actormodel

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props, _}
import akka.stream._
import com.softwaremill.macwire._
import jodd.jerry.Jerry._
import jodd.jerry.{Jerry, JerryNodeFunction}
import jodd.lagarto.dom.Node
import uk.vitalcode.events.crawler.common.AppModule
import uk.vitalcode.events.crawler.services.{HBaseService, HttpClient}
import uk.vitalcode.events.model.{Page, PropType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class FetchPage(page: Page, indexId: String)

case class PagesToFetch(pages: Set[Page], indexId: String)

trait RequesterModule {
    this: AppModule =>

    lazy val requesterRef: ActorRef = system.actorOf(Props(wire[Requester]))

    class Requester extends Actor with ActorLogging {

        final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

        override def postStop(): Unit = httpClient.dispose()

        def receive = {
            case FetchPage(page, indexId) =>
                val senderRef = sender
                val newIndexId = if (page.isRow) page.url else indexId
                fetchPage(page, newIndexId).onComplete {
                    case Success(nextPages) => {
                        if (nextPages.nonEmpty) {
                            log.info(logMessage(s"Sending fetching request for ${nextPages.size} child pages", page))
                            senderRef ! PagesToFetch(nextPages, newIndexId)
                        }
                        else senderRef ! false
                    }
                    case Failure(ex) => {
                        log.warning(logMessage(s"Error during page fetching: ${ex.getMessage} --> ${ex.getStackTrace}", page))
                        senderRef ! false
                    }
                }
            case msg: Any =>
                log.warning(s"Message not delivered: $msg")
                sender ! false
        }

        private def fetchPage(page: Page, indexId: String): Future[Set[Page]] = {
            log.info(logMessage(s"Fetching data from url [${page.url}]", page))

            httpClient.makeRequest(page.url, !page.props.exists(prop => prop.kind == PropType.Image)).map((pageBodyBytes: Array[Byte]) => {

                log.info(logMessage(s"Saving fetched data to the database", page))
                if (indexId != null) {
                    hBaseService.saveData(page, pageBodyBytes, indexId)
                }

                val childPages = if (page.ref == null) {
                    log.info(logMessage(s"Got [${page.pages.size}] child pages of the current page", page))
                    page.pages
                } else {
                    val parentPage = getParent(page, page.ref)
                    log.info(logMessage(s"Got[${parentPage.pages.size}] child pages of the parent $parentPage", page))
                    parentPage.pages
                }

                val dom: Jerry = jerry(new String(pageBodyBytes, "UTF-8"))

                childPages.flatMap(childPage => {
                    var nextPages = Set.empty[Page]
                    log.info(logMessage(s"Found child css link [${childPage.link}]", page))
                    dom.$(childPage.link).each(new JerryNodeFunction {
                        override def onNode(node: Node, index: Int): Boolean = {
                            val baseUri = new URI(page.url)
                            val childLinkUrl = node.getAttribute("href")
                            val childImageUrl = node.getAttribute("src")
                            val childStyle = node.getAttribute("style")
                            val childStyleUrl = if (childStyle != null) """(?<=url\(\')(.*)(?=\'\))""".r.findFirstIn(childStyle) else null
                            val childUri = if (childLinkUrl != null) new URI(childLinkUrl)
                            else if (childImageUrl != null) new URI(childImageUrl) else new URI(childStyleUrl.get)
                            val resolvedUri = baseUri.resolve(childUri).toString
                            val newChildPage = Page(childPage.id, childPage.ref, resolvedUri, childPage.link, childPage.props, childPage.pages, childPage.parent, childPage.isRow)
                            log.info(logMessage(s"Adding child page [$newChildPage]", page))
                            nextPages += newChildPage
                            true
                        }
                    })
                    nextPages
                })
            })
        }

        private def getParent(page: Page, ref: String): Page = if (page.id.equals(ref)) page else getParent(page.parent, ref)

        private def logMessage(message: String, page: Page): String = s"[${page.id}] $message ([$page])"
    }

}
