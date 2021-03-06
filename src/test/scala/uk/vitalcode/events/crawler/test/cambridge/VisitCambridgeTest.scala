package uk.vitalcode.events.crawler.test.cambridge

import uk.vitalcode.events.cambridge.VisitCambridge
import uk.vitalcode.events.crawler.test.common.CrawlerTest

class VisitCambridgeTest extends CrawlerTest {

    "Client crawling visit cambridge web site" should {

        // page 1 list
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/whats-on/searchresults?sr=1&rd=on&anydate=yes", true)
            .returns(getPage("/clientVisitCambridgeTest/list1.html"))
            .once()

        // page 1 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/whats-on/official-guided-tours-cambridge-college-tour-including-kings-college-p568001", true)
            .returns(getPage("/clientVisitCambridgeTest/list1-details-1.html"))
            .once()

        // page 1 link 1 image
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/imageresizer/?image=%2Fdmsimgs%2FGuided%2DTour%2D6%5F68928799%2Ejpg&action=ProductMain", false)
            .returns(getPage("/clientVisitCambridgeTest/image.jpeg"))
            .once()

        // page 1 link 2
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/whats-on/cambridge-thai-festival-p686901", true)
            .returns(getPage("/clientVisitCambridgeTest/list1-details-2.html"))
            .once()

        // page 1 link 2 image
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/imageresizer/?image=%2Fdmsimgs%2FThai%5FFestival%5F2%2Ejpg%5FResized%5F269021640%2Ejpg&action=ProductMain", false)
            .returns(getPage("/clientVisitCambridgeTest/image.jpeg"))
            .once()

        // page 2 list
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/whats-on/searchresults?p=2&sr=1&rd=on&anydate=yes", true)
            .returns(getPage("/clientVisitCambridgeTest/list2.html"))
            .once()

        // page 2 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/whats-on/mill-road-feast-p679931", true)
            .returns(getPage("/clientVisitCambridgeTest/list2-details-1.html"))
            .once()

        // page 2 link 1 image
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/imageresizer/?image=%2Fdmsimgs%2FMill%2DRoad%2Dfood%2Dfair%5F1167514642%2Ejpg&action=ProductMain", false)
            .returns(getPage("/clientVisitCambridgeTest/image.jpeg"))
            .once()

        // page 2 link 2
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/whats-on/capability-brown-glimpses-of-ickworth-p680981", true)
            .returns(getPage("/clientVisitCambridgeTest/list2-details-2.html"))
            .once()

        // page 2 link 2 image
        (httpClientMock.makeRequest _)
            .expects("http://www.visitcambridge.org/imageresizer/?image=%2Fdmsimgs%2Fcb%5Fsummer%5F2146502143%2Ejpg&action=ProductMain", false)
            .returns(getPage("/clientVisitCambridgeTest/image.jpeg"))
            .once()

        "should fetch data from 8 web pages (2 page lists each with 2 links + 1 image each link" in {
            assert(VisitCambridge.page)
        }
    }
}

