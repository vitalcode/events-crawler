package uk.vitalcode.events.crawler.actormodel

import java.io.{BufferedOutputStream, FileOutputStream, InputStream, OutputStream}
import java.net.URI
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.softwaremill.macwire._
import jodd.jerry.Jerry._
import jodd.jerry.{Jerry, JerryNodeFunction}
import jodd.lagarto.dom.Node
import org.openqa.selenium.{Capabilities, Dimension}
import org.openqa.selenium.phantomjs.{PhantomJSDriver, PhantomJSDriverService}
import org.openqa.selenium.remote.DesiredCapabilities
import uk.vitalcode.events.crawler.common.AppModule
import uk.vitalcode.events.crawler.services.{HBaseService, HttpClient}
import uk.vitalcode.events.model.{Page, PropType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent._
import akka._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.util._
import org.apache.commons.io.IOUtils

//import org.openqa.selenium.firefox.FirefoxDriver

case class FetchPage(page: Page, indexId: String)

case class PagesToFetch(pages: Set[Page], indexId: String)

trait RequesterModule {
    this: AppModule =>

    lazy val requesterRef: ActorRef = system.actorOf(Props(wire[Requester]))

    class Requester(httpClient: HttpClient, hBaseService: HBaseService) extends Actor with ActorLogging {

        final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

        def receive = {
            case FetchPage(page, id) =>
                try {
                    log.info(s"Fetching page [${page.id}] ...")

                    var indexId = id
                    val send = sender
                    val timeout = 3000.millis
                    require(page.url != null)
                    log.info(logMessage(s"Fetching data from url [${page.url}]", page))


                    httpClient.makeRequest(page.url, !page.props.exists(prop => prop.kind == PropType.Image)).onComplete {
                        case Success(pageBodyStream: Source[ByteString, Any]) => {

                            val inputStream = pageBodyStream.runWith(
                                StreamConverters.asInputStream(FiniteDuration(5, TimeUnit.MINUTES)) // Try catch
                            )
                            val bytes: Array[Byte] = IOUtils.toByteArray(inputStream)
                            val pageBody: String = new String(bytes, "UTF-8")

                            if (page.isRow) {
                                indexId = page.url
                            }

                            val dom: Jerry = jerry(pageBody)
                            log.info(logMessage(s"Saving fetched data to the database", page))
                            if (indexId != null) {
                                hBaseService.saveData(page, bytes, indexId)
                            }

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
                                send ! PagesToFetch(childPages, indexId)
                            }
                            else send ! false


                        }
                        case Failure(ex) => {
                            log.warning(s"This grinder needs a replacement, seriously! $ex")
                            send ! false
                        }
                    }

                } catch {
                    case e: Exception =>
                        log.warning(s"Exception: $e")
                        sender ! false
                }
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
