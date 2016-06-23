package org.akozlov.examples

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator

import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import javax.management.MBeanAttributeInfo
import javax.management.MBeanInfo
import javax.management.MBeanServer
import javax.management.ObjectName
import javax.management.openmbean.CompositeData
import javax.management.openmbean.CompositeDataSupport
import javax.management.openmbean.CompositeType
import javax.management.openmbean.SimpleType
import javax.management.openmbean.TabularData

import net.liftweb.json.Serialization.write
import net.liftweb.json._

import org.scalatra.ScalatraServlet
import org.scalatra._
import org.scalatra.metrics.{MetricsSupport, HealthChecksSupport}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scalate.ScalateSupport

class ServletWithMetrics extends Servlet with MetricsSupport with HealthChecksSupport {

  val logger = LoggerFactory.getLogger(getClass)

  val defaultName = "Stranger"

  var hwCounter: Long = 0L
  val hwLookup: scala.collection.mutable.Map[String,Long] = scala.collection.mutable.Map()

  val hist = histogram("histogram")
  val cnt =  counter("counter")

  val m = meter("meter")

  healthCheck("response", unhealthyMessage = "Ouch!") { response("Alex", 2) contains "Alex" }

  def response(name: String, id: Long) = { "Hello %s! Your id should be %d.".format(if (name.length > 0) name else defaultName, id) }

  get("/hw/:name") {
    cnt += 1
    val name = params("name")
    hist += name.length
    val startTime = System.nanoTime
    // Scala does not have increment operator
    val retVal = response(name, synchronized { hwLookup.get(name) match { case Some(id) => id; case _ => hwLookup += name -> { hwCounter += 1; hwCounter } ; hwCounter } } )
    val elapsedTime = System.nanoTime - startTime
    logger.info("It took [" + name + "] " + elapsedTime + " " + TimeUnit.NANOSECONDS)
    retVal
  }

  get("/jmx") {
	val mbs = ManagementFactory.getPlatformMBeanServer();
	mbs.queryNames(null, null).map(dumpInfo(mbs, _)).mkString("\n")
  }

  def getClassName(mbs: MBeanServer, oName: ObjectName, name: String): String = {
    if ("org.apache.commons.modeler.BaseModelMBean" == name)
      mbs.getAttribute(oName, "modelerType").toString
    else
      name
  }

  def dumpInfo(mbs: MBeanServer, oName: ObjectName): String = {
	val beanInfo = mbs.getMBeanInfo(oName);
	"beanName: " + oName.toString() + "\n" +
	"className: " + getClassName(mbs, oName, beanInfo.getClassName()) + "\n" +
	beanInfo.getAttributes().map(dumpAttributes(mbs, oName, _)).mkString("\n") + "\n"
  }

  def dumpAttributes(mbs: MBeanServer, oName: ObjectName, attr: MBeanAttributeInfo): String = {
    if (attr.isReadable()) {
      val attName: String = attr.getName();
      if (! "modelerType".equals(attName) && ! "[=: ]+".matches(attName)) {
    	try {
    	  dumpAttribute(mbs, attName, mbs.getAttribute(oName, attName))
    	} catch {
    	  case e: Exception => ""
    	}
      } else ""
    } else ""
  }

  def dumpAttribute(mbs: MBeanServer, attName: String, value: Object): String = {
    "attrName: " + attName + " attrValue: " + {
      if (value == null)
        "null"
      else {
        if (value.isInstanceOf[SimpleType[Long]]) {
    	  val stl = value.asInstanceOf[SimpleType[Long]]
    	  stl.readResolve.toString
        } else if (value.isInstanceOf[SimpleType[String]]) {
    	  val sts = value.asInstanceOf[Array[String]]
    	  sts.toString
        } else if (value.isInstanceOf[Array[Long]]) {
    	  val arr = value.asInstanceOf[Array[Long]]
    	  arr.mkString("[", ",", "]")
        } else if (value.isInstanceOf[Array[Object]]) {
    	  val arr = value.asInstanceOf[Array[Object]]
    	  for (k <- 0 to arr.length) yield dumpAttribute(mbs, k.toString, arr(k)) mkString("[", ",", "]")
        } else if (value.isInstanceOf[CompositeDataSupport]) {
    	  val cds = value.asInstanceOf[CompositeDataSupport]
    	  val cts = cds.getCompositeType()
    	  cts.keySet.map { k => dumpAttribute(mbs, k, cds.get(k)) }.mkString("\n")
        } else if (value.isInstanceOf[CompositeData]) {
    	  val cds = value.asInstanceOf[CompositeData]
    	  val ct = cds.getCompositeType()
    	  ct.keySet.map { k => dumpAttribute(mbs, k, ct.getType(k)) }.mkString("\n")
        } else if (value.isInstanceOf[TabularData]) {
    	  val tds = value.asInstanceOf[TabularData]
    	  tds.values map { k => dumpAttribute(mbs, k.toString, k.asInstanceOf[Object]) } mkString("[", ",", "]")
        } else {
    	  value.toString
        }
      }
    }
  }

  get("/time") {
    val sleepTime = scala.util.Random.nextInt(1000)
    val startTime = System.nanoTime
    timer("timer") {
      Thread.sleep(sleepTime)
      Thread.sleep(sleepTime)
      Thread.sleep(sleepTime)
    }
    logger.info("It took [" + sleepTime + "] " + (System.nanoTime - startTime) + " " + TimeUnit.NANOSECONDS)
    m.mark(1)
  }
}
