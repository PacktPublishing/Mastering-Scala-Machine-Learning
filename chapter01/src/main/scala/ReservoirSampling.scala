package org.akozlov.chapter01

import scala.reflect.ClassTag
import scala.util.Random
import util.Properties

object ReservoirSampling extends App {
  def reservoirSample[T: ClassTag](input: Iterator[T], k: Int): Array[T] = {
    val reservoir = new Array[T](k)
    // Put the first k elements in the reservoir.
    var i = 0
    while (i < k && input.hasNext) {
      val item = input.next()
      reservoir(i) = item
      i += 1
    }

    if (i < k) {
      // If input size < k, trim the array size
      reservoir.take(i)
    } else {
      // If input size > k, continue the sampling process.
      while (input.hasNext) {
        val item = input.next
        val replacementIndex = Random.nextInt(i)
        if (replacementIndex < k) {
          reservoir(replacementIndex) = item
        }
        i += 1
      }
      reservoir
    }
  }

  val numLines=15
  val w = new java.io.FileWriter(new java.io.File("out.txt"))

  val lines = io.Source.fromFile("data/iris/in.txt").getLines
  reservoirSample(lines, numLines).foreach { s =>
    w.write(s + scala.util.Properties.lineSeparator)
  }
  w.close
}
