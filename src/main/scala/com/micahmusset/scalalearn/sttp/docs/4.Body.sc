import cats.effect.{ ContextShift, IO }
import sttp.client3._
import sttp.client3.armeria.cats.ArmeriaCatsBackend
import sttp.model.MediaType

import scala.concurrent.ExecutionContext

implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
implicit val backend: SttpBackend[IO, Any]  = ArmeriaCatsBackend.apply[IO]()

// =====================================================================================================================
// Text data

basicRequest.body("Hello, world!")
basicRequest.body("Hello, world!", "utf-8")

// =====================================================================================================================
// Form Data

// If you set the body as a Map[String, String] or Seq[(String, String)],
// it will be encoded as form-data.
// The content type will default to application/x-www-form-urlencoded

val request = basicRequest.body(Map("k1" -> "v1"))
request.headers
//Vector(..., Content-Type: application/x-www-form-urlencoded, Content-Length: 5)

basicRequest.body(Map("k1" -> "v1"), "utf-8")
basicRequest.body("k1" -> "v1", "k2" -> "v2")
basicRequest.body(Seq("k1" -> "v1", "k2" -> "v2"), "utf-8")

// =====================================================================================================================
// Custom Body Serializers

// It is also possible to set custom types as request bodies,
// as long as there’s an implicit BodySerializer[B] value in scope
case class Person(name: String, surname: String, age: Int)

// for this example, assuming names/surnames can't contain commas
implicit val personSerializer: BodySerializer[Person] = { p: Person =>
  val serialized = s"${p.name},${p.surname},${p.age}"
  StringBody(serialized, "UTF-8", MediaType.TextCsv)
}

val request1 = basicRequest
  .body(Person("mary", "smith", 67))
  .post(uri"https://httpbin.org/post?hello=world")

request1.body.toString
request1.toCurl

//  curl \
//  --request POST \
//  --url 'https://httpbin.org/post?hello=world' \
//  --header 'Content-Type: text/csv; charset=UTF-8' \
//  --data 'mary,smith,67' \
//  --location \
//  --max-redirs 32
