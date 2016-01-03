package uk.vitalcode.events.crawler.model

import uk.vitalcode.events.crawler.model.PropType.PropType

object PropType extends Enumeration {
    type PropType = Value
    val Text, Date = Value
}

case class Page(id: String, url: String, link: String, props: Map[String, Prop], pages: Set[Page], isRow: Boolean = false)

case class Prop(name: String, css: String, kind: PropType)

case class PageBuilder() extends Builder {
    var id: String = _
    var url: String = _
    var link: String = _
    var props: Map[String, Prop] = collection.immutable.HashMap[String, Prop]()
    var pages: Set[Page] = collection.immutable.HashSet()
    var isRow: Boolean = false

    def setId(id: String): PageBuilder = {
        this.id = id
        this
    }

    def setUrl(url: String): PageBuilder = {
        this.url = url
        this
    }

    def setLink(link: String): PageBuilder = {
        this.link = link
        this
    }

    def addProp(propBuilder: PropBuilder): PageBuilder = {
        val prop = propBuilder.build()
        this.props += (prop.name -> prop)
        this
    }

    def addPage(pageBuilder: PageBuilder): PageBuilder = {
        val page: Page = pageBuilder.build()
        this.pages += (page)
        this
    }

    def isRow(isRow: Boolean): PageBuilder = {
        this.isRow = isRow
        this
    }

    override type t = Page

    override def build(): Page = new Page(id, url, link, props, pages, isRow)
}

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

