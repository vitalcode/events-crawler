package uk.vitalcode.events.crawler.model

trait Builder {
    type t

    def build(): t
}
