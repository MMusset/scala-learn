package com.micahmusset.scalalearn.fs2.tutorials.rockthejvm

import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Data.tomHolland
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.Model.Actor
import fs2._

// https://blog.rockthejvm.com/fs2/#6-the-pull-type
object PullType {

  // stream effectively computes the next stream element just in time.

  val tomHollandActorPull: Pull[Pure, Actor, Unit] = Pull.output1(tomHolland)
  val tomHollandActorStream: Stream[Pure, Actor] = tomHollandActorPull.stream


}
