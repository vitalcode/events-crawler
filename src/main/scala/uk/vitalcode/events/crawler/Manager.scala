package uk.vitalcode.events.crawler

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.stream.scaladsl.ImplicitMaterializer
import uk.vitalcode.events.crawler.model.Page

class Manager(requester: ActorRef, page: Page) extends Actor with ImplicitMaterializer with ActorLogging {

    def receive = {
        case page: Page =>
            log.info(s"Manager got page:$page")
            requester ! page
        case n: Int =>
            log.info(n.toString)
            requester ! page
        case strop: Boolean =>
            log.info("Manager completed job")
    }
}

