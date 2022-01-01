package com.micahmusset.scalalearn.http4s.middleware.logging.builder.contexts

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.Domain._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.StructuredLoggerBuilder
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.contexts.DefaultRequestLogConfig.body
import io.circe.literal._
import io.circe.syntax.EncoderOps
import org.http4s.Request
import org.http4s.circe._

trait RequestLogConfig[F[_]] {
  def message: Request[F] => Message
  def context: Request[F] => F[Context]
}

final class EmptyRequestContext[F[_]: Applicative] extends RequestLogConfig[F] {
  override def message: Request[F] => Message      = _ => Message()
  override def context: Request[F] => F[Context] = _ => Context().pure[F]
}

object DefaultRequestLogConfig {
  def body[F[_]: Sync: JsonDecoder](request: Request[F]): F[Map[String, String]] =
    for {
      body <- request.asJson.attempt
    } yield body.fold(
      Map.empty,
      _.asObject
        .map(_.toMap.view.mapValues(_.noSpaces).toMap)
        .getOrElse(Map.empty)
    )
}

final case class DefaultRequestLogConfig[F[_]: Sync: JsonDecoder]() extends RequestLogConfig[F] {
  override def message: Request[F] => Message = request => Message(s"Request ${request.method} ${request.pathInfo}")

  override def context: Request[F] => F[Context] = request =>
    for {
      body <- body(request)
    } yield Context(
      Map(
        "http.request.method"    -> request.method.name,
        "http.request.uri"       -> request.uri.toString,
        "http.request.uri.path"  -> request.pathInfo.toString(),
        "http.request.uri.query" -> request.uri.query.params.asJson.noSpaces,
        "http.request.body"      -> body.asJson.noSpaces,
        "http.request.headers"   -> StructuredLoggerBuilder.headers(request.headers),
        "idempotent_id"          -> "123"
      )
    )
}
