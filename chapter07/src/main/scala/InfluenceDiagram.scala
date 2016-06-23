package org.akozlov.chapter07

import scalax.collection.Graph
import scalax.collection.edge._
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._

import scalax.collection.edge.Implicits._

/**
 * An Influence Diagram example based on Chapter 2 from the book
 */
object InfluenceDiagram extends App {

  var g = Graph(("'Weather'" ~+> "'Weather Forecast'")("Forecast"), ("'Weather Forecast'" ~+> "'Vacation Activity'")("Decision"), ("'Vacation Activity'" ~+> "'Satisfaction'")("Deterministic"), ("'Weather'" ~+> "'Satisfaction'")("Deterministic"))

  println(g.mkString(";"))
  println("Directed: " + g.isDirected)
  println("Acyclic: " + g.isAcyclic)

  g += ("'Satisfaction'" ~+> "'Recommend to a Friend'")("Probabilistic")

  println(g.mkString(";"))
  println("Directed: " + g.isDirected)
  println("Acyclic: " + g.isAcyclic)

  println((g get "'Recommend to a Friend'").incoming)

  g += ("'Satisfaction'" ~+> "'Weather'")("Cyclic")

  println(g.mkString(";"))
  println("Directed: " + g.isDirected)
  println("Acyclic: " + g.isAcyclic)

  g += LDiEdge("'Weather Forecast'", "'Weather'")("Reverse")

  println(g.mkString(";"))
  println("Directed: " + g.isDirected)
  println("Acyclic: " + g.isAcyclic)

  println(g.nodes)
  println(g.edges)

  println((g get "'Weather'").outerNodeTraverser.toList)
  println((g get "'Weather'").innerNodeTraverser.toList)
  println((g get "'Satisfaction'").outerNodeTraverser.toList)
  println((g get "'Satisfaction'").innerNodeTraverser.toList)

  println((g get "'Satisfaction'").incoming)

  println(g.isMulti)
  println(g.isAcyclic)
  println(g.isConnected)

  val nodes = List(1, 3, 5)
  val edges = List(3~1)
  val ints = Graph.from(nodes, edges)

  println(ints)

  println(ints.isMulti)
  println(ints.isAcyclic)
  println(ints.isConnected)
}
