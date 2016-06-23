package org.akozlov.chapter03

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka._

/**
 *
 * Object KafkaWordCount
 *
 * run as:
 *
 * <code>sbt "run-main org.akozlov.chapter03.KafkaWordCount localhost:2181"</code>
 *
 */
object KafkaWordCount {
  def main(args: Array[String]) {
    // Create the context with a 2 second batch size
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("KafkaWordCount")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("/tmp/kafka_check")
    System.out.println("Opening a Kafka consumer at zk: [" + args(0) + "] for group group-1 and topic example")
    val lines = KafkaUtils.createStream(ssc, args(0), "group-1", Map("example" -> 1), StorageLevel.MEMORY_ONLY)
    val words = lines
      .flatMap(_._2.toLowerCase.split("\\W+"))
      .map(word => (word, 1L))
      .reduceByKeyAndWindow(_+_, _-_, Seconds(6), Seconds(2)).print
    ssc.start()
    ssc.awaitTermination()
  }
}
