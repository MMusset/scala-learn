package com.micahmusset.scalalearn.sttp.backends.catsSlf4jLoggingBackend.loggerconfig

import sttp.client3.{Request, Response}

trait LoggerConfig {
  def requestMessage[T, R](request: Request[T, R]): String
  def responseMessage[T, R](request: Request[T, R], response: Response[T]): String

  def requestContext[T, R](request: Request[T, R]): Map[String, String]
  def responseContext[T](response: Response[T]): Map[String, String]
}
