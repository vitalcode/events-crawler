package uk.vitalcode.events.crawler.test.common

import java.io.InputStream

import akka.actor._
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.testkit._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{Admin, Connection, ConnectionFactory}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class CrawlerTest(actorSystem: ActorSystem) extends TestKit(actorSystem)
with DefaultTimeout with ImplicitSender with WordSpecLike
with Matchers with BeforeAndAfterAll with MockFactory with LazyLogging {

    protected var hBaseConn: Connection = _
    protected var hBaseConf: Configuration = _
    protected val testTable: TableName = TableName.valueOf(TestConfig.hbaseTable)

    def this() = this(CrawlerTest.actorSystem)

    protected def testSystem = CrawlerTest.actorSystem

    protected def getPage(fileUrl: String): Future[HttpResponse] = {
        Future {
            val stream: InputStream = getClass.getResourceAsStream(fileUrl)
            val b: Array[Byte] = Stream.continually(stream.read).takeWhile(_ != -1).map(_.toByte).toArray
            HttpResponse().withEntity(ContentTypes.`application/json`, b)
        }
    }

    private def createTestTable(): Unit = {

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

    override def beforeAll(): Unit = {
        val hBaseConf: Configuration = HBaseConfiguration.create()
        hBaseConf.set(HConstants.ZOOKEEPER_QUORUM, TestConfig.hbaseZookeeperQuorum)
        hBaseConf.set(TableInputFormat.INPUT_TABLE, Bytes.toString(testTable.getName))
        hBaseConn = ConnectionFactory.createConnection(hBaseConf)

        createTestTable()
    }

    override def afterAll(): Unit = {
        shutdown()
    }
}

object CrawlerTest {

    val config = ConfigFactory.parseString(
        """
    akka{
        loggers = ["akka.event.slf4j.Slf4jLogger"]
        loglevel = "DEBUG"
        logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
    }
        """)

    val actorSystem: ActorSystem = ActorSystem("CrawlerTest", config)
}
