package com.micahmusset.scalalearn.fs2.kafka.Impl

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatten
import com.micahmusset.scalalearn.fs2.kafka.Alg.ProducerAlg
import com.micahmusset.scalalearn.fs2.kafka.Program.Input
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerResult, ProducerSettings}

object ProducerImpl {
  val producerSettings =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("localhost:29092")
      .withProperty("topic.creation.enable", "true")
}

class ProducerImpl[F[_]: Sync](producer: KafkaProducer.Metrics[F, String, String], topic: String)
    extends ProducerAlg[F, String, ProducerResult[String, String]] {

  override def publish(message: String): F[ProducerResult[String, String]] =
    producer.produceOne(ProducerRecord(topic, "", message)).flatten

  override def publish(message: String, key: String): F[ProducerResult[String, String]] =
    producer.produceOne(ProducerRecord(topic, key, message)).flatten

}

class ProducerImpl1[F[_]: Sync](producer: KafkaProducer.Metrics[F, String, String], topic: String)
  extends ProducerAlg[F, Input, ProducerResult[String, String]] {

  override def publish(message: Input): F[ProducerResult[String, String]] =
    producer.produceOne(topic, "", message.value.toString).flatten

  override def publish(message: Input, key: String): F[ProducerResult[String, String]] =
    producer.produceOne(ProducerRecord(topic, key, message.value.toString)).flatten

}
