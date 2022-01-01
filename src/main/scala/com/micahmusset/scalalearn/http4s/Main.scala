package com.micahmusset.scalalearn.http4s

import cats.effect._
import com.micahmusset.scalalearn.http4s.middleware.logging.LoggingMiddleware
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
        HttpRoutes.of[IO] { case request @ POST -> Root / "hello" / world :? foo =>
          for {
            requestBody  <- request.as[User]
            _            <- Sync[IO].delay(println(requestBody))
            response     <- Ok(json"""{ "name" : ${requestBody.name}, "world" : $world }""")
            responseJson <- response.asJson
            _            <- Sync[IO].delay(println(responseJson))
          } yield response
        }

      val loggedRoutes = LoggingMiddleware.apply[IO](routes.orNotFound)

      val request = POST(json"""{ "name" : "test" }""", uri"/hello/world?foo=bar")

      val program = for {
        response <- loggedRoutes.run(request)
      } yield response

      program.as(ExitCode.Success)
    }
  }
}
