package com.micahmusset.scalalearn.fs2.kafka.Impl

import cats.effect.{ IO, Sync }
import com.micahmusset.scalalearn.fs2.kafka.Alg.JobAlg
import com.micahmusset.scalalearn.fs2.kafka.Program.Input
import fs2.kafka._

object KafkaConsumerImpl {

  val consumerSettings: ConsumerSettings[IO, String, Either[Throwable, Input]] =
    ConsumerSettings[IO, String, Either[Throwable, Input]]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:29092")
      .withGroupId("group")

  def make(topic: String, job: JobAlg[IO, Input, Unit]): fs2.Stream[IO, Unit] =
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic)
      .records
      .evalMap { committable: CommittableConsumerRecord[IO, String, Either[Throwable, Input]] =>
        committable.record.value match {
          case Left(throwable) => Sync[IO].delay(println(s"Error with message, $throwable")) *> Sync[IO].unit
          case Right(input)    => job.run(input)
        }
      }

}
