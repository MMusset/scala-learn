package com.micahmusset.scalalearn.sttp.backends.catsSlf4jLoggingBackend.loggerconfig

import sttp.client3.{ Request, Response }

final class DefaultLoggerConfig extends LoggerConfig {
  override def message[T, R](request: Request[T, R], response: Response[T]): String = {
    val path = request.uri.copy(querySegments = List.empty).toString()

    s"[${response.code}] ${request.method} $path"
  }

  override def requestContext[T, R](request: Request[T, R], response: Response[T]): Map[String, String] = {
    val path = request.uri.copy(querySegments = List.empty).toString()

    Map(
      "http.request.method"    -> request.method.method,
      "http.request.uri"       -> request.uri.toString,
      "http.request.uri.path"  -> path,
      "http.request.uri.query" -> ???,
      "http.request.body"      -> ???,
      "http.request.headers"   -> ???
    )
  }

  override def responseContext[T, R](request: Request[T, R], response: Response[T]): Map[String, String] = {
    val responseContext =
      Map(
        "http.response.code"    -> ???,
        "http.response.body"    -> ???,
        "http.response.headers" -> ???
      )
  }

}
