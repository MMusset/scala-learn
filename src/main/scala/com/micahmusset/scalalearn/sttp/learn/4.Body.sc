import cats.effect.{ContextShift, IO}
import cats.implicits.catsSyntaxApplicativeId
import sttp.client3._
import sttp.client3.armeria.cats.ArmeriaCatsBackend
import sttp.model.Header

import scala.concurrent.ExecutionContext

implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
implicit val backend: SttpBackend[IO, Any]  = ArmeriaCatsBackend.apply[IO]()

// =====================================================================================================================
// Text data

// =====================================================================================================================
// Binary data

// =====================================================================================================================
// Uploading Files

// =====================================================================================================================
// Form Data

// =====================================================================================================================
// Custom Body Serializers
