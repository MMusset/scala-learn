package com.micahmusset.scalalearn.sttp.examples

import cats.effect.{ ContextShift, IO }
import cats.implicits._
import sttp.client3._

object GetRequest {

  def make(implicit contextShift: ContextShift[IO], backend: SttpBackend[IO, Any]): IO[Unit] = {
    val params = Map(
      "hello" -> "world",
      "foo"   -> "bar"
    )

    val request = basicRequest
      .body("Hello, world!")
      .post(uri"https://httpbin.org/post?$params")

    for {
      response <- request.send(backend)
      _        <- println(response.body).pure[IO]
    } yield ()
  }
}
