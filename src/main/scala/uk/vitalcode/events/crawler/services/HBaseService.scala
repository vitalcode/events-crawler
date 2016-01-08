package uk.vitalcode.events.crawler.services

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.codec.digest.DigestUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}

trait HBaseService {

    def saveData(url: String, page: String)
}

class DefaultHBaseService extends HBaseService with LazyLogging {

    val conf: Configuration = HBaseConfiguration.create()
    conf.clear()
    conf.set("hbase.zookeeper.quorum", "robot1.vk,robot2.vk,robot3.vk")

    val connection: Connection = ConnectionFactory.createConnection(conf)


    override def saveData(url: String, page: String) = {

        val maxLogLenght = 100

        logger.info(s"Url: $url")
        logger.info(s"Page: ${page.substring(0, Math.min(page.length(), maxLogLenght));}")

        val table: Table = connection.getTable(TableName.valueOf("page"))

        val put: Put = new Put(Bytes.toBytes(url))

        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("page"), Bytes.toBytes(page))
        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("hash"), Bytes.toBytes(DigestUtils.sha1Hex(page)))

        table.put(put)

        table.close()
    }
}

class TestHBaseService extends HBaseService with LazyLogging {


    override def saveData(url: String, page: String) = {
        val maxLogLenght = 100

        logger.info(s"Url: $url")
        logger.info(s"Page: ${page.substring(0, Math.min(page.length(), maxLogLenght));}")
    }
}
