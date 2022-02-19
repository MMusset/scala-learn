package com.micahmusset.scalalearn.fs2.tutorials.rockthejvm

import cats.effect.IO
import cats.effect.std.Queue
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Model.Actor
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Setup.{ jlActors, liftedJlActors1, spiderMen }
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Transformations.{ avengersActors, toConsole }
import fs2._

import scala.concurrent.duration.DurationInt

// https://blog.rockthejvm.com/fs2/#7-using-concurrency-in-streams
object Concurrency {
  // .merge
  // The execution halts when both streams have halted.

  val concurrentJlActors: Stream[IO, Actor]       = liftedJlActors1.evalMap(actor =>
    IO {
      Thread.sleep(400)
      actor
    }
  )
  val liftedAvengersActors: Stream[IO, Actor]     = avengersActors.covary[IO]
  val concurrentAvengersActors: Stream[IO, Actor] = liftedAvengersActors.evalMap(actor =>
    IO {
      Thread.sleep(200)
      actor
    }
  )
  val mergedHeroesActors: Stream[IO, Unit]        =
    concurrentJlActors.merge(concurrentAvengersActors).through(toConsole)

  // Actor(7,Scarlett,Johansson)
  // Actor(0,Henry,Cavill)
  // Actor(8,Robert,Downey Jr.)
  // Actor(9,Chris,Evans)
  // Actor(1,Gal,Godot)
  // Actor(10,Mark,Ruffalo)
  // Actor(11,Chris,Hemsworth)
  // Actor(2,Ezra,Miller)
  // Actor(12,Jeremy,Renner)
  // Actor(3,Ben,Fisher)
  // Actor(4,Ray,Hardy)
  // Actor(5,Jason,Momoa)

  // Once the Avengers actors are finished, the JLA actors fulfill the rest of the stream.
  // If we don’t care about the results of the second stream running concurrently,
  // we can use the concurrently method instead.

  // If we don’t care about the results of the second stream running concurrently, we can use the concurrently method instead.
  // An everyday use case for this method is implementing a producer-consumer pattern.

  val queue: IO[Queue[IO, Actor]] = Queue.bounded[IO, Actor](10)

  val concurrentlyStreams: Stream[IO, Unit] = Stream.eval(queue).flatMap { q =>
    val producer: Stream[IO, Unit] =
      liftedJlActors1
        .evalTap(actor => IO.println(s"[${Thread.currentThread().getName}] produced $actor"))
        .evalMap(q.offer)
        .metered(1.second)

    val consumer: Stream[IO, Unit] =
      Stream
        .fromQueueUnterminated(q)
        .evalMap(actor => IO.println(s"[${Thread.currentThread().getName}] consumed $actor"))

    producer.concurrently(consumer)
  }

  // [io-compute-2] produced Actor(0,Henry,Cavill)
  // [io-compute-3] consumed Actor(0,Henry,Cavill)
  // [io-compute-2] produced Actor(1,Gal,Godot)
  // [io-compute-0] consumed Actor(1,Gal,Godot)
  // [io-compute-0] produced Actor(2,Ezra,Miller)
  // [io-compute-3] consumed Actor(2,Ezra,Miller)
  // [io-compute-1] produced Actor(3,Ben,Fisher)
  // [io-compute-3] consumed Actor(3,Ben,Fisher)
  // [io-compute-1] produced Actor(4,Ray,Hardy)
  // [io-compute-0] consumed Actor(4,Ray,Hardy)
  // [io-compute-1] produced Actor(5,Jason,Momoa)
  // [io-compute-2] consumed Actor(5,Jason,Momoa)

  // A critical feature of running two streams through the concurrently method
  // is that the second stream halts when the first stream is finished.

  //  Moreover, we can run a set of streams concurrently, deciding the degree of concurrency a priori using streams.
  //  The method parJoin does precisely this. Contrary to the concurrently method, the results of the streams are merged in a single stream, and no assumption is made on streams’ termination.

  val toConsoleWithThread: Pipe[IO, Actor, Unit] = in =>
    in.evalMap(actor => IO.println(s"[${Thread.currentThread().getName}] consumed $actor"))

  val parJoinedActors: Stream[IO, Unit] =
    Stream(
      jlActors.through(toConsoleWithThread),
      avengersActors.through(toConsoleWithThread),
      spiderMen.through(toConsoleWithThread)
    ).parJoin(4)

  // [io-compute-3] consumed Actor(7,Scarlett,Johansson)
  // [io-compute-1] consumed Actor(0,Henry,Cavill)
  // [io-compute-0] consumed Actor(13,Tom,Holland)
  // [io-compute-2] consumed Actor(8,Robert,Downey Jr.)
  // [io-compute-1] consumed Actor(1,Gal,Godot)
  // [io-compute-2] consumed Actor(9,Chris,Evans)
  // [io-compute-1] consumed Actor(14,Tobey,Maguire)
  // [io-compute-1] consumed Actor(10,Mark,Ruffalo)
  // [io-compute-2] consumed Actor(2,Ezra,Miller)
  // [io-compute-2] consumed Actor(15,Andrew,Garfield)
  // [io-compute-0] consumed Actor(3,Ben,Fisher)
  // [io-compute-1] consumed Actor(11,Chris,Hemsworth)
  // [io-compute-3] consumed Actor(12,Jeremy,Renner)
  // [io-compute-0] consumed Actor(4,Ray,Hardy)
  // [io-compute-2] consumed Actor(5,Jason,Momoa)

}
