package com.micahmusset.scalalearn.fs2.tutorials.rockthejvm

import cats.effect._
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Model.Actor
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Setup.jlActors
import fs2._

import scala.util.Random

object ErrorHandling {

  // What if pulling a value from a stream fails with an exception?
  def save(actor: Actor): IO[Int] = IO {
    println(s"Saving actor: $actor")
    if (Random.nextInt() % 2 == 0) {
      throw new RuntimeException("Something went wrong during the communication with the persistence layer")
    }
    println(s"Saved.")
    actor.id
  }

  val savedJlActors: Stream[IO, Int] = jlActors.evalMap(ErrorHandling.save)
  // If we run the above stream, we will likely see the following output:
  // java.lang.RuntimeException: Something went wrong during the communication with the persistence layer
  // the stream is interrupted by the exception
  // every time an exception occurs during pulling elements from a stream, the stream execution ends.

  val errorHandledSavedJlActors: Stream[IO, AnyVal] =
    savedJlActors.handleErrorWith(error => Stream.eval(IO.println(s"Error: $error")))
  // In the above example, we react to an error by returning a stream that prints the error to the console.

  val attemptedSavedJlActors: Stream[IO, Either[Throwable, Int]] = savedJlActors.attempt
  attemptedSavedJlActors.evalMap {
    case Left(error) => IO.println(s"Error: $error")
    case Right(id) => IO.println(s"Saved actor with id: $id")
  }
}
