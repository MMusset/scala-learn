package com.micahmusset.scalalearn.fs2.tutorials.rockthejvm

import cats.effect.IO
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Data._
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Model._
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Setup.jlActors
import fs2._

object Transformations {

  // Inside, every stream is made of chunks.
  // A Chunk[O] is a finite sequence of stream elements of type O stored inside a structure
  val avengersActors: Stream[Pure, Actor] = Stream.chunk(
    Chunk.array(
      Array(
        scarlettJohansson,
        robertDowneyJr,
        chrisEvans,
        markRuffalo,
        chrisHemsworth,
        jeremyRenner
      )
    )
  )

  // 3. Transforming a StreamPermalink

  // concatenates two streams:
  val dcAndMarvelSuperheroes: Stream[Pure, Actor] = jlActors ++ avengersActors

  val printedJlActors: Stream[IO, Unit] = jlActors.flatMap { actor: Actor =>
    Stream.eval(IO.println(actor))
  }

  // The pattern of calling the function Stream.eval inside a flatMap is so common that
  // fs2 provides a shortcut for it through the evalMap method:
  val evalMappedJlActors: Stream[IO, Unit] = jlActors.evalMap(IO.println)

  // If we need to perform some effects on the stream,
  // but we don’t want to change the type of the stream, we can use the evalTap method:
  val evalTappedJlActors: Stream[IO, Actor] = jlActors.evalTap(IO.println)

  // An essential feature of fs2 streams is that their functions take constant time

  // We want to group the Avengers by the actor’s name playing them. We can do it using the fold method:
  val avengersActorsByFirstName: Stream[Pure, Map[String, List[Actor]]] =
    avengersActors.fold(Map.empty[String, List[Actor]]) { (map, actor) =>
      map + (actor.firstName -> (actor :: map.getOrElse(actor.firstName, Nil)))
    }

  // Many other streaming libraries define streams and transformation in terms of sources, pipes, and sinks.
  // In fs2, the only available type modeling the above streaming concepts is Pipe[F[_], -I, +O].
  // So, a Pipe[F[_], I, O] represents nothing more than a function between two streams,
  // the first emitting elements of type I, and the second emitting elements of type O.

  // Stream[IO, Actor] => Stream[IO, String]
  val fromActorToStringPipe: Pipe[IO, Actor, String] = in =>
    in.map(actor => s"${actor.firstName} ${actor.lastName}")

  // Stream[IO, T] => Stream[IO, Unit]
  def toConsole[T]: Pipe[IO, T, Unit] = in =>
    in.evalMap(str => IO.println(str))

  // The through method applies a pipe to a stream.
  //
  // The jlActors stream represents the source,
  // whereas the fromActorToStringPipe represents a pipe,
  // and the toConsole represents the sink.
  val stringNamesOfJlActors: Stream[IO, Unit] =
    jlActors
      .through(fromActorToStringPipe)
      .through(toConsole)

}
