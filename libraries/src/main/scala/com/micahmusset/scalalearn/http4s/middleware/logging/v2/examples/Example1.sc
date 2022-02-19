import cats.data.Kleisli
import cats.effect.{IO, Sync}
import cats.implicits._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.request.{RequestLogConfig, RequestTransformers}
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.response.{ResponseLogConfig, ResponseTransformers}
import io.circe.literal._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.POST
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Request, Response}

import scala.util.matching.Regex

val request: Request[IO] = POST(json"""{ "test1" : "value1" }""", uri"/hello")

val methodTest: Kleisli[IO, Request[IO], Map[String, String]] = RequestTransformers.method[IO]("method")
val uriTest: Kleisli[IO, Request[IO], Map[String, String]]    = ((name: String, queryFilter: Set[String], pathMaskKeys: Set[String], pathMaskValue: String, queryMaskRegex: Regex, queryMaskRegexValue: String) => RequestTransformers.uri(name, queryMaskRegex, queryMaskRegexValue, pathMaskKeys, pathMaskValue, queryFilter))[IO]("uri")
val bodyTest: Kleisli[IO, Request[IO], Map[String, String]]   = RequestTransformers.body[IO]("body")

val response: Response[IO] = Response.apply[IO]().withEntity(json"""{ "test2" : "value2" }""")

val test = bodyTest

val program: IO[Map[String, String]] =
  for {
    context <- test.run(request)
    _       <- Sync[IO].delay(println(context))
  } yield context

program.unsafeRunSync()

val config =
  HttpConfig[IO](
    request = RequestLogConfig[IO](
      message = RequestTransformers.message[IO],
      context = RequestTransformers.method[IO]("http.request.method") |+|
        ((name: String, queryFilter: Set[String], pathMaskKeys: Set[String], pathMaskValue: String, queryMaskRegex: Regex, queryMaskRegexValue: String) => RequestTransformers.uri(name, queryMaskRegex, queryMaskRegexValue, pathMaskKeys, pathMaskValue, queryFilter))[IO]("http.request.uri") |+|
        RequestTransformers.body[IO]("http.request.body")
    ),
    response = ResponseLogConfig[IO](
      message = ResponseTransformers.message[IO],
      context = ResponseTransformers.body[IO]("http.response.body")
    )
  )

val requestContext = config.request.context.run(request)
requestContext.unsafeRunSync()

val responseContext = config.response.context.run(response)
responseContext.unsafeRunSync()

val combinedContext = requestContext |+| responseContext
combinedContext.unsafeRunSync()

val requestMessage = config.request.message.run(request)
requestMessage.unsafeRunSync()

val responseMessage = config.response.message.run(request, response)
responseMessage.unsafeRunSync()
