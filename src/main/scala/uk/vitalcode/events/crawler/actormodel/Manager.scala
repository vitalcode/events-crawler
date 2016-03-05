package uk.vitalcode.events.crawler.actormodel

import akka.actor._
import akka.stream.scaladsl.ImplicitMaterializer
import com.softwaremill.macwire._
import uk.vitalcode.events.crawler.common.{AppConfig, AppModule}
import uk.vitalcode.events.model.Page

trait ManagerModule {
    this: AppModule with RequesterModule =>

    lazy val managerRef: ActorRef = system.actorOf(Props(wire[Manager]))
    lazy val manager: Manager = wire[Manager]

    def requesterFactory = () => requesterRef

    class Manager(requester: ActorRef, page: Page) extends Actor with ActorLogging with ImplicitMaterializer {

        var completed: Boolean = false

        var pagesCount = 0

        var dispose: () => Any = _

        def receive = {
            case PagesToFetch(pages, indexId) =>
                removeCountDown()
                log.info(s"Manager got PagesToFetch request [${PagesToFetch(pages, indexId)}]")
                pages.foreach(pageToFetch => {
                    Thread.sleep(AppConfig.throttle)
                    log.info(s"Manager ask requester to fetch page [$pageToFetch]")
                    addCountDown()
                    requesterFactory() ! FetchPage(pageToFetch, indexId)
                })
            case disposeFunction: (() => Any) =>
                dispose = disposeFunction
                addCountDown()
                requester ! FetchPage(page, null)
            case finish: Boolean =>
                log.info("Manager completes job")
                completed = true
                removeCountDown()
                checkCountDown()
        }

        def printCountDown() = {
            log.info(s"Manager current requested pages count [$pagesCount]")
        }

        def addCountDown() = {
            pagesCount = pagesCount + 1
            printCountDown()
        }

        def removeCountDown() = {
            pagesCount = pagesCount - 1
            printCountDown()
        }

        def checkCountDown() = {
            if (pagesCount == 0) {
                log.info(s"Manager shutting down")
                dispose()
                system.shutdown()
            }
        }
    }

}
