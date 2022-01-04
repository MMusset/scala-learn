package com.micahmusset.scalalearn.http4s.middleware.logging.v2.builder

import cats.Applicative
import cats.data.Kleisli
import cats.implicits._
import cats.kernel.Monoid

object implicits {

  implicit def monoidMap[F[_]: Applicative, A]: Monoid[Kleisli[F, A, Map[String, String]]] = {
    implicit def underlying[B: Monoid]: Monoid[F[B]] = Applicative.monoid[F, B]

    new Monoid[Kleisli[F, A, Map[String, String]]] {
      override def empty: Kleisli[F, A, Map[String, String]] =
        Monoid[Kleisli[F, A, Map[String, String]]].empty

      override def combine(
          x: Kleisli[F, A, Map[String, String]],
          y: Kleisli[F, A, Map[String, String]]
      ): Kleisli[F, A, Map[String, String]] = (x, y).mapN { case (a, b) => a ++ b }
    }
  }

}
