import cats.data.{ Kleisli, OptionT }
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

case class Tweet(id: Int, message: String)

implicit def tweetEncoder: EntityEncoder[IO, Tweet]       = ???
implicit def tweetsEncoder: EntityEncoder[IO, Seq[Tweet]] = ???

def getTweet(tweetId: Int): IO[Tweet]  = ???
def getPopularTweets(): IO[Seq[Tweet]] = ???

val tweetService: HttpRoutes[IO] =
  HttpRoutes.of[IO] { case GET -> Root / "tweets" / IntVar(tweetId) =>
    val result: IO[Response[IO]] = getTweet(tweetId).flatMap(tweet => Ok(tweet))
    result
  }

// =====================================================================================================================
// Running your service as an App

import org.http4s.HttpRoutes
import org.http4s.implicits._

//object Main extends IOApp {
//
//  val helloWorldService = HttpRoutes
//    .of[IO] { case GET -> Root / "hello" / name =>
//      Ok(s"Hello, $name.")
//    }
//    .orNotFound
//
//  def run(args: List[String]): IO[ExitCode] =
//    BlazeServerBuilder[IO](global)
//      .bindHttp(8080, "localhost")
//      .withHttpApp(helloWorldService)
//      .serve
//      .compile
//      .drain
//      .as(ExitCode.Success)
//
//}

// =====================================================================================================================
// The http4s DSL

// HttpRoutes[F] is just a type alias for Kleisli[OptionT[F, *], Request[F], Response[F]]

// HttpRoutes[IO]
// Kleisli[OptionT[IO, *], Request[IO], Response[IO]]

// The central concept of http4s-dsl is pattern matching.
// An HttpRoutes[F] is declared as a simple series of case statements.
// Each case statement attempts to match and optionally extract from an incoming Request[F].

val httpRoutes = HttpRoutes.of[IO] { case _ =>
  IO(Response(Status.Ok))
}

// =====================================================================================================================
// Testing the Service

// We don’t need a server to test our route

val request: Request[IO] = Request[IO](Method.GET, uri"/")
// request: Request[IO] = (
//   GET,
//   Uri(None, None, /, , None),
//   HttpVersion(1, 1),
//   Headers(),
//   Stream(..),
//   org.typelevel.vault.Vault@2101bec0
// )

val requestOrNotFound: Kleisli[IO, Request[IO], Response[IO]] = httpRoutes.orNotFound

val responseProgram: IO[Response[IO]] = requestOrNotFound.run(request)
// response: IO[Response[IO]] = Map(
//   FlatMap(Pure(()), cats.syntax.FlatMapOps$$$Lambda$17815/646004363@bc984c9),
//   cats.data.OptionT$$Lambda$17823/1577214905@34d067
// )

val response: Response[IO] = responseProgram.unsafeRunSync()
// response: Response[IO] = (
//   Status(200),
//   HttpVersion(1, 1),
//   Headers(),
//   Stream(..),
//   org.typelevel.vault.Vault@388f5614
// )

val body: EntityBody[IO] = response.body

// =====================================================================================================================
// Request

val httpRoutes1 =
  HttpRoutes.of[IO] { case _ =>
    IO(Response(Status.Ok))
  }

val request1 = Request[IO](Method.GET, uri"/")
val response =
  httpRoutes1.orNotFound
    .run(request1)
    .unsafeRunSync()

// =====================================================================================================================
// Response - Status Codes

val okIo: IO[Response[IO]] = Ok()
// okIo: IO[Response[IO]] = Pure(
//   (
//     Status(200),
//     HttpVersion(1, 1),
//     Headers(Content-Length: 0),
//     Stream(..),
//     org.typelevel.vault.Vault@14d4087b
//   )
// )

val ok: Response[IO] = okIo.unsafeRunSync()
// ok: Response[IO] = (
//   Status(200),
//   HttpVersion(1, 1),
//   Headers(Content-Length: 0),
//   Stream(..),
//   org.typelevel.vault.Vault@14d4087b
// )

// =====================================================================================================================
// Responding with a body
