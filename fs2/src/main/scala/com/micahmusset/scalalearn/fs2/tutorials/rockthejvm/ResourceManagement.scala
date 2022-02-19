package com.micahmusset.scalalearn.fs2.tutorials.rockthejvm

import cats.effect._
import com.micahmusset.scalalearn.fs2.tutorials.rockthejvm.ErrorHandling.savedJlActors
import fs2._

// https://blog.rockthejvm.com/fs2/#5-resource-management
object ResourceManagement {

  // we don’t have to use the handleErrorWith method to manage the release of resources used by the stream eventually
  // the fs2 library implements the bracket pattern to manage resources.

  // The bracket pattern is a resource management pattern developed in the functional programming domain many years ago. The pattern defines two functions:
  // The first is used to acquire a resource;
  // The second is guaranteed to be called when the resource is no longer needed.

  // def bracket[F[x] >: Pure[x], R](acquire: F[R])(release: R => F[Unit]): Stream[F, R] = ???

  case class DatabaseConnection(connection: String) extends AnyVal

  val acquire = IO {
    val conn = DatabaseConnection("jlaConnection")
    println(s"Acquiring connection to the database: $conn")
    conn
  }

  val release = (conn: DatabaseConnection) =>
    IO.println(s"Releasing connection to the database: $conn")

  val managedJlActors: Stream[IO, Int] =
    Stream.bracket(acquire)(release).flatMap(conn => savedJlActors)

  // Acquiring connection to the database: DatabaseConnection(jlaConnection)
  // Saving actor: Actor(0,Henry,Cavill)
  // Releasing connection to the database: DatabaseConnection(jlaConnection)
  // java.lang.RuntimeException: Something went wrong during the communication with the persistence layer

  // As we can see, the resource we created was released, even if the stream had terminated with an error.
}
