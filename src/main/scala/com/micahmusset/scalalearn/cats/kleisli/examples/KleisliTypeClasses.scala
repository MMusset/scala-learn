package com.micahmusset.scalalearn.cats.kleisli.examples

import cats.Applicative
import cats.implicits._

object KleisliTypeClasses {

  import cats.data.Kleisli
  import cats.~>

  // KleisliTypeClasses

  // TODO: TracedHttpApp

  // Domain
  final case class Request[F[_]](id: String) {
    def mapK[G[_]](f: F ~> G): Request[G] = Request[G](id)
  }

  final case class Response[F[_]](amount: String) {
    def mapK[G[_]](f: F ~> G): Response[G] = Response[G](amount)
  }

  trait Context[F[_]] {
    def id: F[Option[String]]
  }
  trait Span[F[_]]

  type HttpApp[F[_]] = Kleisli[F, Request[F], Response[F]]

  type ContextAndSpan[F[_]] = (Context[F], Span[F])
  type ContextF[F[_], A]    = Kleisli[F, ContextAndSpan[F], A]
  type TracedHttpApp[F[_]]  = HttpApp[ContextF[F, *]]

  trait TracedHttpAppp[F[_]] {
    def runTrace(tracedHttpApp: TracedHttpApp[F]): HttpApp[F]
  }

  private def liftContextF[F[_]](context: ContextAndSpan[F]): ContextF[F, *] ~> F =
    new (ContextF[F, *] ~> F) {
      def apply[A](a: ContextF[F, A]): F[A] = a.run(context)
    }

  private def makeContextAndSpan[F[_]: Applicative]: ContextAndSpan[F] = {
    val context = new Context[F] {
      override def id: F[Option[String]] = "123".some.pure[F]
    }
    val span    = ???

    (context, span)
  }

  private def liftRequestF[F[_]](requestF: Request[F]) = {
    val liftContextF: F ~> Kleisli[F, ContextAndSpan[F], *] = Kleisli.liftK[F, ContextAndSpan[F]]

    requestF.mapK(liftContextF)
  }

  private def liftContextF[F[_]](tracedResponse: Response[ContextF[F, *]], contextAndSpan: (Context[F], Span[F])) = {
    val yy = new (ContextF[F, *] ~> F) {
      def apply[A](a: ContextF[F, A]): F[A] = a.run(contextAndSpan)
    }

    tracedResponse.mapK(yy)
  }

  // 1. Request[F] =>
  // 2.   Request[(Context[F], Span[F])] =>
  // 3.     (Context[F], Span[F]) =>
  // 4.   Response[(Context[F], Span[F])] =>
  // 5.     (Context[F], Span[F]) =>
  // 6. Response[F]
  def make[F[_]: Applicative]: TracedHttpAppp[F] =
    new TracedHttpAppp[F] {
      override def runTrace(tracedEndpoint: TracedHttpApp[F]): HttpApp[F] =
        Kleisli { requestF: Request[F] =>
          val requestContextF: Request[Kleisli[F, (Context[F], Span[F]), *]] = liftRequestF(requestF)
          val contextToResponse                                              = tracedEndpoint.run(requestContextF)

          val contextAndSpan = makeContextAndSpan[F]

          for {
            tracedResponse <- contextToResponse.run(contextAndSpan)
          } yield liftContextF(tracedResponse, contextAndSpan)
        }

    }

}
