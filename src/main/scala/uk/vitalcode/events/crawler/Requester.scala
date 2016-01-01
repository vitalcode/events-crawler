package uk.vitalcode.events.crawler

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.ByteString
import jodd.jerry.Jerry
import jodd.jerry.Jerry._
import org.apache.commons.codec.digest.DigestUtils
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{Connection, Put, Table}
import org.apache.hadoop.hbase.util.Bytes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class Requester(connection: Connection) extends Actor with ImplicitMaterializer with ActorLogging {

    implicit val system = ActorSystem()

    def hbaseTest(url: String, page: String) = {

        val table: Table = connection.getTable(TableName.valueOf("page"))

        val put: Put = new Put(Bytes.toBytes(url))

        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("page"), Bytes.toBytes(page))
        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("hash"), Bytes.toBytes(DigestUtils.sha1Hex(page)))

        table.put(put)

        table.close()
    }

    def receive = {
        case link1: Link =>

            log.info(s"requester link with id:${link1.id}")

            require(link1.url != null)

            val send = sender

            val timeout = 3000.millis
            val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = link1.url))

            responseFuture.map(response => response.status match {
                case OK =>
                    val bs: Future[ByteString] = response.entity.toStrict(timeout).map {
                        _.data
                    }
                    val s: Future[String] = bs.map(_.utf8String)
                    s.map(page => {

                        log.info(page)

                        hbaseTest(link1.url, page)

                        val doc: Jerry = jerry(page)
                        val childLink: Link = if (link1.links != null) link1.links.toList.head else null

                        if (childLink != null) {
                            log.info(s"child css:${childLink.css}")

                            val childUrl = doc.$(childLink.css).attr("href")
                            log.info(s"child url:$childUrl")

                            val newChildLink = Link(childLink.id, childUrl, childLink.css, childLink.links)
                            send ! newChildLink
                        }
                        else send ! false
                    })
                case _ =>
                    send ! false
            })
    }
}
