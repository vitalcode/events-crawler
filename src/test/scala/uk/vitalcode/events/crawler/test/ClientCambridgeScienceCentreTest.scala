package uk.vitalcode.events.crawler.test

import java.io.InputStream

import akka.actor._
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.testkit._
import com.softwaremill.macwire._
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}
import uk.vitalcode.events.crawler._
import uk.vitalcode.events.crawler.actormodel.{ManagerModule, RequesterModule}
import uk.vitalcode.events.crawler.model._
import uk.vitalcode.events.crawler.services.{TestHBaseService, HBaseService, TestHttpClient, HttpClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ClientCambridgeScienceCentreTest(actorSystem: ActorSystem) extends TestKit(actorSystem)
with DefaultTimeout with ImplicitSender with WordSpecLike
with Matchers with BeforeAndAfterAll with MockFactory {

    def this() = this(ClientCambridgeScienceCentreTest.actorSystem)

    "Client crawling Cambridge science centre web site" should {

        val httpClientMock: HttpClient = mock[HttpClient]

        // page 1 list
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/list")
            .returns(getPage("/clientCambridgeScienceCentreTest/list1.html"))
            .once()

        // page 1 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/destination-space-crew-09012016-1500/")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.html"))
            .once()

        // page 1 link 1 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/assets/3a/969c39e09b655c715be0aa6b578908427d75e7.jpg")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.jpg"))
            .once()

        // page 1 link 2
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/Voyagetospace_09012016_1600/")
            .returns(getPage("/clientCambridgeScienceCentreTest/voyagetospace_09012016_1600.html"))
            .once()

        // page 1 link 2 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/assets/3a/0004a8c035b90924f8321df21276fc8f83a6cd.jpg")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.jpg"))
            .once()

        // page 2 list
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/list/?page=2")
            .returns(getPage("/clientCambridgeScienceCentreTest/list2.html"))
            .once()

        // page 2 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/otherworlds/")
            .returns(getPage("/clientCambridgeScienceCentreTest/otherworlds.html"))
            .once()

        // page 2 link 1 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/assets/3a/37cf8f84e5cfa94cdcac3f73bc13cfea3556a7.jpg")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.jpg"))
            .once()

        // page 2 link 2
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/sunday-science-20-march/")
            .returns(getPage("/clientCambridgeScienceCentreTest/sunday-science-20-march.html"))
            .once()

        // page 2 link 2 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/assets/3a/200e303cecd9eee71f77c97ddea630521cbfe9.png")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.jpg"))
            .once()

        // page 3 list
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/list/?page=3")
            .returns(getPage("/clientCambridgeScienceCentreTest/list3.html"))
            .once()

        // page 3 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/february-half-term-2016/")
            .returns(getPage("/clientCambridgeScienceCentreTest/february-half-term-2016.html"))
            .once()

        // page 3 link 1 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/assets/3a/d78141bc0cc3f96d175843c2cd0e97beb9c370.jpg")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.jpg"))
            .once()

        // page 3 link 2
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/electric-universe/")
            .returns(getPage("/clientCambridgeScienceCentreTest/electric-universe.html"))
            .once()

        // page 3 link 2 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/assets/3a/fb2024b1db936348b42d3edd48995c32f69a1d.jpg")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.jpg"))
            .once()

        val managerModule = new AppModule with ManagerModule with RequesterModule {
            override lazy val system = actorSystem

            override lazy val page: Page = PageBuilder()
                .setId("list")
                .setUrl("http://www.cambridgesciencecentre.org/whats-on/list")
                .addPage(PageBuilder()
                    .setId("description")
                    .setLink("div.main_wrapper > section > article > ul > li > h2 > a")
                    .addPage(PageBuilder()
                        .setId("image")
                        .setLink("section.event_detail > div.page_content > article > img")
                    )
                )
                .addPage(PageBuilder()
                    .setRef("list")
                    .setId("pagination")
                    .setLink("div.pagination > div.omega > a")
                )
                .build()

            override lazy val httpClient: HttpClient = httpClientMock

            override lazy val hBaseService: HBaseService = wire[TestHBaseService]
        }

        val managerRef = managerModule.managerRef

        "should fetch data from 9 web pages (3 page lists each with 2 links)" in {
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

object ClientCambridgeScienceCentreTest {

    val config = ConfigFactory.parseString(
        """
    akka{
        loggers = ["akka.event.slf4j.Slf4jLogger"]
        loglevel = "DEBUG"
        logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
    }
        """)

    val actorSystem: ActorSystem = ActorSystem("ClientCambridgeScienceCentreTest", config)
}