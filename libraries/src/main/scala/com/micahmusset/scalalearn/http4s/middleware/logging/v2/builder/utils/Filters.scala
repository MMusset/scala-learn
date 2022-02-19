package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.utils

import org.http4s.{ Query, Uri }

trait Filter[A] {
  def filter(value: A): A
}

final class QueryFilter(keys: Set[String] = Set().empty) extends Filter[Uri] {
  override def filter(uri: Uri): Uri =
    uri.copy(query = Query.fromMap(uri.query.multiParams.view.filterKeys(!keys.contains(_)).toMap))
}

final class BodyFilter(keys: Set[String] = Set().empty) extends Filter[Map[String, String]] {
  override def filter(body: Map[String, String]): Map[String, String] =
    body.view.filterKeys(!keys.contains(_)).toMap
}
