package org.akozlov.chapter07

import scalax.collection.Graph
import scalax.collection.edge._
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._

import scalax.collection.edge.Implicits._

/**
 * An example of JSON pretty print
 */
object InfluenceDiagramToJson extends App {

  val g = Graph[String,LDiEdge](("'Weather'" ~+> "'Weather Forecast'")("Forecast"), ("'Weather Forecast'" ~+> "'Vacation Activity'")("Decision"), ("'Vacation Activity'" ~+> "'Satisfaction'")("Deterministic"), ("'Weather'" ~+> "'Satisfaction'")("Deterministic"), ("'Satisfaction'" ~+> "'Recommend to a Friend'")("Probabilistic"))

  import scalax.collection.io.json.descriptor.predefined.{LDi}
  import scalax.collection.io.json.descriptor.StringNodeDescriptor
  import scalax.collection.io.json._

  val descriptor = new Descriptor[String](
    defaultNodeDescriptor = StringNodeDescriptor,
    defaultEdgeDescriptor = LDi.descriptor[String,String]("Edge")
  )

  import net.liftweb.json._

  println(Printer.pretty(JsonAST.render(JsonParser.parse(g.toJson(descriptor)))))
}
