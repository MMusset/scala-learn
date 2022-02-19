package com.micahmusset.scalalearn.fs2.tutorials.rockthejvm

import cats.effect.IO
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Data._
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Model.Actor
import fs2.{Pure, Stream}

object Model {
  case class Actor(id: Int, firstName: String, lastName: String)
}

object Data {
  // Justice League
  val henryCavil: Actor = Actor(0, "Henry", "Cavill")
  val galGodot: Actor = Actor(1, "Gal", "Godot")
  val ezraMiller: Actor = Actor(2, "Ezra", "Miller")
  val benFisher: Actor = Actor(3, "Ben", "Fisher")
  val rayHardy: Actor = Actor(4, "Ray", "Hardy")
  val jasonMomoa: Actor = Actor(5, "Jason", "Momoa")

  // Avengers
  val scarlettJohansson: Actor = Actor(6, "Scarlett", "Johansson")
  val robertDowneyJr: Actor = Actor(7, "Robert", "Downey Jr.")
  val chrisEvans: Actor = Actor(8, "Chris", "Evans")
  val markRuffalo: Actor = Actor(9, "Mark", "Ruffalo")
  val chrisHemsworth: Actor = Actor(10, "Chris", "Hemsworth")
  val jeremyRenner: Actor = Actor(11, "Jeremy", "Renner")
  val tomHolland: Actor = Actor(13, "Tom", "Holland")
  val tobeyMaguire: Actor = Actor(14, "Tobey", "Maguire")
  val andrewGarfield: Actor = Actor(15, "Andrew", "Garfield")
}

object Setup {

  // Pure Stream
  // Using the Pure effect means that pulling the elements from the stream cannot fail.
  val jlActors: Stream[Pure, Actor] = Stream(
    henryCavil,
    galGodot,
    ezraMiller,
    benFisher,
    rayHardy,
    jasonMomoa
  )

  // .emit
  // create a pure stream with only one element
  val tomHollandStream: Stream[Pure, Actor] = Stream.emit(tomHolland)

  // .emits
  // create a pure stream with a sequence of elements
  val spiderMen: Stream[Pure, Actor] = Stream.emits(List(
    tomHolland,
    tobeyMaguire,
    andrewGarfield
  ))

  // .toList
  val jlActorList: List[Actor] = jlActors.toList
  val jlActorVector: Vector[Actor] = jlActors.toVector

  // infinite stream
  // Repeat this stream an infinite number of times.
  val infiniteJLActors: Stream[Pure, Actor] = jlActors.repeat

  // Since we cannot put an infinite stream into a list,
  // we take the stream’s first n elements and convert them into a list.
  val repeatedJLActorsList: List[Actor] = infiniteJLActors.take(12).toList


  // Starting from the stream we already defined, we can create a new effectful stream mapping
  // the Pure effect in an IO effect using the covary[F] method:
  val liftedJlActors1: Stream[IO, Actor] = jlActors.covary[IO]
  val liftedJlActors2: Stream[IO, Actor] = jlActors.evalMap(actor => IO(actor))

  // In most cases,
  // we want to create a stream directly evaluating some statements that may produce side effects.
  // So, for example, let’s try to persist an actor through a stream:
  val savingTomHolland: Stream[IO, Unit] = Stream.eval {
    IO {
      println(s"Saving actor $tomHolland")
      Thread.sleep(1000)
      println("Finished")
    }
  }
  // eval that takes an IO effect and returns a Stream that will evaluate the IO effect when pulled.

  // A question arises:
  // How do we pull the values from an effectful stream?
  // We cannot convert such a stream into a Scala collection using the toList function.

  // In fs2 jargon, we need to compile the stream into a single instance of the effect:
  val compiledStream: Stream.CompileOps[IO, IO, Unit] = savingTomHolland.compile

  // In this case, we also applied the drain method, which discards any effect output.
  val compiledDrainedStream: IO[Unit] = savingTomHolland.compile.drain

  // However, once compiled, we return to have many choices.
  // For example, we can transform the compiled stream into an effect containing a List:
  val jlActorsEffectfulList: IO[List[Actor]] = liftedJlActors1.compile.toList

}
