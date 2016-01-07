package uk.vitalcode.events.crawler.test

import akka.actor.{Actor, IndirectActorProducer, ActorSystem}
import akka.testkit._
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers, WordSpecLike}
import uk.vitalcode.events.crawler._
import uk.vitalcode.events.crawler.model.{Page, PageBuilder, PropBuilder, PropType}

class ManagerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with UserModule
with WordSpecLike with ShouldMatchers with BeforeAndAfterAll {

    def this() = this(ActorSystem("MySpec"))

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }

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

            class DependencyInjector extends IndirectActorProducer {

                override def produce(): Actor = requester

                override def actorClass: Class[_ <: Actor] = classOf[Requester]
            }

            implicit val di = classOf[DependencyInjector]

            val echo = TestActorRef(TestActors.echoActorProps)
            val manager = TestActorRef(new Manager(echo, page, () => requesterFactory))

            manager ! true
            expectNoMsg()

            manager.underlyingActor.completed should equal(true)
        }
    }
}
