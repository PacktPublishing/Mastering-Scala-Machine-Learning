package org.akozlov.chapter02

object KellyCriterion extends App {

  def logFactorial(n: Int) = { (1 to n).map(Math.log(_)).sum }

  def cmnp(m: Int, n: Int, p: Double) = {
    Math.exp(logFactorial(n) -
      logFactorial(m) +
      m*Math.log(p) -
      logFactorial(n-m) +
      (n-m)*Math.log(1-p))
  }

  val p = 0.105
  val n = 60

  var cumulative = 0.0

  for(i <- 0 to 14) {
    val prob = cmnp(i,n,p)
    cumulative += prob
    println(f"We expect $i wins with $prob%.6f probability $cumulative%.3f cumulative (n = $n, p = $p).")
  }
}
