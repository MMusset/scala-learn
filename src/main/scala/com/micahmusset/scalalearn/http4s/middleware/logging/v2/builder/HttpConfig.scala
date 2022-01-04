package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder

import cats.data.Kleisli
import cats.effect.Sync
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.request.{
  RequestLogConfig,
  RequestTransformers
}
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.response.{
  ResponseLogConfig,
  ResponseTransformers
}

object HttpConfig {

  def default[F[_]: Sync](): HttpConfig[F] =
    HttpConfig[F](
      request = RequestLogConfig[F](
        message = RequestTransformers.message[F],
        context = Kleisli.pure(Map.empty[String, String])
      ),
      response = ResponseLogConfig[F](
        message = ResponseTransformers.message[F],
        context = Kleisli.pure(Map.empty[String, String])
      )
    )

}

final case class HttpConfig[F[_]: Sync](
    request: RequestLogConfig[F],
    response: ResponseLogConfig[F]
)
