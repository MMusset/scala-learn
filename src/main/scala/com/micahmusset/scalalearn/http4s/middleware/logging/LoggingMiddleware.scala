package com.micahmusset.scalalearn.http4s.middleware.logging

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.StructuredLoggerBuilder
import io.chrisdavenport.log4cats.StructuredLogger
import org.http4s._

object LoggingMiddleware {
  def apply[F[_]: Sync](routes: HttpApp[F])(implicit logger: StructuredLogger[F]): HttpApp[F] =
    Kleisli[F, Request[F], Response[F]] { (request: Request[F]) =>
      for {
        requestLog_  <- attemptLog(requestLog(request))
        response     <- routes.run(request)
        responseLog_ <- attemptLog(responseLog(request, response))
      } yield response
    }

  private def attemptLog[F[_]: Sync](
      logBuilder: F[StructuredLoggerBuilder[F]]
  )(implicit logger: StructuredLogger[F]): F[Unit] =
    for {
      result <- logBuilder.attempt
      _      <- result.fold(
                  error =>
                    StructuredLogger
                      .withContext[F](logger)(Map.empty)
                      .warn(error)("Error logging http request/response: " + error.getMessage),
                  logBuilder =>
                    StructuredLogger
                      .withContext[F](logger)(logBuilder.context.value)
                      .info(logBuilder.message.value)
                )
    } yield ()

  private def requestLog[F[_]: Sync](request: Request[F]): F[StructuredLoggerBuilder[F]] =
    StructuredLoggerBuilder[F]()
      .withMessageFromRequest(request)
      .withContextFromRequest(request)

  private def responseLog[F[_]: Sync](request: Request[F], response: Response[F]): F[StructuredLoggerBuilder[F]] =
    StructuredLoggerBuilder[F]()
      .withMessageFromResponse(request, response)
      .addContextFromRequest(request)
      .flatMap(_.addContextFromResponse(response))
}
