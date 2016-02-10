package uk.vitalcode.events.crawler.common

import akka.actor.ActorSystem
import com.softwaremill.macwire._
import uk.vitalcode.events.crawler.model.Page
import uk.vitalcode.events.crawler.services.{DefaultHBaseService, HBaseService, DefaultHttpClient, HttpClient}

trait AppModule {

    def page: Page
    def system: ActorSystem

    lazy val httpClient: HttpClient = wire[DefaultHttpClient]
    lazy val hBaseService: HBaseService = wire[DefaultHBaseService]
}
