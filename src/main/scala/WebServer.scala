import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.StatusCodes

/**
 * A 404 server similar to
 * https://github.com/kubernetes/ingress-nginx/tree/master/images/404-server
 *
 * Serves a 404 page at /
 * Serves 200 on a /healthz
 */
object WebServer {

  private var _count = new AtomicLong(0L)

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()

    val requestHandler: HttpRequest => HttpResponse =
      {
        case HttpRequest(GET, Uri.Path("/healthz"), _, _, _) =>
          HttpResponse(entity = HttpEntity(
            ContentTypes.`text/plain(UTF-8)`,
            { _count.getAndIncrement().toString }))

        case r: HttpRequest =>
          r.discardEntityBytes() // important to drain incoming HTTP Entity stream
          HttpResponse(StatusCodes.NotFound, entity = "Unknown resource!")
      }

    val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)

    Await.result(system.whenTerminated, Duration.Inf)

  }
}
