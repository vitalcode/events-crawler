package uk.vitalcode.events.crawler.services

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.codec.digest.DigestUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants, TableName}
import uk.vitalcode.events.crawler.common.AppConfig
import uk.vitalcode.events.crawler.model.Page

trait HBaseService {
    def saveData(page: Page, data: String, indexId: String)
}

class DefaultHBaseService extends HBaseService with LazyLogging {

    // TODO pass from client or test remove dependency
    val conf: Configuration = HBaseConfiguration.create()
    conf.clear()
    conf.set(HConstants.ZOOKEEPER_QUORUM, AppConfig.hbaseZookeeperQuorum)
    conf.set(TableInputFormat.INPUT_TABLE, AppConfig.hbaseTable)

    val connection: Connection = ConnectionFactory.createConnection(conf)

    override def saveData(page: Page, data: String, indexId: String) = {
        logger.info(s"Saving data to HBase fetched from url [${page.url}]")

        val table: Table = connection.getTable(TableName.valueOf(AppConfig.hbaseTable))

        val put: Put = new Put(Bytes.toBytes(UUID.randomUUID.toString))
        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("data"), Bytes.toBytes(data))
        put.addColumn(Bytes.toBytes("content"), Bytes.toBytes("hash"), Bytes.toBytes(DigestUtils.sha1Hex(data)))
        put.addColumn(Bytes.toBytes("metadata"), Bytes.toBytes("indexId"), Bytes.toBytes(indexId))
        put.addColumn(Bytes.toBytes("metadata"), Bytes.toBytes("pageId"), Bytes.toBytes(page.id))
        put.addColumn(Bytes.toBytes("metadata"), Bytes.toBytes("mineType"), Bytes.toBytes(pageMineType(page.url)))
        put.addColumn(Bytes.toBytes("metadata"), Bytes.toBytes("url"), Bytes.toBytes(page.url))

        table.put(put)
        table.close()
    }

    // TODO refactor
    private def pageMineType(url: String): String = {
        if (url.contains(".jpg") || url.contains(".jpeg")) {
            "image/jpeg"
        } else if (url.contains(".png")) {
            "image/png"
        } else {
            "text/html"
        }
    }
}

class TestHBaseService extends HBaseService with LazyLogging {

    override def saveData(page: Page, data: String, indexId: String) = {
        val maxLogLenght = 200

        logger.info(s"url: [${page.url}]")
        //logger.info(s"content:data [${data.replace('\n', ' ').substring(0, Math.min(data.length(), maxLogLenght));}]")
        logger.info(s"content:hash [${DigestUtils.sha1Hex(data)}]")
        logger.info(s"metadata:indexId: [$indexId]")
        logger.info(s"metadata:pageId [${page.id}]")
        logger.info(s"metadata:mineType: [${pageMineType(page.url)}]")
    }

    // TODO refactor
    private def pageMineType(url: String): String = {
        if (url.contains(".jpg") || url.contains(".jpeg")) {
            "image/jpeg"
        } else if (url.contains(".png")) {
            "image/png"
        } else {
            "text/html"
        }
    }
}
