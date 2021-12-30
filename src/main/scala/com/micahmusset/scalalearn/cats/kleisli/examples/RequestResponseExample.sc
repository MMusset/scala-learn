import cats._
import cats.effect.IO
import cats.implicits._

// ===================================================================================================================
// Setup

// Domain
final case class Request(id: String)
final case class Payment(amount: String)
final case class RemoteRequest(amount: String)
final case class Response(amount: String)

// Functions
def getPayment[F[_]: Applicative](request: Request): F[Payment]             = Payment(request.id).pure[F]           // Mock a database call
def paymentToRequest[F[_]: Applicative](payment: Payment): F[RemoteRequest] = RemoteRequest(payment.amount).pure[F] // Lift Request into context F
def runRequest[F[_]: Applicative](request: RemoteRequest): F[Response]      = Response(request.amount).pure[F]      // Mock a http call

// ===================================================================================================================
// Endpoints

val request1 = Request("123")
val request2 = Request("456")

// for comprehension
// Request => F[Response]
def endpoint1[F[_]: Monad](request: Request): F[Response] =
  for {
    payment       <- getPayment[F](request)
    remoteRequest <- paymentToRequest[F](payment)
    response      <- runRequest[F](remoteRequest)
  } yield response

endpoint1[IO](request1).unsafeRunSync()

// Monadic Composition
// Request => F[Response]
def endpoint2[F[_]: Monad](request: Request): F[Response] =
  getPayment[F](request) >>=
    paymentToRequest[F] >>=
    runRequest[F]

endpoint2[IO](request1).unsafeRunSync()

// Kleisli Composition
// Kleisli[F, Request, Response]
// Request => F[Response]
import cats.data.Kleisli

def endpoint3[F[_]: Applicative: FlatMap]: Kleisli[F, Request, Response] =
  Kleisli(getPayment[F]) andThen Kleisli(paymentToRequest[F]) andThen Kleisli(runRequest[F])

def endpoint3Run[F[_]: Applicative: FlatMap]: Request => F[Response] = endpoint3[F].run

endpoint3Run[IO].apply(request1).unsafeRunSync()

// So lets say at the top of my program I want to declare a http endpoint,
// but I don't' want to run the endpoint because I don't have a request.
// I can create an instance of Kleisli[F, Request, Response] and inject that into my program.
// Then when my program receives a request, I can run this endpoint with that request.
val example1: Kleisli[IO, Request, Response] = endpoint3[IO]
val example1Run: IO[Response]                = endpoint3[IO].run(request1)

// And when I receive a new request I can run the endpoint with that new request.
endpoint3[IO].run(request2)

// Cool, but why couldn't I just inject endpoint1, which is a function from Request to IO[Response]?
val example2: Request => IO[Response] = endpoint1[IO]
val example2Run                       = endpoint1[IO](request1)

// And the answer is I could

// Lets make a type aliases
type HttpApp1[F[_]] = Kleisli[F, Request, Response]
type HttpApp2[F[_]] = Request => F[Response]

// ===================================================================================================================
// Program1
def mountHttpApp1[F[_]](service: HttpApp1[F], prefix: String): F[Response] = service.run(request1)

val route1: HttpApp1[IO] = example1

val program1 = mountHttpApp1(route1, "/")

program1.unsafeRunSync()

// ===================================================================================================================
// Program2
def mountHttpApp2[F[_]](service: HttpApp2[F], prefix: String): F[Response] = service(request2)

val route2: HttpApp2[IO] = example2

val program2 = mountHttpApp2(route2, "/")

program2.unsafeRunSync()
