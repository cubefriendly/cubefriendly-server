import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.cubefriendly.AppModule
import org.cubefriendly.manager.{CubeManager, CubeManagerModule}
import org.cubefriendly.processors.DataProcessorProvider
import org.cubefriendly.rest.{CubeQueryService, SourceService}
import scaldi.Injectable

/**
 * Cubefriendly
 * Created by david on 24.05.15.
 * This code is released under Apache 2 license
 */
object CubefriendlyServer extends App with SourceService with CubeQueryService with Injectable{

  implicit val appModule = new AppModule :: new CubeManagerModule

  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override implicit val manager = inject[CubeManager]
  override implicit val provider = inject[DataProcessorProvider]
  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  val corsHeaders = List(RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE"),
    RawHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cache-Control") )

  val optionsSupport = {
    options {complete("")}
  }

  val corsRoutes = {
    respondWithHeaders(corsHeaders) {optionsSupport ~ sourceRoutes ~ cubeQueryRoutes}
  }

  Http().bindAndHandle(corsRoutes, config.getString("http.interface"), config.getInt("http.port"))
}
