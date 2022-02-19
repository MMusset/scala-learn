package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder.utils

import org.http4s.{ Query, Uri }

import scala.util.matching.Regex

trait Mask[A] {
  def mask(value: A): A
}

final class PathMask(regex: Regex, maskValue: String) extends Mask[Uri] {
  override def mask(uri: Uri): Uri =
    uri.copy(path = Uri.Path.unsafeFromString(regex.replaceAllIn(uri.path.renderString, "$1" + maskValue)))
}

final class QueryMask(keys: Set[String] = Set().empty, maskValue: String) extends Mask[Uri] {
  override def mask(uri: Uri): Uri =
    uri.copy(query = Query.fromMap(uri.query.multiParams.view.map { case (k, v) =>
      if (keys.contains(k)) (k, Seq(maskValue)) else (k, v)
    }.toMap))
}

final class BodyMask(keys: Set[String] = Set().empty, maskValue: String) extends Mask[Map[String, String]] {
  override def mask(body: Map[String, String]): Map[String, String] =
    body.view.map { case (k, v) => if (keys.contains(k)) (k, maskValue) else (k, v) }.toMap
}
