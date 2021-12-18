import cats.effect.{ContextShift, IO}
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
