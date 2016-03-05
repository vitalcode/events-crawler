package uk.vitalcode.events.crawler.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.stream.ActorMaterializer
import uk.vitalcode.events.crawler.common.AppConfig

import scala.concurrent.Future

trait HttpClient {
    def makeRequest(url: String): Future[HttpResponse]
}

class DefaultHttpClient(system: ActorSystem) extends HttpClient {

    implicit val materializer = ActorMaterializer.create(system)

    override def makeRequest(url: String): Future[HttpResponse] = {
        Http(system).singleRequest(buildHttpRequest(url))
    }

    private def buildHttpRequest(url: String): HttpRequest = {
        val acceptEncoding = headers.`Accept-Encoding`(
            List(HttpEncodingRange(HttpEncodings.gzip), HttpEncodingRange(HttpEncodings.deflate)))
        val accept = headers.Accept(
            List(MediaRange(MediaTypes.`text/html`), MediaRange(MediaTypes.`application/xml`),
                MediaRange(MediaTypes.`application/xhtml+xml`), MediaRange(MediaTypes.`image/webp`)))
        val userAgent = headers.`User-Agent`(AppConfig.userAgent)

        HttpRequest(
            uri = url,
            method = HttpMethods.GET,
            headers = List(acceptEncoding, accept, userAgent),
            protocol = HttpProtocols.`HTTP/1.0`)
    }
}

