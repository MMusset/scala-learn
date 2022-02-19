// Composing Monadic Functions with Kleisli Arrows
// https://sanj.ink/posts/2017-06-07-composing-monadic-functions-with-kleisli-arrows.html

// Functions
def power2: Int => Double = Math.pow(_, 2)

def doubleToInt: Double => Int = _.toInt

def mul2: Int => Int = _ * 2

def intToString: Int => String = _.toString

// ========================================================================================
// Compose Functions

val seed: Int    = 3
val res1: Double = power2(seed)
val res2: Int    = doubleToInt(res1)
val res3: Int    = mul2(res2)
val res4: String = intToString(res3)
// returns "18"

// Compose
val pipeline1: Int => String = intToString compose mul2 compose doubleToInt compose power2
pipeline1(3)
// returns "18"

// andThen
val pipeline2: Int => String = power2 andThen doubleToInt andThen mul2 andThen intToString
pipeline2(3)
// returns "18"

// ========================================================================================
// Monadic Functions

def stringToNonEmptyString: String => Option[String] = value => if (value.nonEmpty) Option(value) else None
def stringToNumber: String => Option[Int]            = value => if (value.matches("-?[0-9]+")) Option(value.toInt) else None

//val pipeline3: String => Option[Int] = ???

// Compilation Errors
// val pipeline3: String => Option[Int] = stringToNonEmptyString andThen stringToNumber
// stringToNonEmptyString returns an Option[String] instead of String

// ========================================================================================
// FlatMap Monadic Functions

val pipeline4: String => Option[Int] = { seed =>
  for {
    res1: String <- stringToNonEmptyString(seed)
    res2: Int    <- stringToNumber(res1)
  } yield res2
}

// With a slight syntax tweak
val pipeline4v1: String => Option[Int] = { seed =>
  for {
    res1: String <- Option(seed) // We raised the Int into a the Option Monad, Option[Int]
    res2: String <- stringToNonEmptyString(res1)
    res3: Int    <- stringToNumber(res2)
  } yield res3
}

// ========================================================================================
// Plain Monads
// >>= is alias for flatMap

import cats.implicits._ // Brings in a Monadic instance for Option

val pipeline5: String => Option[Int] = { stringToNonEmptyString(_) >>= stringToNumber }
val pipeline6: String => Option[Int] = Option(_) >>= stringToNonEmptyString >>= stringToNumber

// Or if we have the input up front:
Option("1000") >>= stringToNonEmptyString >>= stringToNumber // Some(1000)
Option("") >>= stringToNonEmptyString >>= stringToNumber     // None
Option("A12B") >>= stringToNonEmptyString >>= stringToNumber // None

// ========================================================================================
// Syntax Comparison for Monadic Composition

val pipeline4v1Again: String => Option[Int] = { seed =>
  for {
    res1 <- Option(seed)
    res2 <- stringToNonEmptyString(res1)
    res3 <- stringToNumber(res2)
  } yield res3
}
val pipeline5Again: String => Option[Int]   = { stringToNonEmptyString(_) >>= stringToNumber }
val pipeline6Again: String => Option[Int]   = Option(_) >>= stringToNonEmptyString >>= stringToNumber

// ========================================================================================
// Kleisli Composition

import cats.data.Kleisli

val stringToNonEmptyStringK: Kleisli[Option, String, String] = Kleisli(stringToNonEmptyString)
val stringToNumberK: Kleisli[Option, String, Int]            = Kleisli(stringToNumber)

val pipeline7: Kleisli[Option, String, Int] = stringToNonEmptyStringK andThen stringToNumberK

pipeline7("1000") // Some(1000)
pipeline7("")     // None
pipeline7("A12B") // None

// Benefits of Kleisli Composition
// 1. Allows programming in a more composition like style.
// 2. Abstracts away the lifting of values into a Monad.
