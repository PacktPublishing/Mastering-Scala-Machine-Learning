package org.akozlov.chapter07

import scalax.collection.GraphPredef._, scalax.collection.GraphEdge._
import scalax.collection.constrained.{Config, ConstraintCompanion, Graph => DAG}
import scalax.collection.constrained.constraints._ // Connected and Acyclic constraint

/**
 * Acyclic constraint
 */
object AcyclicWithSideEffect extends ConstraintCompanion[Acyclic] {
  def apply [N, E[X] <: EdgeLikeIn[X]] (self: DAG[N,E]) =
    new Acyclic[N,E] (self) {
      override def onAdditionRefused(refusedNodes: Iterable[N],
                                     refusedEdges: Iterable[E[N]],
                                     graph:        DAG[N,E]) = {
        println("Addition refused: " + "nodes = " + refusedNodes + ", edges = " + refusedEdges)
        true
      }
    }
}

/**
 * Connected constraint
 */
object ConnectedWithSideEffect extends ConstraintCompanion[Connected] {
  def apply [N, E[X] <: EdgeLikeIn[X]] (self: DAG[N,E]) =
    new Connected[N,E] (self) {
      override def onSubtractionRefused(refusedNodes: Iterable[DAG[N,E]#NodeT],
                                        refusedEdges: Iterable[DAG[N,E]#EdgeT],
                                        graph:        DAG[N,E]) = {
        println("Subtraction refused: " + "nodes = " + refusedNodes + ", edges = " + refusedEdges)
        true
      }
    }
}

object ConstrainedGraph extends App {

  implicit val conf: Config = ConnectedWithSideEffect && AcyclicWithSideEffect

  val g = DAG(1~>2, 1~>3, 2~>3, 3~>4) // Graph()

  println(g ++ List(1~>4, 3~>1))

  println(g - 2~>3)

  println(g - 2)

  println((g + 4~>5) - 3)
}
