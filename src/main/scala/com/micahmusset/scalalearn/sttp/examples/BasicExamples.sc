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

val request: Request[Either[String, String], Any] = basicRequest
  .body("Hello, world!")
  .post(uri"https://httpbin.org/post?hello=world")

val sendRequest: IO[Response[Either[String, String]]] = request.send(backend)

val program: IO[Unit] = for {
  response <- sendRequest
  _        <- println(response.body).pure[IO]
} yield ()

program.unsafeRunSync()

// =====================================================================================================================
