import cats.effect._
import cats.implicits.toFunctorOps
import io.circe._
import io.circe.literal._
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.implicits._

// =====================================================================================================================
// Json Examples

val json1 = json"""{"name":"Alice"}"""
val json2 =
  json"""
    {
      "name": "Alice",
      "company": {
        "name": "Samsung"
      }
    }
  """

// =====================================================================================================================
// Request Params to Map[String, String]
// Request[IO] => IO[Map[String, String]]

val program1: IO[Map[String, String]] =
  for {
    params <- POST(json2, uri"/hello").asJson
  } yield params.asObject
    .map(_.toMap.view.mapValues(_.noSpaces).toMap)
    .getOrElse(Map.empty)

program1.unsafeRunSync()

val program2: IO[Map[String, String]] = for {
  params <- POST(json2, uri"/hello").asJson
} yield params.as[Map[String, String]].getOrElse(Map.empty)

program2.unsafeRunSync()

// =====================================================================================================================
// Response Body Json to Map[String, String]
// IO[Response[IO]] => IO[Map[String, String]]

val program3: IO[Map[String, String]] =
  for {
    response <- Ok(json2)
    body     <- response.asJson
  } yield body.asObject
    .map(_.toMap.view.mapValues(_.noSpaces).toMap)
    .getOrElse(Map.empty)

program3.unsafeRunSync()

import org.http4s.circe.CirceEntityCodec._

//val program4: IO[Map[String, String]] =
//  for {
//    response <- Ok(json2)
//    body     <- response.as[Map[String, String]]
//  } yield body
//
//program4.unsafeRunSync()


Map("ewrew" -> "werwe").asJson
