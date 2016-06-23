package org.akozlov.chapter08

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import sys.process._;

/**
 * Jython engine JSR223 test
 */
object Jython extends App {

  val manager = new ScriptEngineManager();
  val engines = manager.getEngineFactories();
  val engine = new ScriptEngineManager().getEngineByName("jython");

  val startTime0 = System.nanoTime
  for (i <- 1 to 100) {
    engine.eval("from datetime import datetime, timedelta; yesterday = str(datetime.now()-timedelta(days=1))")
    val yesterday = engine.get("yesterday")
  }
  val jsr223Elapsed = 1e-9 * (System.nanoTime - startTime0)

  val startTime1 = System.nanoTime
  for (i <- 1 to 100) {
    val yesterday = Process(Seq("/usr/local/bin/python", "-c", """from datetime import datetime, timedelta; print(datetime.now()-timedelta(days=1))""")).!!
  }
  val processElapsed = 1e-9 * (System.nanoTime - startTime1)

  val startTime2 = System.nanoTime
  for (i <- 1 to 100) {
    val yesterday = Process(Seq("bin/rYesterday.R")).!!
  }
  val rElapsed = 1e-9 * (System.nanoTime - startTime2)

  println(f"JSR223: $jsr223Elapsed; Python: $processElapsed; R: $rElapsed")
}
