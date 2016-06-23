package org.akozlov.chapter06

import java.io._

import java.time.ZoneOffset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.spark.{SparkConf,SparkContext}
import org.apache.spark.storage.StorageLevel

/**
 *
 * Object Sessionize
 *
 * run as:
 *
 * <code>sbt run"</code>
 *
 * The Spark dependency has been added as a part of build setup
 */
object Sessionize extends App {
  val sc = new SparkContext("local[8]", "Sessionize", new SparkConf())

  val checkoutPattern = ".*>checkout.*".r.pattern

  // a basic page view structure
  case class PageView(ts: String, path: String) extends Serializable with Ordered[PageView] {
    override def toString: String = {
      s"($ts #$path)"
    }
    def compare(other: PageView) = ts compare other.ts
  }

  // represent a session
  case class Session[A  <: PageView](id: String, visits: Seq[A]) extends Serializable {
    override def toString: String = {
      val vsts = visits.mkString("[", ",", "]")
      s"($id -> $vsts)"
    }
  }

  def toEpochSeconds(str: String) = { LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC) }

  val sessions = sc.textFile("data/clickstream")
    .map(line => {val parts = line.split("\t"); (parts(4), new PageView(parts(0), parts(20)))})
    .groupByKey.map(x => { new Session(x._1, x._2.toSeq.sorted) } )
    .cache

  // sessions.take(100).foreach(println)

  def findAllCheckoutSessions(s: Session[PageView]) = {
    s.visits.tails.filter {
      _ match { case PageView(ts1, "mycompanycom>homepage") :: PageView(ts2, page) :: tail if (page != "mycompanycom>homepage" ) => true; case _ => false }
    }
    .foldLeft(Seq[Session[PageView]]()) {
      case (r, x) => {
        x.find(y => checkoutPattern.matcher(y.path).matches) match {
          case Some(checkout) if (toEpochSeconds(checkout.ts) > toEpochSeconds(x.head.ts) + 60) => r.:+(new Session(s.id, x.slice(0, x.indexOf(checkout))))
          case _ => r
        }
      }
    }
  }

  val prodLandingSessions = sessions.flatMap(findAllCheckoutSessions)

  prodLandingSessions.collect.foreach(println)

  sc.stop()
}
