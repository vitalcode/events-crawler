package uk.vitalcode.events.crawler

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.stream.scaladsl.ImplicitMaterializer
import uk.vitalcode.events.crawler.model.Page

class Manager(requester: ActorRef, page: Page) extends Actor with ImplicitMaterializer with ActorLogging {

    var completed: Boolean = false

    def receive = {
        case PagesToFetch(pages) =>
            pages.foreach(pageToFetch => {
                log.info(s"Manager got page: ${pageToFetch.id}")
                requester ! FetchPage(pageToFetch)
            })
        case n: Int =>
            log.info(n.toString)
            requester ! FetchPage(page)
        case strop: Boolean =>
            log.info("Manager completed job")
            completed = true
    }
}

