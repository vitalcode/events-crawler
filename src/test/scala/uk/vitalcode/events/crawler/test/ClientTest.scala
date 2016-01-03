package uk.vitalcode.events.crawler.test

import org.scalatest.{BeforeAndAfterAll, FunSuite, ShouldMatchers}
import uk.vitalcode.events.crawler.model._

class ClientTest extends FunSuite with ShouldMatchers with BeforeAndAfterAll {

    test("try to build page object") {

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

        val result = 3
        result should equal(3)
    }

    override protected def beforeAll(): Unit = {
    }

    override protected def afterAll(): Unit = {
    }
}
