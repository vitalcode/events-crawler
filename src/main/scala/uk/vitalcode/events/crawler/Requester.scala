package uk.vitalcode.events.crawler

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.ByteString
import jodd.jerry.Jerry
import jodd.jerry.Jerry._
import uk.vitalcode.events.crawler.model.Page

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class Requester(httpClient: HttpClient, hBaseService: HBaseService) extends Actor with ImplicitMaterializer with ActorLogging {

    implicit val system = ActorSystem()

    def receive = {
        case pageObj1: Page =>

            val pageObj = pageObj1

            log.info(s"requester page:${pageObj.id}")

            require(pageObj.url != null)

            val send = sender

            val timeout = 3000.millis

            //            val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = pageObj.url))

            val responseFuture: Future[HttpResponse] = httpClient.makeRequest(pageObj.url)

            responseFuture.map(response => response.status match {
                case OK =>
                    val bs: Future[ByteString] = response.entity.toStrict(timeout).map {
                        _.data
                    }
                    val s: Future[String] = bs.map(_.utf8String)
                    s.map(pageHtml => {

                        //log.info(pageHtml)
                        hBaseService.saveData(pageObj.url, pageHtml)

                        val doc: Jerry = jerry(pageHtml)
                        val childPage: Page = if (pageObj.pages != null && pageObj.pages.nonEmpty) pageObj.pages.head else null

                        if (childPage != null) {
                            log.info(s"child css:${childPage.link}")

                            val childUrl = doc.$(childPage.link).attr("href")
                            log.info(s"child url:$childUrl")

                            val newChildPage = Page(childPage.id, childUrl, childPage.link, childPage.props, childPage.pages, childPage.isRow)
                            send ! newChildPage
                        }
                        else send ! false
                    })
                case _ =>
                    send ! false
            })
    }
}
