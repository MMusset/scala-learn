package com.micahmusset.scalalearn.fs2.kafka.Alg

trait JobAlg[F[_], A, B] {

  def run(message: A): F[B]

}
