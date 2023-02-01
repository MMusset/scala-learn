package com.micahmusset.scalalearn.fs2.kafka

import cats.effect.{ ExitCode, IO, IOApp }
import com.micahmusset.scalalearn.fs2.kafka.Impl.{ HttpEndpoint, ProducerImpl }
import com.micahmusset.scalalearn.fs2.kafka.Program.producerSettings
import fs2.kafka.{ KafkaProducer, ProducerResult }

object MainHttpApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val resources =
      for {
        producer1 <- KafkaProducer.resource(producerSettings)
      } yield producer1

    val program =
      resources.use { case (producer1: KafkaProducer.Metrics[IO, String, String]) =>
        // Producer
        val topic1Producer: ProducerImpl[IO] = new ProducerImpl(producer1, "topic1")

        // Endpoints
        val endpoint1 = new HttpEndpoint[IO, ProducerResult[String, String]](topic1Producer)

        val response = endpoint1.executePostRequest

        response
      }

    program.as(ExitCode.Success)
  }
}
