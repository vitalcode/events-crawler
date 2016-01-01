package uk.vitalcode.events.crawler

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.stream.scaladsl.ImplicitMaterializer

class Manager(requester: ActorRef, link: Link) extends Actor with ImplicitMaterializer with ActorLogging {

    def receive = {
        case link: Link =>
            log.info(s"Manager got link:$link")
            requester ! link
        case n: Int =>
            log.info(n.toString)
            requester ! link
        case strop: Boolean =>
            log.info("Manager completed job")
    }
}

