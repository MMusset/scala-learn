package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.request

import cats.Applicative
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.domain.{ BodyConfig, PathConfig, QueryConfig }
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.implicits._
import org.http4s.Request

object RequestLogConfig {
  def empty[F[_]: Sync](): RequestLogConfig[F] =
    new RequestLogConfig[F](
      message = RequestTransformers.message[F],
      context = Kleisli.pure(Map.empty[String, String])
    )
}

final case class RequestLogConfig[F[_]: Sync: Applicative](
    message: Kleisli[F, Request[F], String],
    context: Kleisli[F, Request[F], Map[String, String]]
) {

  def addRequestMessage(message: String): RequestLogConfig[F] =
    copy(
      message = Kleisli[F, Request[F], String](_ => message.pure[F]),
      context = this.context
    )

  def addStatic(name: String, value: String): RequestLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+| RequestTransformers.static[F](name, value)
    )

  def addRequestMethod(name: String): RequestLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+| RequestTransformers.method[F](name)
    )

  def addRequestUri(
      name: String,
      pathConfig: PathConfig = PathConfig(
        maskRegex = "(^|/)[^/]*[0-9]{2,}[^/]*".r,
        maskValue = "_"
      ),
      queryConfig: QueryConfig = QueryConfig(
        filterKeys = Set[String](),
        maskKeys = Set[String](),
        maskValue = "REDACTED"
      )
  ): RequestLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+|
        RequestTransformers.uri[F](
          name,
          pathConfig.maskRegex,
          pathConfig.maskValue,
          queryConfig.maskKeys,
          queryConfig.maskValue,
          queryConfig.filterKeys
        )
    )

  def addRequestBody(
      name: String,
      bodyConfig: BodyConfig = BodyConfig(
        filterKeys = Set[String](),
        maskKeys = Set[String](),
        maskValue = "REDACTED"
      )
  ): RequestLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+|
        RequestTransformers.body[F](name, bodyConfig.filterKeys, bodyConfig.maskKeys, bodyConfig.maskValue)
    )
}
