package com.micahmusset.scalalearn.fs2.examples.concurency

import cats.effect._
import cats.effect.implicits.genTemporalOps
import cats.implicits._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final case class SuccessResult(value: String)

final class RemoteService[F[_]: Async: Temporal: Clock](timeout: FiniteDuration) {
  def postHttpRequest: F[SuccessResult] = for {
    _ <- Sync[F].delay(SuccessResult("POST HTTP REQUEST"))
    _ <- Temporal[F].sleep(timeout)
  } yield SuccessResult("Success Result")
}

sealed trait IndividualTaskAlg[F[_], A, B] {
  def name: String
  def service: A
  def run: F[B]
  def timeout: FiniteDuration
}

final case class IndividualTaskSuccess[F[_]: Sync](name: String, service: RemoteService[F], timeout: FiniteDuration = 60.seconds)
    extends IndividualTaskAlg[F, RemoteService[F], SuccessResult] {
  def run: F[SuccessResult] = Sync[F].pure(println(s"Task Name: $name")) *> service.postHttpRequest
}

final case class IndividualTaskFail[F[_]: Sync](name: String, service: RemoteService[F], timeout: FiniteDuration = 60.seconds)
    extends IndividualTaskAlg[F, RemoteService[F], SuccessResult] {
  def run: F[SuccessResult] = Sync[F].raiseError[SuccessResult](new Throwable("arrrgg"))
}

final case class GroupResult[F[_]](
    successTasks: List[(IndividualTaskAlg[F, RemoteService[F], SuccessResult], SuccessResult)],
    failureTasks: List[(IndividualTaskAlg[F, RemoteService[F], SuccessResult], Throwable)]
)

trait GroupTaskAlg[F[_]] {

  def run(tasks: List[IndividualTaskAlg[F, RemoteService[F], SuccessResult]]): F[GroupResult[F]]

}

final case class GroupTaskLogger[F[_]: Sync](impl: GroupTaskAlg[F]) extends GroupTaskAlg[F] {

  override def run(tasks: List[IndividualTaskAlg[F, RemoteService[F], SuccessResult]]): F[GroupResult[F]] =
    for {
      _           <- Sync[F].delay(println(s"Started Program"))
      _           <- Sync[F].delay(println(s"================================================================"))
      _           <- Sync[F].delay(println(s"LIST: $tasks"))
      _           <- Sync[F].delay(println(s"================================================================"))
      groupResult <- impl.run(tasks)
      _           <- Sync[F].delay(println(s"================================================================"))
      _           <- Sync[F].delay(println(s"SUCCESS COUNT: ${groupResult.successTasks.length}"))
      _           <- Sync[F].delay(println(s"SUCCESS: ${groupResult.successTasks}"))
      _           <- Sync[F].delay(println(s"================================================================"))
      _           <- Sync[F].delay(println(s"FAIL COUNT: ${groupResult.failureTasks.length}"))
      _           <- Sync[F].delay(println(s"FAIL: ${groupResult.failureTasks}"))
      _           <- Sync[F].delay(println(s"================================================================"))
      _           <- Sync[F].delay(println(s"Finished Program"))
    } yield groupResult

}

final case class GroupTasksImpl[F[_]: Async: Temporal]() extends GroupTaskAlg[F] {

  override def run(tasks: List[IndividualTaskAlg[F, RemoteService[F], SuccessResult]]): F[GroupResult[F]] =
    for {
      result        <- runTasks(tasks)
      successResults = result.mapFilter {
                         case (rule, Right(value)) => Some((rule, value))
                         case (_, Left(_))         => None
                       }
      failResults    = result.mapFilter {
                         case (rule, Left(value)) => Some((rule, value))
                         case (_, Right(_))       => None
                       }
    } yield GroupResult(successResults, failResults)

  def runTasks(
      tasks: List[IndividualTaskAlg[F, RemoteService[F], SuccessResult]]
  ): F[List[(IndividualTaskAlg[F, RemoteService[F], SuccessResult], Either[Throwable, SuccessResult])]] =
    fs2.Stream
      .emits(tasks)
      .covary
      .parEvalMapUnordered(3)(task => task.run.timeout(task.timeout).attempt.map(result => (task, result)))
      .compile
      .toList

}

object GroupTaskProgram {

  val fastRemoteService = new RemoteService[IO](1.seconds)
  val slowRemoteService = new RemoteService[IO](10.seconds)

  val task1 = IndividualTaskSuccess[IO]("Task 1", fastRemoteService, 2.seconds)
  val task2 = IndividualTaskFail[IO]("Task 2", fastRemoteService, 2.seconds)
  val task3 = IndividualTaskSuccess[IO]("Task 3", fastRemoteService, 2.seconds)

  val tasks = List(task1, task2, task3)

  val program       = GroupTasksImpl[IO]()
  val loggedProgram = GroupTaskLogger[IO](program)

  def make: IO[Unit] = loggedProgram.run(tasks) *> IO.unit
}
