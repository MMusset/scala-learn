package com.micahmusset.scalalearn.sttp

import cats.effect.{ ContextShift, ExitCode, IO, IOApp }
import com.micahmusset.scalalearn.sttp.examples.GetRequest
import sttp.client3.SttpBackend
import sttp.client3.armeria.cats.ArmeriaCatsBackend

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val backend: SttpBackend[IO, Any]  = ArmeriaCatsBackend.apply[IO]()

    val program = for {
      _ <- GetRequest.make
    } yield ()

    program.as(ExitCode.Success)
  }
}
