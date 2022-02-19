import cats.effect.IO
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder._
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.request.RequestLogConfig
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.configs.response.ResponseLogConfig
import com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.domain.{ BodyConfig, PathConfig, QueryConfig }
import io.circe.literal._
import org.http4s.Response
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.POST
import org.http4s.implicits.http4sLiteralsSyntax

// Raw
//val config1 =
//  HttpConfig[IO](
//    request = RequestLogConfig[IO](
//      message = RequestTransformers.message[IO],
//      context = RequestTransformers.method[IO]("http.request.method") |+|
//        RequestTransformers.uri[IO]("http.request.uri") |+|
//        RequestTransformers.body[IO]("http.request.body")
//    ),
//    response = ResponseLogConfig[IO](
//      message = ResponseTransformers.message[IO],
//      context = ResponseTransformers.body[IO]("http.response.body")
//    )
//  )

// Static
val appName = "test_app"

// Path
val pathConfig = PathConfig(
  maskRegex = "(^|/)[^/]*[0-9]{2,}[^/]*".r,
  maskValue = "_"
)

// Query
val queryConfig = QueryConfig(
  filterKeys = Set[String]("principal"),
  maskKeys = Set[String]("password"),
  maskValue = "REDACTED"
)

// Body
val bodyConfig = BodyConfig(
  filterKeys = Set[String]("principal"),
  maskKeys = Set[String]("password"),
  maskValue = "REDACTED"
)

val config3: HttpConfig[IO] = HttpConfig[IO](
  request = RequestLogConfig
    .empty[IO]()
    .addStatic("app_name", appName)
    .addRequestMethod("http_method")
    .addRequestUri("http_uri", pathConfig, queryConfig)
    .addRequestBody("request_body", bodyConfig),
  response = ResponseLogConfig
    .empty[IO]()
    .addStatic("app_name", appName)
    .addRequestMethod("request_method")
    .addRequestUri("request_uri", pathConfig, queryConfig)
    .addRequestBody("request_body", bodyConfig)
    .addResponseBody("response_body", bodyConfig)
)

val config = config3

val request =
  POST(
    json"""{ "name" : "test", "password" : "password", "principal" : "principal" }""",
    uri"/hello/3f5b07f9-62ea-4802-b971-e4ccbf7a0648/star?foo=bar&password=password&cats=dogs&principal=principal"
  )

val requestContext: Map[String, String] = config.request.context.run(request).unsafeRunSync()
// HashMap(
//   app_name     -> test_app,
//   request_body -> {"name":"\"test\"", "password":"\"REDACTED\""},
//   http_method  -> POST,
//   http_uri     -> /hello/_/star?password=REDACTED&cats=dogs&foo=bar
// )

val response: Response[IO] =
  Response
    .apply[IO]()
    .withEntity(
      json"""{ "cool" : "beans", "name" : "name", "world" : "world", "password" : "password", "principal" : "principal" }"""
    )

val responseContext: Map[String, String] = config.response.context.run(request, response).unsafeRunSync()
// Map(
//   app_name       -> test_app,
//   request_method -> POST,
//   request_uri    -> /hello/_/star?password=REDACTED&cats=dogs&foo=bar,
//   request_body   -> {"name":"\"test\"","password":"\"REDACTED\""},
//   response_body  -> {"cool":"\"beans\"","name":"\"name\"","world":"\"world\"","password":"\"REDACTED\""}
// )
