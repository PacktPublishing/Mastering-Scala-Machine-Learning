package org.akozlov.chapter07

import scala.io.Source

import scala.util.hashing.{MurmurHash3 => Hash}
import scala.util.matching.Regex

import java.util.{Date => javaDateTime}

import java.io.File
import net.liftweb.json._
import Extraction._
import Serialization.{read, write}

/**
 * Sample code to trasform Enron emails from http://www.cs.cmu.edu/~./enron/enron_mail_20150507.tgz
 *
 * To get the dataset: {{{
 * bash$ (mkdir Enron; cd Enron; wget -O - http://www.cs.cmu.edu/~./enron/enron_mail_20150507.tgz | tar xzvf -)
 * }}}
 * 
 * To run: {{{
 * bash$ sbt --error "run-main org.akozlov.chapter07.EnronEmail Enron/maildir" > graph.json
 * }}}
 */
object EnronEmail {

  val emailRe = """[a-zA-Z0-9_.+\-]+@enron.com""".r.unanchored

  def emails(s: String) = {
    for (email <- emailRe findAllIn s) yield email
  }

  def hash(s: String) = {
    java.lang.Integer.MAX_VALUE.toLong + Hash.stringHash(s)
  }

  val messageRe =
    """(?:Message-ID:\s+)(<[A-Za-z0-9_.+\-@]+>)(?s)(?:.*?)(?m)
      |(?:Date:\s+)(.*?)$(?:.*?)
      |(?:From:\s+)([a-zA-Z0-9_.+\-]+@enron.com)(?:.*?)
      |(?:Subject: )(.*?)$""".stripMargin.r.unanchored

  case class Relation(from: String, fromId: Long, to: String, toId: Long, source: String, messageId: String, date: javaDateTime, subject: String)

  implicit val formats = Serialization.formats(NoTypeHints)

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree) else Stream.empty)

  def main(args: Array[String]) {
    getFileTree(new File(args(0))).par.map {
      file => {
        "\\.$".r findFirstIn file.getName match {
          case Some(x) =>
            try {
              val src = Source.fromFile(file, "us-ascii")
              val message = try src.mkString finally src.close()
              message match {
                case messageRe(messageId, date, from , subject) =>
                  val fromLower = from.toLowerCase
                  for (to <- emails(message).filter(_ != fromLower).toList.distinct)
                    println(write(Relation(fromLower, hash(fromLower), to, hash(to), file.toString, messageId, new javaDateTime(date), subject)))
                case _ =>
              }
            } catch {
              case e: Exception => System.err.println(e)
            }
          case _ =>
        }
      }
    }
  }
}
