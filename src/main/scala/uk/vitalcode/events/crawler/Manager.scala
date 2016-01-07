package uk.vitalcode.events.crawler

import akka.actor._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import uk.vitalcode.events.crawler.model.Page

//class Manager(requester: ActorRef, page: Page)(implicit di: Class[_ <: IndirectActorProducer]) extends Actor with ActorLogging
//with ImplicitMaterializer with UserModule {

class Manager(requester: ActorRef, page: Page, requesterFactory: () => Requester) extends Actor with ActorLogging
with ImplicitMaterializer  {


    var completed: Boolean = false

    def receive = {
        case PagesToFetch(pages) =>
            pages.foreach(pageToFetch => {
                log.info(s"Manager request fetching: ${pageToFetch.id}")
                requesterFactory11() ! FetchPage(pageToFetch)
            })
        case n: Int =>
            log.info(n.toString)
            requester ! FetchPage(page)
        case strop: Boolean =>
            log.info("Manager completed job")
            completed = true
    }

    private def requesterFactory11(): ActorRef = {
        // DI


        // context.actorOf(Props(produce()))
        //        context.actorOf(Props(classOf[re], new TestHttpClient(), new TestHBaseService()))

        //val requesterRef = system.actorOf(Props(requester))
        //context.actorOf(Props(classOf[DependencyInjector],  DI(context.system).ctx))

        //println("WWWOOOWWWWW " + DI(context.system).ctx)

        context.actorOf(
            Props(classOf[DependencyInjector], context, requesterFactory), "helloBean")
    }

}

class DependencyInjector(applicationContext: ActorContext, requesterFactory: () => Requester) extends IndirectActorProducer {

    override def actorClass = classOf[Actor]
    override def produce = requesterFactory() //wire[Requester] // new Requester(new TestHttpClient(), new TestHBaseService())
    //DI(applicationContext.system).ctx

}





