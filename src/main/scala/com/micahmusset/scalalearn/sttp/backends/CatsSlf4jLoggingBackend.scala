package com.micahmusset.scalalearn.sttp.backends

import cats.effect.Concurrent
import cats.implicits._
import io.chrisdavenport.log4cats.StructuredLogger
import sttp.capabilities.Effect
import sttp.client3.impl.cats.CatsMonadAsyncError
import sttp.client3.{ Request, Response, SttpBackend }
import sttp.monad.MonadError

trait LoggerConfig {
  def message[T, R](request: Request[T, R], response: Response[T]): String
  def context[T, R](request: Request[T, R], response: Response[T]): Map[String, String]
}

final class DefaultLoggerConfig extends LoggerConfig {
  override def message[T, R](request: Request[T, R], response: Response[T]): String = {
    val path = request.uri.copy(querySegments = List.empty).toString()

    s"[${response.code}] ${request.method} $path"
  }

  override def context[T, R](request: Request[T, R], response: Response[T]): Map[String, String] = {
    val path = request.uri.copy(querySegments = List.empty).toString()

    val requestContext =
      Map(
        "http.request.method"    -> request.method.method,
        "http.request.uri"       -> request.uri.toString,
        "http.request.uri.path"  -> path,
        "http.request.uri.query" -> ???,
        "http.request.body"      -> ???,
        "http.request.headers"   -> ???
      )

    val responseContext =
      Map(
        "http.response.code"    -> ???,
        "http.response.body"    -> ???,
        "http.response.headers" -> ???
      )

    requestContext ++ responseContext
  }

}

final class CatsSlf4jLoggingBackend[F[_]: Concurrent, +P] private (delegate: SttpBackend[F, P])(
    logger: StructuredLogger[F],
    config: LoggerConfig
) extends SttpBackend[F, P] {

  override def send[T, R >: P with Effect[F]](request: Request[T, R]): F[Response[T]] =
    for {
      response <- delegate.send(request)
      _        <- StructuredLogger
                    .withContext(logger)(config.context(request, response))
                    .info(config.message(request, response))
    } yield response

  override def close(): F[Unit] = delegate.close()

  /**
   * A monad instance for the effect type used when returning responses. Allows writing wrapper backends, which
   * map/flatMap over the return value of [[send]].
   */
  override def responseMonad: MonadError[F] = new CatsMonadAsyncError[F]

}
