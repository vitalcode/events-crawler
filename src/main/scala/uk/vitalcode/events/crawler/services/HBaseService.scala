package uk.vitalcode.events.crawler.services

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.codec.digest.DigestUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants, TableName}
import uk.vitalcode.events.crawler.common.AppConfig

trait HBaseService {
    def saveData(url: String, page: String)
}

class DefaultHBaseService extends HBaseService with LazyLogging {

    val conf: Configuration = HBaseConfiguration.create()
    conf.set(HConstants.ZOOKEEPER_QUORUM, AppConfig.hbaseZookeeperQuorum)
    conf.set(TableInputFormat.INPUT_TABLE, AppConfig.hbaseTable)

    val connection: Connection = ConnectionFactory.createConnection(conf)

    override def saveData(url: String, page: String) = {
        logger.info(s"Saving data to HBase fetched from url [$url]")

        val table: Table = connection.getTable(TableName.valueOf(AppConfig.hbaseTable))

        val put: Put = new Put(Bytes.toBytes(url))
        // TODO also add metadata:mine-type
        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("data"), Bytes.toBytes(page))
        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("hash"), Bytes.toBytes(DigestUtils.sha1Hex(page)))

        table.put(put)
        table.close()
    }
}

class TestHBaseService extends HBaseService with LazyLogging {

    override def saveData(url: String, page: String) = {
        val maxLogLenght = 200

        logger.info(s"Url: $url")
        logger.info(s"Data: ${page.replace('\n', ' ').substring(0, Math.min(page.length(), maxLogLenght));}")
    }
}
