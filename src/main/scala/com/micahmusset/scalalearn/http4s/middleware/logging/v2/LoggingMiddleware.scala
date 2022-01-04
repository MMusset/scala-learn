package com.micahmusset.scalalearn.http4s.middleware.logging.v2

import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.HttpConfig
import io.chrisdavenport.log4cats.StructuredLogger
import org.http4s._

final class LoggingMiddleware[F[_]: Sync](routes: HttpApp[F], config: HttpConfig[F])(implicit
    logger: StructuredLogger[F]
) {
  def make: HttpApp[F] =
    Kleisli[F, Request[F], Response[F]] { (request: Request[F]) =>
      for {
        requestLog  <- requestLog(request).attempt
        _           <- requestLog.fold(errorLog, _.pure[F])
        response    <- routes.run(request)
        responseLog <- responseLog(request, response).attempt
        _           <- responseLog.fold(errorLog, _.pure[F])
      } yield response
    }

  private def requestLog(request: Request[F])(implicit logger: StructuredLogger[F]): F[Unit] =
    for {
      message <- config.request.message.run(request)
      context <- config.request.context.run(request)
      _       <- StructuredLogger.withContext[F](logger)(context).info(message)
    } yield ()

  private def responseLog(request: Request[F], response: Response[F])(implicit logger: StructuredLogger[F]): F[Unit] =
    for {
      message         <- config.response.message.run(request, response)
      responseContext <- config.response.context.run(request, response)
      _               <- StructuredLogger.withContext[F](logger)(responseContext).info(message)
    } yield ()

  private def errorLog(error: Throwable)(implicit logger: StructuredLogger[F]): F[Unit] =
    StructuredLogger
      .withContext[F](logger)(Map.empty)
      .warn(error)("Error logging http request/response: " + error.getMessage)

}
