import cats.data.OptionT
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax

// =====================================================================================================================
// Hello World
val helloWorldService: HttpRoutes[IO] =
  HttpRoutes.of[IO] { case GET -> Root / "hello" / name =>
    Ok(s"Hello, $name.")
  }

val request: Request[IO]                = Request().withUri(uri"/hello/world")
val response: OptionT[IO, Response[IO]] = helloWorldService.run(request)
val program: IO[Option[Response[IO]]]   = response.value

program.unsafeRunSync()

// =====================================================================================================================
// Returning content in the response

