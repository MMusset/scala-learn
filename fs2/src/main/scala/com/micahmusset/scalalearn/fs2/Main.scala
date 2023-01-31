package com.micahmusset.scalalearn.fs2

import cats.effect.{ ExitCode, IO, IOApp }
import com.micahmusset.scalalearn.fs2.examples.concurency.GroupTaskProgram

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    // Something

    GroupTaskProgram.make.as(ExitCode.Success)
  //    IO.pure(1).as(ExitCode.Success)
}
