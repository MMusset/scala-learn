import cats.effect.{ContextShift, IO}
import cats.implicits.catsSyntaxApplicativeId
import sttp.client3._
import sttp.client3.armeria.cats.ArmeriaCatsBackend
import sttp.model.Header

import scala.concurrent.ExecutionContext

implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
implicit val backend: SttpBackend[IO, Any]  = ArmeriaCatsBackend.apply[IO]()

// =====================================================================================================================
// Initial requests

// Contains
// Headers = Accept-Encoding: gzip, deflate
val basic: RequestT[Empty, Either[String, String], Any] = basicRequest
basic.headers

// Headers = None
val empty: RequestT[Empty, Either[String, String], Any] = emptyRequest
empty.headers

// Debugging requests
basicRequest.get(uri"http://httpbin.org/ip").toCurl
//curl \
//  --request GET \
//  --url 'http://httpbin.org/ip' \
//  --location \
//  --max-redirs 32

// =====================================================================================================================
// Headers

basicRequest.header("User-Agent", "myapp")
basicRequest.header(Header("k1", "v1"), replaceExisting = false)
basicRequest.header("k2", "v2")
basicRequest.header("k3", "v3", replaceExisting = true)
basicRequest.headers(Map("k4" -> "v4", "k5" -> "v5"))
basicRequest.headers(Header("k9", "v9"), Header("k10", "v10"), Header("k11", "v11"))

// Common Headers
// For some common headers, dedicated methods are provided:

basicRequest.contentType("application/json")
basicRequest.contentType("application/json", "iso-8859-1")
basicRequest.contentLength(128)
basicRequest.acceptEncoding("gzip, deflate")

// =====================================================================================================================
// Basic authentication

// Username and password are encoded using Base64:
val username = "mary"
val password = "p@assword"

val response = basicRequest.auth.basic(username, password)
response.headers
// Vector(..., Authorization: Basic bWFyeTpwQGFzc3dvcmQ=)

// A bearer token:
val token = "zMDjRfl76ZC9Ub0wnz4XsNiRVBChTYbJcE3F"

val response1 = basicRequest.auth.bearer(token)
response.headers
// Vector(..., Authorization: Basic bWFyeTpwQGFzc3dvcmQ=)

// =====================================================================================================================
// Digest authentication

val authBackend = new DigestAuthenticationBackend(backend)

val secureDigestRequest = basicRequest.auth
  .digest(username, password)
  .get(uri"http://httpbin.org/ip")
  .send(authBackend)

val program1: IO[Unit] = for {
  response <- secureDigestRequest
  _        <- println(response.body).pure[IO]
} yield ()

program1.unsafeRunSync()
