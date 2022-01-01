package com.micahmusset.scalalearn.http4s.middleware.logging.builder

import cats.effect.Sync
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.Domain._
import com.micahmusset.scalalearn.http4s.middleware.logging.builder.contexts.{
  DefaultRequestLogConfig,
  DefaultResponseLogConfig,
  RequestLogConfig,
  ResponseLogConfig
}
import io.circe.syntax.EncoderOps
import org.http4s.{ Headers, Request, Response }

object StructuredLoggerBuilder {

  def headers(headers: Headers): String =
    headers.headers
      .flatMap(header => Map(header.name.toString -> header.value))
      .toMap
      .asJson
      .noSpaces

}

final case class StructuredLoggerBuilder[F[_]: Sync](
    context: Context = Context(),
    message: Message = Message()
) {

  // With Message
  def withMessageFromRequest(
      request: Request[F],
      transformation: RequestLogConfig[F] = DefaultRequestLogConfig[F]()
  ): StructuredLoggerBuilder[F] =
    copy(message = transformation.message(request))

  def withMessageFromResponse(
      request: Request[F],
      response: Response[F],
      transformation: ResponseLogConfig[F] = DefaultResponseLogConfig[F]()
  ): StructuredLoggerBuilder[F] =
    copy(message = transformation.message(request, response))

  // With Context
  def withContextFromRequest(
      request: Request[F],
      transformation: RequestLogConfig[F] = DefaultRequestLogConfig[F]()
  ): F[StructuredLoggerBuilder[F]] =
    for {
      cxt <- transformation.context(request)
    } yield copy(context = cxt)

  def withContextFromResponse(
      response: Response[F],
      transformation: ResponseLogConfig[F] = DefaultResponseLogConfig[F]()
  ): F[StructuredLoggerBuilder[F]] =
    for {
      cxt <- transformation.context(response)
    } yield copy(context = cxt)

  // Add Context
  def addContextFromRequest(
      request: Request[F],
      transformation: RequestLogConfig[F] = DefaultRequestLogConfig[F]()
  ): F[StructuredLoggerBuilder[F]] =
    for {
      cxt <- transformation.context(request)
    } yield copy(context = Context(context.value ++ cxt.value))

  def addContextFromResponse(
      response: Response[F],
      transformation: ResponseLogConfig[F] = DefaultResponseLogConfig[F]()
  ): F[StructuredLoggerBuilder[F]] =
    for {
      cxt <- transformation.context(response)
    } yield copy(context = Context(context.value ++ cxt.value))

}
