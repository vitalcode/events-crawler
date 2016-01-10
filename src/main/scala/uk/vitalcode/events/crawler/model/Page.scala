package uk.vitalcode.events.crawler.model

import uk.vitalcode.events.crawler.model.PropType.PropType

object PropType extends Enumeration {
    type PropType = Value
    val Text, Date = Value
}

case class Page(var id: String, var ref: String,
                var url: String, var link: String,
                var props: collection.mutable.Map[String, Prop], var pages: collection.mutable.Set[Page],
                var parent: Page, var isRow: Boolean) {

    def this() = this(
        null, null,
        null, null,
        collection.mutable.HashMap[String, Prop](),
        collection.mutable.HashSet(),
        null, false
    )

    override def toString(): String = id

    override def hashCode(): Int = {
        var result: Int = 17
        result = 31 * result + (if (id != null) id.hashCode else 0)
        result = 31 * result + (if (ref != null) ref.hashCode else 0)
        result = 31 * result + (if (url != null) url.hashCode else 0)
        result = 31 * result + (if (link != null) link.hashCode else 0)
        result = 31 * result + (if (props != null) props.hashCode else 0)
        result = 31 * result + (if (pages != null) pages.hashCode else 0)
        result = 31 * result + (if (isRow) 1 else 0)
        result
    }

    // todo fix equalrs according to hashCode
    //override def equals(obj: scala.Any): Boolean = super.equals(obj)
}

case class Prop(name: String, css: String, kind: PropType)

case class PageBuilder() extends Builder {

    private val page = new Page()

    def setId(id: String): PageBuilder = {
        page.id = id
        this
    }

    def setRef(ref: String): PageBuilder = {
        page.ref = ref
        this
    }

    def setUrl(url: String): PageBuilder = {
        page.url = url
        this
    }

    def setLink(link: String): PageBuilder = {
        page.link = link
        this
    }

    def addProp(propBuilder: PropBuilder): PageBuilder = {
        val prop = propBuilder.build()
        page.props += (prop.name -> prop)
        this
    }

    def addPage(pageBuilder: PageBuilder): PageBuilder = {
        pageBuilder.setParent(page)
        val childPage: Page = pageBuilder.build()
        addPage(childPage)
        this
    }

    def addPage(childPage: Page): PageBuilder = {
        page.pages += (childPage)
        this
    }


    def isRow(isRow: Boolean): PageBuilder = {
        page.isRow = isRow
        this
    }

    private def setParent(parent: Page): PageBuilder = {
        page.parent = parent
        this
    }

    override type t = Page

    override def build(): Page = page
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

