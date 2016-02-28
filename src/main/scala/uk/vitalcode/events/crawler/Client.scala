package uk.vitalcode.events.crawler

import akka.actor._
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{Admin, Connection, ConnectionFactory}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import uk.vitalcode.events.cambridge
import uk.vitalcode.events.crawler.actormodel.{ManagerModule, RequesterModule}
import uk.vitalcode.events.crawler.common.{AppConfig, AppModule}
import uk.vitalcode.events.model.Page

object Client extends LazyLogging {

    def main(args: Array[String]) {
        if (args.length > 0) {
            args(0) match {
                case "scrawl" =>
                    runScrawler()
                case _ => info()
            }
        } else {
            info()
        }
    }

    private def info(): Unit = {
        println("arguments: [scrawl]")
    }

    private def runScrawler(): Unit = {

        val hBaseConf: Configuration = HBaseConfiguration.create()
        hBaseConf.set(HConstants.ZOOKEEPER_QUORUM, AppConfig.hbaseZookeeperQuorum)
        hBaseConf.set(TableInputFormat.INPUT_TABLE, AppConfig.hbaseTable)

        val hBaseConn: Connection = ConnectionFactory.createConnection(hBaseConf)

        val managerModule = new AppModule with ManagerModule with RequesterModule {
            override lazy val system = ActorSystem("ScrawlerSystem")
            override lazy val page: Page = cambridge.Pages.cambridgeScienceCentre
            override lazy val hBaseConnection: Connection = hBaseConn
        }

        createTestTable(hBaseConn)

        managerModule.managerRef ! 1
    }

    protected val testTable: TableName = TableName.valueOf(AppConfig.hbaseTable)

    private def createTestTable(hBaseConn: Connection): Unit = {

        val admin: Admin = hBaseConn.getAdmin()
        if (admin.isTableAvailable(testTable)) {
            admin.disableTable(testTable)
            admin.deleteTable(testTable)
            logger.info(s"Test table [$testTable] deleted")
        }

        val tableDescriptor: HTableDescriptor = new HTableDescriptor(testTable)
        tableDescriptor.addFamily(new HColumnDescriptor("content"))
        tableDescriptor.addFamily(new HColumnDescriptor("metadata"))
        admin.createTable(tableDescriptor)
        logger.info(s"New Test table [$testTable] created")

        admin.close()
    }
}
