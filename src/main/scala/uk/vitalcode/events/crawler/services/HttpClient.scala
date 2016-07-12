package uk.vitalcode.events.crawler.services

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.openqa.selenium.Dimension
import org.openqa.selenium.phantomjs.{PhantomJSDriver, PhantomJSDriverService}
import org.openqa.selenium.remote.DesiredCapabilities
import uk.vitalcode.events.crawler.common.AppConfig
import uk.vitalcode.events.model.{Page, PropType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

trait HttpClient {
    def makeRequest(url: String, phantom: Boolean): Future[Source[ByteString, Any]]
}

class DefaultHttpClient(system: ActorSystem) extends HttpClient {

    implicit val materializer = ActorMaterializer.create(system)

    private def createPhantomDriver() = {

        val caps = new DesiredCapabilities()
        caps.setJavascriptEnabled(true) // TODO may be removed
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
            "/opt/phantomjs-2.1.1-linux-x86_64/bin/phantomjs"
        )

        val driver = new PhantomJSDriver(caps)
        driver.manage().window().setSize(new Dimension(1920, 1080))
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS) // TODO may be removed
        driver
    }

    private def buildHttpRequest(url: String): HttpRequest = {
        val acceptEncoding = headers.`Accept-Encoding`(
            List(HttpEncodingRange(HttpEncodings gzip), HttpEncodingRange(HttpEncodings.deflate)))
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

    private def getImage(url: String): Future[Source[ByteString, Any]] =
        Http(system)
            .singleRequest(buildHttpRequest(url))
            .map(r => r.entity.dataBytes)

    private def getWebPage(url: String): Future[Source[ByteString, Any]] = {
        val p = Promise[Source[ByteString, Any]]()
        // From: http://stackoverflow.com/questions/25094568/best-practices-with-akka-in-scala-and-third-party-java-libraries
        // Other: http://stackoverflow.com/questions/31226569/how-to-wrap-blocking-io-in-scala-as-non-blocking
        Future {
            try {
                val driver = createPhantomDriver()
                driver.get(url)
                val pageBody = driver.getPageSource
                driver.quit()
                p.success(Source.single(ByteString(pageBody)))
            } catch {
                case e: Exception =>
                    p.failure(e)
            }
        }
        p.future
    }

    override def makeRequest(url: String, phantom: Boolean): Future[Source[ByteString, Any]] = {
        if (!phantom) getImage(url) else getWebPage(url)
    }
}


