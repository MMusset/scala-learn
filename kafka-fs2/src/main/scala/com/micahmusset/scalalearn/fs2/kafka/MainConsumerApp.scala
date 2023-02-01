package com.micahmusset.scalalearn.fs2.kafka

import cats.effect.{ ExitCode, IO, IOApp }
import com.micahmusset.scalalearn.fs2.kafka.Impl.{ Jobs, KafkaConsumerImpl, ProducerImpl1 }
import com.micahmusset.scalalearn.fs2.kafka.Program.producerSettings
import fs2.kafka.KafkaProducer

object MainConsumerApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val resources =
      for {
        producer1 <- KafkaProducer.resource(producerSettings)
      } yield producer1

    val program =
      resources.use { case (producer1: KafkaProducer.Metrics[IO, String, String]) =>
        // Producers
        val topic1Producer = new ProducerImpl1(producer1, "topic1")

        // Consumers
        val consumerJobStream1 = KafkaConsumerImpl.make(topic = "topic1", job = Jobs.job1)
        val consumerJobStream2 = KafkaConsumerImpl.make(topic = "topic2", job = Jobs.job2)
        val consumerJobStream3 = KafkaConsumerImpl.make(topic = "topic3", job = Jobs.job3)

        val job4               = Jobs.job4(topic1Producer)
        val consumerJobStream4 = KafkaConsumerImpl.make(topic = "topic4", job = job4)

        val streams =
          List(consumerJobStream1, consumerJobStream2, consumerJobStream3, consumerJobStream4)
            .fold(fs2.Stream.empty)(_ mergeHaltBoth _)

        streams.compile.drain
      }

    program.as(ExitCode.Success)
  }
}
