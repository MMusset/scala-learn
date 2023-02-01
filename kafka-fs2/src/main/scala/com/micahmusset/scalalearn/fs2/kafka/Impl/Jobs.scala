package com.micahmusset.scalalearn.fs2.kafka.Impl

import cats.effect.IO
import cats.effect.kernel.Sync
import com.micahmusset.scalalearn.fs2.kafka.Alg.{JobAlg, ProducerAlg}
import com.micahmusset.scalalearn.fs2.kafka.Program.{Example, Input}
import fs2.kafka.ProducerResult

object Jobs {

  val job1 = new JobAlg[IO, Input, Unit] {
    override def run(message: Input): IO[Unit] = Sync[IO].delay(println("Its job 1"))
  }

  val job2 = new JobAlg[IO, Input, Unit] {
    override def run(message: Input): IO[Unit] = Sync[IO].delay(println("Its job 2"))
  }

  val job3 = new JobAlg[IO, Input, Unit] {
    override def run(message: Input): IO[Unit] = Sync[IO].delay(println("Its job 3"))
  }

  def job4(producer1: ProducerAlg[IO, Input, ProducerResult[String, String]]) =
    new JobAlg[IO, Input, Unit] {
      override def run(message: Input): IO[Unit] =
        for {
          _               <- Sync[IO].delay(println("Its job 4"))
          producer1Message = Example(1)
          _               <- producer1.publish(producer1Message)
        } yield ()
    }
}
