import cats.effect.{ ContextShift, IO }
import cats.implicits.catsSyntaxApplicativeId
import sttp.client3._
import sttp.client3.armeria.cats.ArmeriaCatsBackend

import scala.concurrent.ExecutionContext

implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

// An implementation of a backend
// This can be swapped out for other backends
implicit val backend: SttpBackend[IO, Any] = ArmeriaCatsBackend.apply[IO]()

// =====================================================================================================================
// Example 1

val request1: Request[Either[String, String], Any] = basicRequest
  .body("Hello, world!")
  .post(uri"https://httpbin.org/post?hello=world")

val sendRequest1: IO[Response[Either[String, String]]] = request1.send(backend)

val program: IO[Unit] = for {
  response <- sendRequest1
  _        <- println(response.body).pure[IO]
} yield ()

program.unsafeRunSync()

// =====================================================================================================================
// Example 2
val world = "world"

val request2 = basicRequest
  .body("Hello, world!")
  .post(uri"https://httpbin.org/post?hello=$world")

// uri"..." does automatic uri interpolation
// uri and query params are sanitised
request2.uri.toString

val program: IO[Unit] = for {
  response <- request2.send(backend)
  _        <- println(response.body).pure[IO]
} yield ()

program.unsafeRunSync()

// =====================================================================================================================
// Example 3
val params = Map("hello" -> "world")

val request3 = basicRequest
  .body("Hello, world!")
  .post(uri"https://httpbin.org/post?$params")

// uri"..." does automatic uri interpolation
// uri and query params are sanitised
request3.uri.toString

val program: IO[Unit] = for {
  response <- request3.send(backend)
  _        <- println(response.body).pure[IO]
} yield ()

program.unsafeRunSync()

// =====================================================================================================================
