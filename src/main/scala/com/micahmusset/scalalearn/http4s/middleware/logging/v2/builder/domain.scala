package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder

import scala.util.matching.Regex

object domain {

  final case class PathConfig(maskRegex: Regex, maskValue: String)

  final case class QueryConfig(filterKeys: Set[String], maskKeys: Set[String], maskValue: String)

  final case class BodyConfig(filterKeys: Set[String], maskKeys: Set[String], maskValue: String)

}
