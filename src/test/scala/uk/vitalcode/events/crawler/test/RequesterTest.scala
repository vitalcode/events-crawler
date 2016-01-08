package uk.vitalcode.events.crawler.test

import akka.actor._
import akka.testkit._
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers, WordSpecLike}
import uk.vitalcode.events.crawler._
import uk.vitalcode.events.crawler.model.{Page, PageBuilder, PropBuilder, PropType}

// TODO add real requester tests
class RequesterTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with ShouldMatchers with BeforeAndAfterAll {

    def this() = this(ActorSystem("MySpec"))

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }

    "When sending manager terminating message" must {

        "must terminate fetching" in {

            val requesterTestRef: TestActorRef[Nothing] = TestActorRef(TestActors.echoActorProps)

            val managerModule = new UserModule with ManagerModule with RequesterModule {
                // TODO get system from the test class constructor
                override lazy val system = ActorSystem("MySpec")
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

                override lazy val requesterRef: ActorRef = requesterTestRef
            }

            val managerRef = TestActorRef(managerModule.manager)
            managerRef ! true
            expectNoMsg()

            managerRef.underlyingActor.completed should equal(true)
        }
    }
}
