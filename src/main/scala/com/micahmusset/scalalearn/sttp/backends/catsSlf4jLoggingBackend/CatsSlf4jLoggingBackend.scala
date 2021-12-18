package com.micahmusset.scalalearn.sttp.backends.catsSlf4jLoggingBackend

import cats.effect.Concurrent
import cats.implicits._
import com.micahmusset.scalalearn.sttp.backends.catsSlf4jLoggingBackend.loggerconfig.LoggerConfig
import io.chrisdavenport.log4cats.StructuredLogger
import sttp.capabilities.Effect
import sttp.client3.impl.cats.CatsMonadAsyncError
import sttp.client3.{ Request, Response, SttpBackend }
import sttp.monad.MonadError

final class CatsSlf4jLoggingBackend[F[_]: Concurrent, +P] private (delegate: SttpBackend[F, P])(
    logger: StructuredLogger[F],
    config: LoggerConfig
) extends SttpBackend[F, P] {

  override def send[T, R >: P with Effect[F]](request: Request[T, R]): F[Response[T]] =
    for {
      response <- delegate.send(request)
      _        <- StructuredLogger
                    .withContext(logger)(context(request, response))
                    .info(config.message(request, response))
    } yield response

  override def close(): F[Unit] = delegate.close()

  /**
   * A monad instance for the effect type used when returning responses. Allows writing wrapper backends, which
   * map/flatMap over the return value of [[send]].
   */
  override def responseMonad: MonadError[F] = new CatsMonadAsyncError[F]

  private def context[T, R](request: Request[T, R], response: Response[T]): Map[String, String] =
    config.requestContext(request, response) ++ config.responseContext(request, response)

}
