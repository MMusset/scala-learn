import cats.effect._
import io.circe._
import io.circe.literal._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

def hello(name: String): Json =
  json"""{"hello": $name}"""

val greetingJson = hello("world")
// greeting: Json = JObject(object[hello -> "world"])

import org.http4s.circe._

val response: Response[IO] = Ok(greetingJson).unsafeRunSync()
// res1: Response[IO] = (
//   Status(200),
//   HttpVersion(1, 1),
//   Headers(Content-Type: application/json, Content-Length: 17),
//   Stream(..),
//   org.typelevel.vault.Vault@57e1173a
// )

import org.http4s.client.dsl.io._

val response2: Request[IO] = POST(json"""{"name": "Alice"}""", uri"/hello")
// res2: Request[IO] = (
//   POST,
//   Uri(None, None, /hello, , None),
//   HttpVersion(1, 1),
//   Headers(Content-Type: application/json, Content-Length: 16),
//   Stream(..),
//   org.typelevel.vault.Vault@6ee5092e
// )

val body: EntityBody[IO] = response2.body

val headers: Headers = response2.headers

// =====================================================================================================================
// Encoding case classes as JSON

case class Hello(name: String)
case class User(name: String)

// =====================================================================================================================
// Receiving raw JSON

val request3: Request[IO] = POST("""{"name":"Bob"}""", uri"/hello")
val request3Json          = request3.as[Json].unsafeRunSync()
// res10: Json = JObject(object[name -> "Bob"])

val response3: IO[Response[IO]] = Ok("""{"name":"Alice"}""")
val response3Json: Json         = response3.flatMap(_.as[Json]).unsafeRunSync()
// res9: Json = JObject(object[name -> "Alice"])

val response4: Response[IO]#Self                   = Response.apply[IO]().withEntity(json"""{"name":"Alice"}""")
val response4Body: EntityBody[IO]                  = response4.body
val response4Headers: Headers                      = response4.headers
val response4JsonIO: IO[Json]                      = response4.as[Json]
val response4Json: Json                            = response4JsonIO.unsafeRunSync()
val response4BodyMap1: Option[Map[String, Json]]   = response4Json.asObject.map(_.toMap)
val response4BodyMap2: Option[Map[String, String]] = response4Json.asObject.map(_.toMap.view.mapValues(_.noSpaces).toMap)

// =====================================================================================================================
// Request Params to Map[String, String]
// Request[IO] => IO[Map[String, String]]

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

// program1
val program1: IO[Map[String, String]] =
  for {
    params <- POST(json2, uri"/hello").asJson
  } yield params.asObject
    .map(_.toMap.view.mapValues(_.noSpaces).toMap)
    .getOrElse(Map.empty)

program1.unsafeRunSync()

// program2
val program2: IO[Map[String, String]] = for {
  params <- POST(json2, uri"/hello").asJson
} yield params.as[Map[String, String]].getOrElse(Map.empty)

program2.unsafeRunSync()

// =====================================================================================================================
// Response Body Json to Map[String, String]
// IO[Response[IO]] => IO[Map[String, String]]

val json3 = json"""{"name":"Alice"}"""
val json4 =
  json"""
    {
      "name": "Alice",
      "company": {
        "name": "Samsung"
      }
    }
  """

// program3
val program3: IO[Map[String, String]] =
  for {
    response <- Ok(json3)
    body     <- response.as[Json]
  } yield body.asObject
    .map(_.toMap.view.mapValues(_.noSpaces).toMap)
    .getOrElse(Map.empty)

program3.unsafeRunSync()

import org.http4s.circe.CirceEntityCodec._

// program4
val program4: IO[Map[String, String]] =
  for {
    response <- Ok(json3)
    body     <- response.as[Map[String, String]]
  } yield body

program4.unsafeRunSync()
