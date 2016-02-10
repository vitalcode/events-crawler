package uk.vitalcode.events.crawler.common

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

    val conf: Config = ConfigFactory.load()

    def userAgent: String = {
        conf.getString("crawler.httpClient.userAgent")
    }

    def throttle: Int = {
        conf.getInt("crawler.httpClient.throttle")
    }

    def hbaseTable: String = {
        conf.getString("crawler.hbase.table")
    }

    def hbaseZookeeperQuorum: String = {
        conf.getString("crawler.hbase.zookeeperQuorum")
    }
}
