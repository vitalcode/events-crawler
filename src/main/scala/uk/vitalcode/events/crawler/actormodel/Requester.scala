package uk.vitalcode.events.crawler.actormodel

import java.net.URI
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import jodd.jerry.Jerry._
import jodd.jerry.{Jerry, JerryNodeFunction}
import jodd.lagarto.dom.Node
import org.openqa.selenium.Capabilities
import org.openqa.selenium.phantomjs.{PhantomJSDriver, PhantomJSDriverService}
import org.openqa.selenium.remote.DesiredCapabilities
import uk.vitalcode.events.crawler.common.AppModule
import uk.vitalcode.events.crawler.services.{HBaseService, HttpClient}
import uk.vitalcode.events.model.Page

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

//import org.openqa.selenium.firefox.FirefoxDriver

case class FetchPage(page: Page, indexId: String)

case class PagesToFetch(pages: Set[Page], indexId: String)

trait RequesterModule {
    this: AppModule =>

    lazy val requesterRef: ActorRef = system.actorOf(Props(wire[Requester]))

    class Requester(httpClient: HttpClient, hBaseService: HBaseService) extends Actor
        with ImplicitMaterializer with ActorLogging {

        def receive = {
            case FetchPage(page, id) =>
                try {
                    log.info(s"Fetching page [${page.id}] ...")

                    var indexId = id
                    val send = sender
                    val timeout = 3000.millis
                    require(page.url != null)
                    log.info(logMessage(s"Fetching data from url [${page.url}]", page))

                    // http://stackoverflow.com/questions/24365154/web-crawling-ajax-javascript-enabled-pages-using-java

                    val caps = new DesiredCapabilities()
                    //caps.setJavascriptEnabled(true); //< not really needed: JS enabled by default
                    //caps.setCapability("takesScreenshot", true); //< yeah, GhostDriver haz screenshotz!
                    caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                        "lib/phantomjs"
                    )

                    val driver = new PhantomJSDriver(caps)
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    try {
                        log.info(logMessage(s"Got response from url [${page.url}]", page))
                        driver.get(page.url)
                        val pageBody = driver.getPageSource()
                        driver.quit()

                        if (page.isRow) {
                            indexId = page.url
                        }

                        val dom: Jerry = jerry(pageBody)
                        log.info(logMessage(s"Saving fetched data to the database", page))
                        if (indexId != null) {
                            hBaseService.saveData(page, pageBody, indexId)
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
