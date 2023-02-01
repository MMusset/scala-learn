package com.micahmusset.scalalearn.fs2.kafka

import cats.effect.IO
import fs2.kafka._
import io.circe.{Decoder, Json}

object Program {

  implicit object Example {
    implicit val decoder: Decoder[Example] = Decoder.instance(_.get[Int]("value").map(Example.apply))
  }

  case class Example(value: Int)

  type Input = Example

  implicit def deserializer[A: Decoder]: GenericDeserializer[KeyOrValue, IO, Either[Throwable, A]] = Deserializer.string[IO]
    .map(io.circe.parser.parse)
    .flatMap(_.fold(GenericDeserializer.fail[IO, Json], GenericDeserializer.const[IO, Json]))
    .flatMap(_.as[A].fold(GenericDeserializer.fail[IO, A], GenericDeserializer.const[IO, A]))
    .attempt

  // Producer
  val producerSettings =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("localhost:29092")
      .withProperty("topic.creation.enable", "true")

  val produce =
    KafkaProducer
      .resource(producerSettings)
      .use(_.produceOne(ProducerRecord("topic1", "key", "value")))

  // Job
  def processRecord(record: ConsumerRecord[String, Either[Throwable, Input]]): IO[Unit] = IO.println(record)

  // Consumer
  val consumerSettings =
    ConsumerSettings[IO, String, Either[Throwable, Input]]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:29092")
      .withGroupId("group")

  // Consumer Program
  val stream: fs2.Stream[IO, Unit] =
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo("topic1")
      .records
      .evalMap { committable =>
        processRecord(committable.record)
      }

}
