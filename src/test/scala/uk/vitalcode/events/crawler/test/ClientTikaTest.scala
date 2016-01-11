package uk.vitalcode.events.crawler.test

import java.io.InputStream

import akka.actor._
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}
import uk.vitalcode.events.crawler._
import uk.vitalcode.events.crawler.actormodel.{RequesterModule, ManagerModule}
import uk.vitalcode.events.crawler.model._
import uk.vitalcode.events.crawler.services.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ClientTikaTest extends TestKit(ActorSystem("ClientTikaTest", ConfigFactory.parseString(ClientTikaTest.config)))
with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll
with MockFactory {
    val httpClientMock: HttpClient = mock[HttpClient]

    "Client crawling apache tika web site" should {

        (httpClientMock.makeRequest _)
            .expects("https://tika.apache.org/download.html")
            .returns(getPage("/pageA.html"))

        (httpClientMock.makeRequest _)
            .expects("http://archive.apache.org/dist/incubator/tika/")
            .returns(getPage("/pageB.html"))

        val managerModule = new AppModule with ManagerModule with RequesterModule {
            // TODO get system from the test class constructor
            override lazy val system = ActorSystem("ClientTest")
            override lazy val page: Page = PageBuilder()
                .setId("pageA")
                .setUrl("https://tika.apache.org/download.html")
                .addProp(PropBuilder()
                    .setName("Title")
                    .setCss("p.title")
                    .setKind(PropType.Text)
                )
                .addPage(PageBuilder()
                    .setId("PageB")
                    .setLink(".section p > a:nth-child(2)")
                )
                .build()

            override lazy val httpClient: HttpClient = httpClientMock
        }

        val managerRef = managerModule.managerRef

        "must fetch data from two pages" in {

            within(500.millis) {
                managerRef ! 1
                expectNoMsg()
            }
        }
    }

    override def afterAll(): Unit = {
        shutdown()
    }

    private def getPage(fileUrl: String): Future[HttpResponse] = {
        Future {
            val stream: InputStream = getClass.getResourceAsStream(fileUrl)
            val b: Array[Byte] = Stream.continually(stream.read).takeWhile(_ != -1).map(_.toByte).toArray
            HttpResponse().withEntity(ContentTypes.`application/json`, b)
        }
    }
}

object ClientTikaTest {

    val config =
        """
    akka {
      loglevel = "WARNING"
    }
        """
}
