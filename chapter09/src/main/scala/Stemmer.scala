/*

   Porter stemmer in Scala. The original paper is in

       Porter, 1980, An algorithm for suffix stripping, Program, Vol. 14,
       no. 3, pp 130-137,

   See also http://tartarus.org/martin/PorterStemmer/def.txt

 */

package org.akozlov.chapter09

class Stemmer extends Serializable
{
  // Word to be passed during steps/recursions (to be stemmed).
  var b = ""
  // Just recode the existing stuff, then go through and refactor with some intelligence.
  def cons(i: Int): Boolean = {
    val ch = b(i)
    ch match {
      case 'a' | 'e' | 'i' | 'o' | 'u' => false
      case 'y' => i == 0 || !cons(i-1)
      case _ => true;
    }
  }
  /* m() measures the number of consonant sequences between 0 and j. if c is
      a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
      presence,
         <c><v>       gives 0
         <c>vc<v>     gives 1
         <c>vcvc<v>   gives 2
         ....
   */
  def calcM(l: Int): Int = {
    (0 until l).foldLeft((0, false)) { case(r, s) => if (!cons(s)) (r._1, false) else if (!r._2 && s>0) (r._1 + 1, true) else (r._1, true) } ._1
  }
  // Removing the suffix 's', does a vowel exist?
  def vowelInStem(s: String): Boolean = {
    !(0 until b.length - s.length).forall(cons(_))
  }
  // doublec(j) is true <=> j,(j-1) contain a double consonant
  def doublec(): Boolean = {
    b.length > 1 && b.takeRight(2)(0) == b.takeRight(2)(1) && cons(b.length - 1)
  }
  // the stem ends cvc, where the second c is not W, X or Y
  def cvc(s: String): Boolean = {
    val i = b.length - 1 - s.length; return !(i < 2 || !cons(i) || cons(i-1) || !cons(i-2) || b(i) == 'w' || b(i) == 'x' || b(i) == 'y')
  }
  // Returns true if it did the change
  def replacer(orig: String, replace: String, checker: Int => Boolean ): Boolean = {
    if (b.endsWith(orig) && checker(calcM(b.length - orig.length)))
    { b = b.substring(0, b.length - orig.length) + replace; true }
    else
      false
  }
  // Find the first replacement from the list (including checker condition, the conditional checker for m)
  def processSubList(l:List[(String, String)], checker: Int=>Boolean ): Boolean = {
    l.find(x => replacer(x._1, x._2, checker)) match {
      case Some((x, y))  => true
      case _ => false
    }
  }

  def step1(s: String) = {
    b = s
    // step 1a
    processSubList(List(("sses", "ss"), ("ies","i"), ("ss","ss"), ("s", "")), _>=0)
    // step 1b
    if (!(replacer("eed", "ee", _>0)))
    {
      if ((vowelInStem("ed") && replacer("ed", "", _>=0)) || (vowelInStem("ing") && replacer("ing", "", _>=0)))
      {
        if (!processSubList(List(("at", "ate"), ("bl","ble"), ("iz","ize")), _>=0 ) )
        {
          // if this isn't done, then it gets more confusing.
          if (doublec() && b.last != 'l' && b.last != 's' && b.last != 'z') { b = b.substring(0, b.length - 1) }
          else
            if (calcM(b.length) == 1 && cvc("")) { b = b + "e" }
        }
      }
    }
    // step 1c
    (vowelInStem("y") && replacer("y", "i", _>=0))
    this
  }

  def step2() = {
    processSubList(List(("ational", "ate"),("tional","tion"),("enci","ence"),("anci","ance"),("izer","ize"),("bli","ble"),("alli", "al"),
      ("entli","ent"),("eli","e"),("ousli","ous"),("ization","ize"),("ation","ate"),("ator","ate"),("alism","al"),
      ("iveness","ive"),("fulness","ful"),("ousness", "ous"),("aliti", "al"),("iviti","ive"),("biliti", "ble"),("logi", "log")), _>0)
    this
  }

