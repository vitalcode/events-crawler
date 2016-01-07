package uk.vitalcode.events.crawler.test

import java.io.InputStream

import akka.actor.{Actor, IndirectActorProducer, ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}
import uk.vitalcode.events.crawler._
import uk.vitalcode.events.crawler.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ClientTest extends TestKit(ActorSystem("ClientTest", ConfigFactory.parseString(ClientTest.config)))
with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll
with MockFactory
with UserModule {

    override lazy val httpClient: HttpClient = mock[HttpClient]

    "Crawling apache tika web site" should {

            (httpClient.makeRequest _)
                .expects("https://tika.apache.org/download.html")
                .returns(getPage("/pageA.html"))

            (httpClient.makeRequest _)
                .expects("http://archive.apache.org/dist/incubator/tika/")
                .returns(getPage("/pageB.html"))

            val page: Page = PageBuilder()
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

        //DI(system).ctx = () => requesterFactory

        val requesterRef = system.actorOf(Props(classOf[Requester], httpClient, hBaseService))
        val manager = system.actorOf(Props(classOf[Manager], requesterRef, page, () => requesterFactory))

        "get two pages" in {
            within(500.millis) {

                manager ! 1

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


object ClientTest extends UserModule {

    val config =
        """
    akka {
      loglevel = "WARNING"
    }
    akka.loggers = ["akka.testkit.TestEventListener"]
        """

}
