package util

/**
  * Created by marian on 5/6/17.
  */
object WordWrap {

  def wrap(input: String, lineWidth: Int): List[String] = {
    val len = input.length
    val MAXL = len + 2
    var str = Array.ofDim[Char](MAXL)
    input.copyToArray(str)
    var memo = Array.ofDim[Int](MAXL)
    var cut = Array.ofDim[Int](MAXL)
    Array.fill(len + 2)(0x7fffffff).copyToArray(memo)
    memo(len + 1) = 0
    for (i <- len - 1 to 0 by -1) {
      val upto = Math.min(i + lineWidth, len)
      for (j <- i + 1 to upto) {
        if (str(j) == ' ' || str(j) == '\u0000') {
          val d = lineWidth - (j - i)
          if (memo(i) > d * d * d + memo(j + 1)) {
            memo(i) = d * d * d + memo(j + 1)
            cut(i) = j
          }
        }
      }
    }
    var i = 0
    while (i < len) {
      str(cut(i)) = '\n'
      i = cut(i) + 1
    }
    val result=new String(str.take(len))
   // println(result)
    result.split("\n").toList
  }


  def main(args: Array[String]): Unit = {
   val l=wrap("aaa bb cc d efg", 6)
    println(l.mkString("\n"))
  }
}