  def step3() = {
    processSubList(List(("icate", "ic"),("ative",""),("alize","al"),("iciti","ic"),("ical","ic"),("ful",""),("ness","")), _>0)
    this
  }

  def step4() = {
    // first part.
    if (!processSubList(List(("al",""),("ance",""),("ence",""),("er",""),("ic",""),("able",""),("ible",""),("ant",""),("ement",""),("ment",""),("ent","")), _>1 ))
    {
      // special part.
      if (b.length < 4 || !(b(b.length - 4) == 's' && replacer("ion", "", _>1) || b(b.length - 4) == 't') && replacer("ion", "", _>1))
      {
        // third part.
        processSubList(List(("ou",""),("ism",""),("ate",""),("iti",""),("ous",""),("ive",""),("ize","")), _>1)
      }
    }
    this
  }

  def step5a() = {
    replacer("e", "", _>1)
    if (!cvc("e")) replacer("e", "", _==1)
    this
  }

  def step5b() = {
    if (calcM(b.length) > 1 && doublec() && b.endsWith("l")) { b = b.substring(0, b.length - 1) }
    this
  }

  def stem(s: String): String = if (s.length <= 2) s else step1(s).step2.step3.step4.step5a.step5b.b
}

/**
 *
 * Companion Object (singleton): The main program will run stemmer
 *
 * run as:
 *
 * <code>sbt "run-main org.akozlov.chapter09.Stemmer local[2] shakespeare leotolstoy chekhov nytimes nips enron bible"</code>
 *
 * or from spark-shell:
 * <code>Stemmer.main(Array("local[2]", "shakespeare", "leotolstoy", "chekhov", "nytimes", "nips", "enron", "bible"))</code>
 *
 * The word bags datasets can be downloaded from <a href="http://www.gutenberg.org">http://www.gutenberg.org</a> and
 * <a href="http://archive.ics.uci.edu/ml/machine-learning-databases/bag-of-words/">http://archive.ics.uci.edu/ml/machine-learning-databases/bag-of-words</a>
 *
 */
object Stemmer {

  import org.apache.spark._

  def main(args: Array[String]) {

    val stemmer = new Stemmer

    val conf = new SparkConf().
      setAppName("Stemmer").
      setMaster(args(0))

    val sc = new SparkContext(conf)

    val stopwords = scala.collection.immutable.TreeSet(
      "", "i", "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "from", "had", "has", "he", "her", "him", "his", "in", "is", "it", "its", "my", "not", "of", "on", "she", "that", "the", "to", "was", "were", "will", "with", "you"
    ) map { stemmer.stem(_) }

    val bags = for (name <- args.slice(1, args.length)) yield {
      val rdd = sc.textFile(name).map(_.toLowerCase)
      if (name == "nytimes" || name == "nips" || name == "enron")
        rdd.filter(!_.startsWith("zzz_")).flatMap(_.split("_")).map(stemmer.stem(_)).distinct.filter(!stopwords.contains(_)).cache
      else {
        val withCounts = rdd.flatMap(_.split("\\W+")).map(stemmer.stem(_)).filter(!stopwords.contains(_)).map((_, 1)).reduceByKey(_+_)
        val minCount = scala.math.max(1L, 0.0001 * withCounts.count.toLong)
        withCounts.filter(_._2 > minCount).map(_._1).cache
      }
    }

    val cntRoots = (0 until { args.length - 1 }).map(i => Math.sqrt(bags(i).count.toDouble))

    for(l <- 0 until { args.length - 1 }; r <- l until { args.length - 1 }) {
      val cnt = bags(l).intersection(bags(r)).count
      println("The intersect " + args(l+1) + " x " + args(r+1) + " is: " + cnt + " (" + (cnt.toDouble / cntRoots(l) / cntRoots(r)) + ")")
    }

    sc.stop
  }
}
