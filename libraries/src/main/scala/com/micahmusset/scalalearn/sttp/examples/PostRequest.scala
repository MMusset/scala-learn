package com.micahmusset.scalalearn.sttp.examples

import cats.effect.{ ContextShift, IO }
import cats.implicits._
import sttp.client3._

object PostRequest {

  final case class Action(value: String)

  def make(implicit contextShift: ContextShift[IO], backend: SttpBackend[IO, Any]): IO[Unit] = {
    val requiredParams = Map(
      "hello" -> "world",
      "foo"   -> "bar"
    )

    val someOptionalParams = Map(
      "sleep" -> Some(Action("bed")).map(_.value),
      "eat"   -> Some(Action("truffle")).map(_.value)
    )

    val noneOptionalParams = Map(
      "sleep" -> None.map(_.toString),
      "eat"   -> None.map(_.toString)
    )

    val paramsWithSomeOptionalParams = requiredParams ++ someOptionalParams

    val paramsWithNoneOptionalParams = requiredParams ++ noneOptionalParams

    val uri1 =
      uri"https://httpbin.org/post"
        .addParams(requiredParams)
        .addParam("sleep", Some("bed"))
        .addParam("eat", Some("truffle"))

    //  {
    //    "eat": "truffle",
    //    "foo": "bar",
    //    "hello": "world",
    //    "sleep": "bed"
    //  }

    val uri2 =
      uri"https://httpbin.org/post"
        .addParams(requiredParams)
        .addParam("sleep", None)
        .addParam("eat", None)

    //  {
    //    "foo": "bar",
    //    "hello": "world"
    //  }

    val uri3 =
      uri"https://httpbin.org/post"
        .addParam("hello", "world")
        .addParam("foo", "bar")
        .addParam("sleep", Some("bed"))
        .addParam("eat", Some("truffle"))

    //  {
    //    "eat": "truffle",
    //    "foo": "bar",
    //    "hello": "world",
    //    "sleep": "bed"
    //  }

    val uri4 =
      uri"https://httpbin.org/post"
        .addParam("hello", "world")
        .addParam("foo", "bar")
        .addParam("sleep", None)
        .addParam("eat", None)

    //  {
    //    "foo": "bar",
    //    "hello": "world"
    //  }

    val uri5 = uri"https://httpbin.org/post?$paramsWithSomeOptionalParams"

    //  {
    //    "eat": "truffle",
    //    "foo": "bar",
    //    "hello": "world",
    //    "sleep": "bed"
    //  }

    val uri6 = uri"https://httpbin.org/post?$paramsWithNoneOptionalParams"

    //  {
    //    "foo": "bar",
    //    "hello": "world"
    //  }

    val request = basicRequest
      .body("Hello, world!")
      .post(uri5)

    for {
      response <- request.send(backend)
      _        <- println(response.body).pure[IO]
    } yield ()
  }
}
