package uk.vitalcode.events.crawler

import java.io.InputStream

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer

import scala.concurrent.Future


trait HttpClient {
    def makeRequest(url: String): Future[HttpResponse]
}

class DefaultHttpClient extends HttpClient {
    //    final ActorSystem system = ActorSystem.create()
    //    final Materializer materializer = ActorMaterializer.create(system)

    def makeRequest(url: String): Future[HttpResponse] = {

        implicit val system = ActorSystem()
        implicit val materializer = ActorMaterializer.create(system)


        Http().singleRequest(HttpRequest(uri = url))

        //        val json =
        //            """
        //        {
        //           "results" : [
        //              {
        //                 "elevation" : 8815.71582031250,
        //                 "location" : {
        //                    "lat" : 27.9880560,
        //                    "lng" : 86.92527800000001
        //                 },
        //                 "resolution" : 152.7032318115234
        //              }
        //           ],
        //           "status" : "OK"
        //        } """
        //
        //        Future {
        //            HttpResponse()
        //                .withStatus(StatusCodes.OK)
        //                .withEntity(ContentTypes.`application/json`, json.getBytes)
        //        }
    }
}

class TestHttpClient extends HttpClient {
    //    final ActorSystem system = ActorSystem.create()
    //    final Materializer materializer = ActorMaterializer.create(system)

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Future


    override def makeRequest(url: String): Future[HttpResponse] = {
        Future {
            val stream : InputStream = getClass.getResourceAsStream("/pageA.html")
            val b: Array[Byte] = Stream.continually(stream.read).takeWhile(_ != -1).map(_.toByte).toArray
            HttpResponse().withEntity(ContentTypes.`application/json`, b)
        }
    }
}
