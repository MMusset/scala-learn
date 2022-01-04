package com.micahmusset.scalalearn.http4s

import cats.effect._
import com.micahmusset.scalalearn.http4s.middleware.logging.v1.LoggingMiddleware
import com.micahmusset.scalalearn.http4s.middleware.logging.v2._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.HttpConfig
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.request.RequestLogConfig
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.response.ResponseLogConfig
import io.chrisdavenport.log4cats.StructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    val resources = for {
      logger <- Resource.eval[IO, StructuredLogger[IO]](Slf4jLogger.fromName[IO]("slf4j"))
    } yield logger

    resources.use { case (logger: StructuredLogger[IO]) =>
      implicit val implicitLogger = logger

      case class User(name: String)

      implicit val decoder = jsonOf[IO, User]

      val routes: HttpRoutes[IO] =
        HttpRoutes.of[IO] { case request @ POST -> Root / "hello" / world :? foo :? password :? cats =>
          for {
            requestBody  <- request.as[User]
//            _            <- Sync[IO].delay(println(requestBody))
            response     <- Ok(json"""{ "name" : ${requestBody.name}, "world" : $world, "password" : "password" }""")
            responseJson <- response.asJson
//            _            <- Sync[IO].delay(println(responseJson))
          } yield response
        }

      val loggedRoutes1 = LoggingMiddleware.apply[IO](routes.orNotFound)

      val defaultConfig = HttpConfig.default[IO]()
      val config        = {
        val filterKeys = Set[String]()
        val maskKeys   = Set("password")
        val mask       = "REDACTED"

        HttpConfig[IO](
          request = RequestLogConfig
            .empty[IO]()
            .addRequestMethod("http_method")
            .addRequestUri("http_uri")
            .addRequestBody("request_body"),
          response = ResponseLogConfig
            .empty[IO]()
            .addRequestMethod("request_method")
            .addRequestUri("request_uri")
            .addRequestBody("request_body")
            .addResponseBody("response_body")
        )
      }

      val loggedRoutes2 = new LoggingMiddleware[IO](routes.orNotFound, config).make

      // Perform
      val request = POST(json"""{ "name" : "test", "password" : "password" }""",
                         uri"/hello/world?foo=bar&password=password&cats=dogs"
      )

      val program = for {
//        _ <- loggedRoutes1.run(request)
        _ <- loggedRoutes2.run(request)
      } yield ()

      program.as(ExitCode.Success)
    }
  }
}
