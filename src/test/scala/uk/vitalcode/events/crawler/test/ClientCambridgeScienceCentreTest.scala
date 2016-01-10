package uk.vitalcode.events.crawler.test

import java.io.InputStream

import akka.actor._
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}
import uk.vitalcode.events.crawler._
import uk.vitalcode.events.crawler.actormodel.{ManagerModule, RequesterModule}
import uk.vitalcode.events.crawler.model._
import uk.vitalcode.events.crawler.services.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ClientCambridgeScienceCentreTest(actorSystem: ActorSystem) extends TestKit(actorSystem)
with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll
with MockFactory {

    def this() = this(ClientCambridgeScienceCentreTest.actorSystem)

    val httpClientMock: HttpClient = mock[HttpClient]

    "Client crawling Cambridge science centre web site" should {

        // page 1 list
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/list")
            .returns(getPage("/clientCambridgeScienceCentreTest/list1.html"))

        // page 1 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/destination-space-crew-09012016-1500/")
            .returns(getPage("/clientCambridgeScienceCentreTest/destination-space-crew-09012016-1500.html"))

        // page 1 link 2
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/Voyagetospace_09012016_1600/")
            .returns(getPage("/clientCambridgeScienceCentreTest/voyagetospace_09012016_1600.html"))

        // page 2 list
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/list/?page=2")
            .returns(getPage("/clientCambridgeScienceCentreTest/list2.html"))

        // page 2 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/otherworlds/")
            .returns(getPage("/clientCambridgeScienceCentreTest/otherworlds.html"))

        // page 2 link 2
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events/sunday-science-20-march/")
            .returns(getPage("/clientCambridgeScienceCentreTest/sunday-science-20-march.html"))

        val managerModule = new UserModule with ManagerModule with RequesterModule {
            override lazy val system = actorSystem


            val pageBuilderList = PageBuilder()
                .setId("list")
                .setUrl("http://www.cambridgesciencecentre.org/whats-on/list")
                .addPage(PageBuilder()
                    .setId("description")
                    .setLink("div.main_wrapper > section > article > ul > li > h2 > a")
                )

            val page2 = PageBuilder()
                .setId("pagination")
                .setLink("div.pagination > div.omega > a")
                .addPage(pageBuilderList.build().pages.head) // todo FIX THIS
                .build()

            pageBuilderList.addPage(page2)

            override lazy val page: Page = pageBuilderList.build()


            //override lazy val page: Page = pageA

            //            override lazy val page: Page = PageBuilder()
            //                .setId("list")
            //                .setUrl("http://www.cambridgesciencecentre.org/whats-on/list")
            //                .addPage(PageBuilder()
            //                    .setId("description")
            //                    .setLink("div.main_wrapper > section > article > ul > li > h2 > a")
            //                )
            //                .addPage(PageBuilder()
            //                    .setRef("list")
            //                    .setLink("div.pagination > div.omega > a")
            //                    .addPage()
            //                )
            //                .build()

            override lazy val httpClient: HttpClient = httpClientMock
        }

        val managerRef = managerModule.managerRef

        "must fetch data from 6 web pages (2 page lists each with 2 links)" in {

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
        akka {
        loglevel = "WARNING"
        }
        """)

    val actorSystem: ActorSystem = ActorSystem("ClientCambridgeScienceCentreTest", config)
}