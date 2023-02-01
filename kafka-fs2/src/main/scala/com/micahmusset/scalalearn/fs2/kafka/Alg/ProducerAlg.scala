package com.micahmusset.scalalearn.fs2.kafka.Alg

trait ProducerAlg[F[_], A, B] {

  def publish(message: A): F[B]

  def publish(message: A, key: String): F[B]

}
