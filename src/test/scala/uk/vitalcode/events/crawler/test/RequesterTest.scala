package uk.vitalcode.events.crawler.test

import akka.actor.ActorSystem
import akka.testkit._
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers, WordSpecLike}
import uk.vitalcode.events.crawler.Manager
import uk.vitalcode.events.crawler.model.{Page, PageBuilder, PropBuilder, PropType}

class RequesterTest extends TestKit(ActorSystem(RequesterTest.actorSystem))
with ImplicitSender with WordSpecLike with BeforeAndAfterAll with ShouldMatchers {

    "When sending manager terminating message" must {

        "should terminate" in {

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


            val echo = TestActorRef(TestActors.echoActorProps)
            val manager = TestActorRef(new Manager(echo, page))

            manager ! true
            expectNoMsg()

            manager.underlyingActor.completed should equal(true)
        }
    }

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
}

object RequesterTest {
    val actorSystem = "RequesterTest"
}
