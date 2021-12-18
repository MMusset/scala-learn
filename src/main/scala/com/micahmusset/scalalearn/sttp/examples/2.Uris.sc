import cats.effect.{ ContextShift, IO }
import sttp.client3._
import sttp.client3.armeria.cats.ArmeriaCatsBackend

import scala.concurrent.ExecutionContext

implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

// An implementation of a backend
// This can be swapped out for other backends
implicit val backend: SttpBackend[IO, Any] = ArmeriaCatsBackend.apply[IO]()

// The Uri class is immutable, and can be constructed by hand
// but in many cases the URI interpolator will be easier to use.

val user   = "Mary Smith"
val filter = "programming languages"

uri"http://example.com/$user/skills?filter=$filter".toString ==
  "http://example.com/Mary%20Smith/skills?filter=programming+languages"

uri"http://example.org/${"a/b"}".toString() == "http://example.org/a%2Fb"

// the embedded / is not escaped
uri"http://example.org/${"a"}/${"b"}".toString() == "http://example.org/a/b"

// the embedded : is not escaped
uri"http://${"example.org:8080"}".toString() == "http://example.org:8080"
