package com.micahmusset.scalalearn.fs2

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    // Something

    IO.pure(1).as(ExitCode.Success)
  }
}
