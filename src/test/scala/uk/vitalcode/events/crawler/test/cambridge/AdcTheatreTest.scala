package uk.vitalcode.events.crawler.test.cambridge

import uk.vitalcode.events.cambridge.AdcTheater
import uk.vitalcode.events.crawler.test.common.CrawlerTest

class AdcTheatreTest extends CrawlerTest {

    "Crawling visit Abc Theater" should {

        // page 1 list
        mock("http://www.adctheatre.com/whats-on.aspx", true, "/adcTheatre/list1.html")

        // page 1 link 1
        mock("http://www.adctheatre.com/whats-on/drama/cast-2016-as-you-like-it-preview.aspx", true, "/adcTheatre/list1-details-1.html")
        // page 1 link 1 image
        mock("http://www.adctheatre.com/media/107947393/As-You-Like-It_Landscape.jpg", false, "/adcTheatre/image.jpeg")

        // page 1 link 2
        mock("http://www.adctheatre.com/whats-on/workshop/backstage-at-the-adc-theatre.aspx", true, "/adcTheatre/list1-details-2.html")
        // page 1 link 2 image
        mock("http://www.adctheatre.com/media/997805/curtain_Landscape.jpg", false, "/adcTheatre/image.jpeg")

        // page 1 link 3
        mock("http://www.adctheatre.com/whats-on/musical/made-in-dagenham.aspx", true, "/adcTheatre/list1-details-3.html")
        // page 1 link 3 image
        mock("http://www.adctheatre.com/media/112832935/Made-in-Dagenham_Landscape.jpg", false, "/adcTheatre/image.jpeg")

        "should fetch data from 7 web pages" in {
            assert(AdcTheater.page)
        }
    }
}

