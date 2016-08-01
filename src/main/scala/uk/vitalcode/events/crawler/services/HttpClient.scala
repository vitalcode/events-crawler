package uk.vitalcode.events.crawler.services

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import org.openqa.selenium.Dimension
import org.openqa.selenium.phantomjs.{PhantomJSDriver, PhantomJSDriverService}
import org.openqa.selenium.remote.DesiredCapabilities
import uk.vitalcode.events.crawler.common.AppConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

trait HttpClient {
    def makeRequest(url: String, phantom: Boolean): Future[Source[ByteString, Any]]
}

class DefaultHttpClient(system: ActorSystem) extends HttpClient {

    implicit val materializer = ActorMaterializer.create(system)

    override def makeRequest(url: String, phantom: Boolean): Future[Source[ByteString, Any]] = {
        if (phantom) getWebPage(url) else getImage(url)
    }

    private def createPhantomDriver(): PhantomJSDriver = {
        val caps = new DesiredCapabilities()
        caps.setJavascriptEnabled(true)
        caps.setCapability("phantomjs.page.settings.takesScreenshot", false)
        caps.setCapability("phantomjs.page.settings.loadImages", false)
        caps.setCapability("phantomjs.page.settings.userAgent", AppConfig.httpClientUserAgent)
        caps.setCapability("phantomjs.page.settings.resourceTimeout", AppConfig.httpClientTimeout)
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, AppConfig.httpClientPhantomPath)

        val driver = new PhantomJSDriver(caps)
        driver.manage().window().setSize(new Dimension(AppConfig.httpClientWindowWidth, AppConfig.httpClientWindowHeight))
        driver.manage().timeouts().setScriptTimeout(AppConfig.httpClientTimeout, TimeUnit.MILLISECONDS)
        driver.manage().timeouts().pageLoadTimeout(AppConfig.httpClientTimeout, TimeUnit.MILLISECONDS)
        driver.manage().timeouts().implicitlyWait(AppConfig.httpClientTimeout, TimeUnit.MILLISECONDS)
        driver
    }

    private def buildHttpRequest(url: String): HttpRequest = {
        val acceptEncoding = headers.`Accept-Encoding`(
            List(HttpEncodingRange(HttpEncodings.gzip), HttpEncodingRange(HttpEncodings.deflate)))
        val accept = headers.Accept(
            List(MediaRange(MediaTypes.`text/html`), MediaRange(MediaTypes.`application/xml`),
                MediaRange(MediaTypes.`application/xhtml+xml`), MediaRange(MediaTypes.`image/webp`)))
        val userAgent = headers.`User-Agent`(AppConfig.httpClientUserAgent)

        HttpRequest(
            uri = url,
            method = HttpMethods.GET,
            headers = List(acceptEncoding, accept, userAgent),
            protocol = HttpProtocols.`HTTP/1.0`)
    }

    //    private def getImage(url: String): Future[Source[ByteString, Any]] =
    //    //        Http(system)
    //    //            .singleRequest(buildHttpRequest(url))
    //    //            .map(r => r.entity.dataBytes)
    //        Future {Source.empty[ByteString]}

    //    private def getImage(url: String): Future[Source[ByteString, Any]] =
    //        Source.single(buildHttpRequest(url))
    //                .map(r => r.entity.dataBytes)
    //                .runWith(Sink.head)


    private def getImage(url: String): Future[Source[ByteString, Any]] = {
        val connectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = Http().outgoingConnection(url)
        Source.single(buildHttpRequest(url))
            .via(connectionFlow)
            .runWith(Sink.head)
            .map(r => r.entity.dataBytes)
    }

    private def getWebPage(url: String): Future[Source[ByteString, Any]] = {
        val p = Promise[Source[ByteString, Any]]()
        Future {
            try {
                val driver = createPhantomDriver()
                driver.get(url)
                val page = driver.getPageSource
                driver.quit()
                p.success(Source.single(ByteString(page)))
            } catch {
                case e: Exception =>
                    p.failure(e)
            }
        }
        p.future
    }
}


