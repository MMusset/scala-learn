package com.micahmusset.scalalearn.fs2.kafka.Impl

import cats.effect._
import com.micahmusset.scalalearn.fs2.kafka.Alg.ProducerAlg

final class HttpEndpoint[F[_]: Sync, A](topic1Producer: ProducerAlg[F, String, A]) {

  def executePostRequest: F[A] = {
    val message = "I am a message!"
    val key     = "123"

    topic1Producer.publish(message, key)
  }
}
