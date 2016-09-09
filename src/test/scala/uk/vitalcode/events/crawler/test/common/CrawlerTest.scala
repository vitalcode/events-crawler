package uk.vitalcode.events.crawler.test.common

import java.io.InputStream

import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{Admin, Connection, ConnectionFactory}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}
import uk.vitalcode.events.crawler.actormodel.{ManagerModule, RequesterModule}
import uk.vitalcode.events.crawler.common.AppModule
import uk.vitalcode.events.crawler.services.HttpClient
import uk.vitalcode.events.model.Page

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

abstract class CrawlerTest(actorSystem: ActorSystem) extends TestKit(actorSystem)
    with DefaultTimeout with ImplicitSender with WordSpecLike
    with Matchers with BeforeAndAfterAll with MockFactory with LazyLogging {

    protected var hBaseConn: Connection = _
    protected var hBaseConf: Configuration = _
    protected val testTable: TableName = TableName.valueOf(TestConfig.hbaseTable)
    protected val httpClientMock: HttpClient = mock[HttpClient]

    def this() = this(CrawlerTest.actorSystem)

    protected def testSystem = CrawlerTest.actorSystem

    protected def mock(url: String, html: Boolean, path: String) = {
        (httpClientMock.makeRequest _)
            .expects(url, html)
            .returns(getPage(path))
            .once()
    }

    protected def getPage(fileUrl: String): Array[Byte] = {
        val stream: InputStream = getClass.getResourceAsStream(fileUrl)
        Stream.continually(stream.read).takeWhile(_ != -1).map(_.toByte).toArray
    }

    protected def assert(page: Page): Unit = {
        val managerModule = new AppModule with ManagerModule with RequesterModule {
            override lazy val system = testSystem
            override lazy val pages: Set[Page] = Set(page)
            override lazy val hBaseConnection: Connection = hBaseConn
            override lazy val httpClient: HttpClient = httpClientMock
        }

        within(20.seconds) {
            val dispose = () => hBaseConn.close()
            managerModule.managerRef ! dispose
            expectNoMsg()
        }
    }

    private def createTestTable(): Unit = {

        val admin: Admin = hBaseConn.getAdmin
        if (admin.isTableAvailable(testTable)) {
            if (!admin.isTableDisabled(testTable)) admin.disableTable(testTable)
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
        hBaseConf = HBaseConfiguration.create()
        hBaseConf.set(HConstants.ZOOKEEPER_QUORUM, TestConfig.hbaseZookeeperQuorum)
        hBaseConf.set(TableInputFormat.INPUT_TABLE, TestConfig.hbaseTable)
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
