package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.response

import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.domain.{ BodyConfig, PathConfig, QueryConfig }
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.implicits._
import org.http4s.{ Request, Response }

object ResponseLogConfig {
  def empty[F[_]: Sync](): ResponseLogConfig[F] =
    new ResponseLogConfig[F](
      message = ResponseTransformers.message[F],
      context = Kleisli.pure(Map.empty[String, String])
    )
}

final case class ResponseLogConfig[F[_]: Sync](
    message: Kleisli[F, (Request[F], Response[F]), String],
    context: Kleisli[F, (Request[F], Response[F]), Map[String, String]]
) {

  def addStatic(name: String, value: String): ResponseLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+| ResponseTransformers.static(name, value)
    )

  def addRequestMethod(name: String): ResponseLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+| ResponseTransformers.method[F](name)
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
  ): ResponseLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+|
        ResponseTransformers.uri[F](
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
  ): ResponseLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+|
        ResponseTransformers.body[F](name, bodyConfig.filterKeys, bodyConfig.maskKeys, bodyConfig.maskValue)
    )

  def addResponseBody(
      name: String,
      bodyConfig: BodyConfig = BodyConfig(
        filterKeys = Set[String](),
        maskKeys = Set[String](),
        maskValue = "REDACTED"
      )
  ): ResponseLogConfig[F] =
    copy(
      message = this.message,
      context = this.context |+|
        ResponseTransformers.body[F](name, bodyConfig.filterKeys, bodyConfig.maskKeys, bodyConfig.maskValue)
    )

}
