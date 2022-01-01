package com.micahmusset.scalalearn.http4s.middleware.logging.builder.contexts

import cats.Applicative
import cats.effect._
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.Domain._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.StructuredLoggerBuilder._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.contexts.DefaultResponseLogConfig.responseBody
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.{ Request, Response }

trait ResponseLogConfig[F[_]] {
  def message: (Request[F], Response[F]) => Message
  def context: Response[F] => F[Context]
}

final class EmptyResponseLogConfig[F[_]: Applicative] extends ResponseLogConfig[F] {
  override def message: (Request[F], Response[F]) => Message = (_, _) => Message()
  override def context: Response[F] => F[Context]            = _ => Context().pure[F]
}

object DefaultResponseLogConfig {
  def responseBody[F[_]: Sync: JsonDecoder](response: Response[F]): F[Context] =
    for {
      body <- response.asJson.attempt
    } yield body.fold(
      _ => Context(),
      _.asObject
        .map(_.toMap.view.mapValues(_.noSpaces).toMap)
        .map(Context)
        .getOrElse(Context())
    )
}

final case class DefaultResponseLogConfig[F[_]: Sync: JsonDecoder]() extends ResponseLogConfig[F] {

  override def message: (Request[F], Response[F]) => Message = (request, response) =>
    Message(s"Response [${response.status.code}] ${request.method} ${request.pathInfo}")

  override def context: Response[F] => F[Context] = response =>
    for {
      body <- responseBody(response)
    } yield Context(
      Map(
        "http.response.code"    -> response.status.code.toString,
        "http.response.body"    -> body.value.asJson.noSpaces,
        "http.response.headers" -> headers(response.headers),
        "idempotent_id"         -> "123"
      )
    )

}
