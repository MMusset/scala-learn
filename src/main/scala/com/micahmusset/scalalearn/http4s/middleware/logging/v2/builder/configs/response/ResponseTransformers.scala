package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.response

import cats.Applicative
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.utils._
import io.circe.syntax._
import org.http4s.circe.{ JsonDecoder, _ }
import org.http4s.{ Request, Response }

import scala.util.matching.Regex

object ResponseTransformers {

  def static[F[_]: Applicative](
      name: String,
      value: String
  ): Kleisli[F, (Request[F], Response[F]), Map[String, String]] =
    Kleisli[F, (Request[F], Response[F]), Map[String, String]] { case (_, _) => Map(name -> value).pure[F] }

  def message[F[_]: Applicative]: Kleisli[F, (Request[F], Response[F]), String] =
    Kleisli[F, (Request[F], Response[F]), String] { case (request, response) =>
      s"Response [${response.status.code}] ${request.method} ${request.pathInfo}".pure[F]
    }

  def method[F[_]: Applicative](name: String): Kleisli[F, (Request[F], Response[F]), Map[String, String]] =
    Kleisli[F, (Request[F], Response[F]), Map[String, String]] { case (request, _) =>
      Map(name -> request.method.name).pure[F]
    }

  def uri[F[_]: Applicative](
      name: String,
      pathMaskRegex: Regex,
      pathMaskRegexValue: String,
      queryMaskKeys: Set[String],
      queryMaskValue: String,
      queryFilter: Set[String]
  ): Kleisli[F, (Request[F], Response[F]), Map[String, String]] =
    Kleisli[F, (Request[F], Response[F]), Map[String, String]] { case (request, _) =>
      val filterAndMask =
        new QueryFilter(queryFilter).filter _ andThen
          new QueryMask(queryMaskKeys, queryMaskValue).mask andThen
          new PathMask(pathMaskRegex, pathMaskRegexValue).mask

      val value = filterAndMask(request.uri).renderString

      Map(name -> value).pure[F]
    }

  def body[F[_]: Sync: JsonDecoder](
      name: String,
      filter: Set[String],
      maskKeys: Set[String],
      maskValue: String
  ): Kleisli[F, (Request[F], Response[F]), Map[String, String]] =
    Kleisli[F, (Request[F], Response[F]), Map[String, String]] { case (request, _) =>
      for {
        bodyMap <- extractBody(request)
      } yield {
        val filterAndMask = new BodyFilter(filter).filter _ andThen new BodyMask(maskKeys, maskValue).mask
        val value         = filterAndMask(bodyMap).asJson.noSpaces

        Map(name -> value)
      }
    }

  def responseBody[F[_]: Sync: JsonDecoder](
      name: String,
      filter: Set[String],
      maskKeys: Set[String],
      maskValue: String = "REDACTED"
  ): Kleisli[F, (Request[F], Response[F]), Map[String, String]] =
    Kleisli[F, (Request[F], Response[F]), Map[String, String]] { case (_, response) =>
      for {
        bodyMap <- extractBody(response)
      } yield {
        val filterAndMask = new BodyFilter(filter).filter _ andThen new BodyMask(maskKeys, maskValue).mask
        val value         = filterAndMask(bodyMap).asJson.noSpaces

        Map(name -> value)
      }
    }

  private def extractBody[F[_]: Sync: JsonDecoder](request: Request[F]): F[Map[String, String]] =
    for {
      body <- request.asJson.attempt
    } yield body.fold(
      _ => Map.empty,
      _.asObject
        .map(_.toMap.view.mapValues(_.noSpaces).toMap)
        .getOrElse(Map.empty)
    )

  private def extractBody[F[_]: Sync: JsonDecoder](response: Response[F]): F[Map[String, String]] =
    for {
      body <- response.asJson.attempt
    } yield body.fold(
      _ => Map.empty,
      _.asObject
        .map(_.toMap.view.mapValues(_.noSpaces).toMap)
        .getOrElse(Map.empty)
    )

}
