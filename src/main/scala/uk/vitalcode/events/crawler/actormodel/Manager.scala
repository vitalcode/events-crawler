package uk.vitalcode.events.crawler.actormodel

import akka.actor._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import uk.vitalcode.events.crawler.UserModule
import uk.vitalcode.events.crawler.model.Page

trait ManagerModule {
    this: UserModule with RequesterModule =>

    lazy val managerRef: ActorRef = system.actorOf(Props(wire[Manager]))
    lazy val manager: Manager = wire[Manager]

    def requesterFactory = () => requesterRef

    class Manager(requester: ActorRef, page: Page) extends Actor with ActorLogging
    with ImplicitMaterializer {


        var completed: Boolean = false

        def receive = {
            case PagesToFetch(pages) =>
                pages.foreach(pageToFetch => {
                    log.info(s"Manager request fetching: ${pageToFetch.id}")
                    println(s"Manager request fetching: ${pageToFetch}")
                    val requesterRef = requesterFactory()
                    requesterRef ! FetchPage(pageToFetch)
                })
            case n: Int =>
                log.info(n.toString)
                requester ! FetchPage(page)
            case strop: Boolean =>
                log.info("Manager completed job")
                completed = true
        }
    }
}





