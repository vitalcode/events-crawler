package uk.vitalcode.events.crawler.common

import akka.actor.ActorSystem
import com.softwaremill.macwire._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants}
import uk.vitalcode.events.crawler.services._
import uk.vitalcode.events.model.Page

trait AppModule {

    val page: Page

    val system: ActorSystem

    lazy val hBaseConf: Configuration = HBaseConfiguration.create()
    hBaseConf.set(HConstants.ZOOKEEPER_QUORUM, AppConfig.hbaseZookeeperQuorum)
    hBaseConf.set(TableInputFormat.INPUT_TABLE, AppConfig.hbaseTable)

    lazy val hBaseConnection: Connection = ConnectionFactory.createConnection(hBaseConf)

    lazy val httpClient: HttpClient = wire[DefaultHttpClient]
    lazy val hBaseService: HBaseService = wire[DefaultHBaseService]
}
