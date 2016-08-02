package uk.vitalcode.events.crawler.services

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Source
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.ByteString
import org.apache.commons.io.IOUtils
import org.apache.hadoop.hbase.util.Bytes
import org.openqa.selenium.Dimension
import org.openqa.selenium.phantomjs.{PhantomJSDriver, PhantomJSDriverService}
import org.openqa.selenium.remote.DesiredCapabilities
import uk.vitalcode.events.crawler.common.AppConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._

trait HttpClient {
    def makeRequest(url: String, phantom: Boolean): Future[Array[Byte]]
}

class DefaultHttpClient(system: ActorSystem) extends HttpClient {

    implicit val materializer = ActorMaterializer.create(system)

    override def makeRequest(url: String, phantom: Boolean): Future[Array[Byte]] = {
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

    private def getImage(url: String): Future[Array[Byte]] =
        Http(system).singleRequest(buildHttpRequest(url))
            .map(response => {

                response.entity.toStrict(60.second)
                    .map(b => {
                        val d: ByteString =  ByteString. b.getData()
                    }

                    )




//                val transformedData: Future[ExamplePerson] =
//                    strictEntity flatMap { e =>
//                        e.dataBytes
//                            .runFold(ByteString.empty) { case (acc, b) => acc ++ b }
//                            .map(parse)
//                    }


                val inputStream = r.entity.dataBytes.runWith(
                    StreamConverters.asInputStream(FiniteDuration(AppConfig.httpClientTimeout, TimeUnit.SECONDS))
                )
                IOUtils.toByteArray(inputStream)
            })

    private def getWebPage(url: String): Future[Array[Byte]] = {
        val p = Promise[Array[Byte]]()
        Future {
            try {
                val driver = createPhantomDriver()
                driver.get(url)
                val page = driver.getPageSource
                driver.quit()
                p.success(Bytes.toBytes(page))
            } catch {
                case e: Exception =>
                    p.failure(e)
            }
        }
        p.future
    }
}


