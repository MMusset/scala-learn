package com.micahmusset.scalalearn.http4s.middleware.logging.v1.builder

object Domain {

  final case class Context(value: Map[String, String] = Map.empty)
  final case class Message(value: String = "")

}
