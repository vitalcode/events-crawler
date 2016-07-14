package uk.vitalcode.events.crawler.common

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

    val conf: Config = ConfigFactory.load()

    def httpClientUserAgent: String = conf.getString("crawler.httpClient.userAgent")

    def httpClientThrottle: Int = conf.getInt("crawler.httpClient.throttle")

    def httpClientTimeout: Int = conf.getInt("crawler.httpClient.timeout")

    def httpClientPhantomPath: String = conf.getString("crawler.httpClient.phantom.path")

    def httpClientWindowWidth: Int = conf.getInt("crawler.httpClient.phantom.windowWidth")

    def httpClientWindowHeight: Int = conf.getInt("crawler.httpClient.phantom.windowHeight")

    def hbaseTable: String = conf.getString("crawler.hbase.table")

    def hbaseZookeeperQuorum: String = conf.getString("crawler.hbase.zookeeperQuorum")
}
