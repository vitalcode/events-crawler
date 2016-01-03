package uk.vitalcode.events.crawler

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.codec.digest.DigestUtils
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{Connection, Put, Table}
import org.apache.hadoop.hbase.util.Bytes

trait HBaseService {

    def saveData(connection: Connection, url: String, page: String)
}

trait DefaultHBaseService extends HBaseService with LazyLogging {

    override def saveData(connection: Connection, url: String, page: String) = {

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

trait TestHBaseService extends HBaseService with LazyLogging {

    override def saveData(connection: Connection, url: String, page: String) = {
        val maxLogLenght = 100

        logger.info(s"Url: $url")
        logger.info(s"Page: ${page.substring(0, Math.min(page.length(), maxLogLenght));}")
    }
}
