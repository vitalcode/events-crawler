package uk.vitalcode.events.crawler.test.cambridge

import uk.vitalcode.events.cambridge.CambridgeScienceCentre
import uk.vitalcode.events.crawler.test.common.CrawlerTest

class ScienceCentreTest extends CrawlerTest {

    "Client crawling Cambridge science centre web site" should {

        // page 1 list
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events-calendar/", true)
            .returns(getPage("/clientCambridgeScienceCentreTest/list1.html"))
            .once()

        // page 1 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events-calendar/gums-bumsshow-10/", true)
            .returns(getPage("/clientCambridgeScienceCentreTest/list1-details-1.html"))
            .once()

        // page 1 link 1 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/cache/25/44/25446f694110fcaa739951a7a5151025.jpg", false)
            .returns(getPage("/clientCambridgeScienceCentreTest/image.jpeg"))
            .once()

        // page 2 list
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events-calendar/week/2016-09-12/#pagination-fragment", true)
            .returns(getPage("/clientCambridgeScienceCentreTest/list2.html"))
            .once()

        // page 2 link 1
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/whats-on/events-calendar/recycle-life-0910/", true)
            .returns(getPage("/clientCambridgeScienceCentreTest/list2-details-1.html"))
            .once()

        // page 2 link 1 image
        (httpClientMock.makeRequest _)
            .expects("http://www.cambridgesciencecentre.org/media/cache/3e/9a/3e9a8c833f775230fc4f2bf9c740bb70.jpg", false)
            .returns(getPage("/clientCambridgeScienceCentreTest/image.jpeg"))
            .once()

        "should fetch data from 6 web pages (2 page lists each with 1 links)" in {

            assert(CambridgeScienceCentre.page)

        }
    }
}

