package uk.vitalcode.events.crawler.actormodel

import akka.actor._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import uk.vitalcode.events.crawler.common.AppModule
import uk.vitalcode.events.crawler.model.Page

trait ManagerModule {
    this: AppModule with RequesterModule =>

    lazy val managerRef: ActorRef = system.actorOf(Props(wire[Manager]))
    lazy val manager: Manager = wire[Manager]

    def requesterFactory = () => requesterRef

    class Manager(requester: ActorRef, page: Page) extends Actor with ActorLogging with ImplicitMaterializer {

        var completed: Boolean = false

        def receive = {
            case PagesToFetch(pages, indexId) =>
                pages.foreach(pageToFetch => {
                    log.info(s"Manager ask requester to fetch page [$pageToFetch]")
                    val requesterRef = requesterFactory()
                    requesterRef ! FetchPage(pageToFetch, indexId)
                })
            case n: Int =>
                log.info(n.toString)
                requester ! FetchPage(page, null)
            case strop: Boolean =>
                log.info("Manager completed job")
                completed = true
        }
    }

}
