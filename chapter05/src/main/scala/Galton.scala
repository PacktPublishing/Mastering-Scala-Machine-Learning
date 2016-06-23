package org.akozlov.chapter05

import scala.util.Random.nextGaussian

object Galton extends App {

  val x0 = Vector.fill(201)(100 * nextGaussian)
  val y0 = Vector.fill(201)(30 * nextGaussian)

  val x1 = (x0, y0).zipped.map((a,b) => 0.5 * (a + b) )
  val y1 = (x0, y0).zipped.map((a,b) => 0.5 * (a - b) )

  val a = (x1, y1).zipped.map(_*_).sum / x1.map(x => x* x).sum
  println(f"Regression coefficient is $a%.3f")
}
