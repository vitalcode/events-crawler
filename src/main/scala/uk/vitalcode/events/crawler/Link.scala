package uk.vitalcode.events.crawler

case class Link(id: String, url: String, css: String, links: Set[Link]) {
}


