package uk.vitalcode.events.crawler.model

import uk.vitalcode.events.crawler.model.PropType.PropType

object PropType extends Enumeration {
    type PropType = Value
    val Text, Date = Value
}

case class Prop(name: String, css: String, kind: PropType)

case class PropBuilder() extends Builder {
    private var name: String = _
    private var css: String = _
    private var kind: PropType = _

    def setName(name: String): PropBuilder = {
        this.name = name
        this
    }

    def setCss(css: String): PropBuilder = {
        this.css = css
        this
    }

    def setKind(kind: PropType): PropBuilder = {
        this.kind = kind
        this
    }

    override type t = Prop

    override def build(): Prop = new Prop(name, css, kind)
}