package pdfGenerator.wrap.smawh
// http://www.geeksforgeeks.org/dynamic-programming-set-18-word-wrap/
/**
  * Created by marian on 2/19/16.
  */
object WordWrap1 {


  def solveWordWrap(l: Array[Int], n: Int, maxLineLength: Int): Unit = {
    var extras = Array.ofDim[Int](n + 1, n + 1)
    var lc = Array.ofDim[Int](n + 1, n + 1)
    var c = Array.ofDim[Int](n + 1)
    var p = Array.ofDim[Int](n + 1)
    // calculate extra spaces in a single line.  The value extra[i][j]
    // indicates extra spaces if words from word number i to j are
    // placed in a single line

    for (i <- 1 to n) {
      extras(i)(i) = maxLineLength - l(i-1)
      for (j <- i+1 to n) {
        extras(i)(j) = extras(i)(j - 1) - l(j - 1) - 1
      }
    }

    for (i <- 1 to n) {
      for (j <- i to n) {
        if (extras(i)(j) < 0) {
          lc(i)(j) = Int.MaxValue
        }
        else {
          if (j == n && extras(i)(j) >= 0) {
            lc(i)(j) = 0
          }
          else {
            lc(i)(j) = extras(i)(j) * extras(i)(j)
          }
        }
      }
    }

    c(0) = 0
    for (j <- 1 to n) {
      c(j) = Int.MaxValue
      for (i <- 1 to j) {
        if (c(i - 1) != Int.MaxValue && lc(i)(j) != Int.MaxValue && (c(i - 1) + lc(i)(j) < c(j))) {
          c(j) = c(i - 1) + lc(i)(j)
          p(j) = i
        }
      }
    }
    println("n:"+n+" P:"+p.mkString(" "))
    printSolution(p, n)
  }


  def printSolution(p: Array[Int], n: Int): Int = {
    var k = 0
    if (p(n) == 1) {
      k = 1
    }
    else {
      k = printSolution(p, p(n) - 1) + 1
    }
    println(s"Line number $k: From word no. ${p(n)} to $n \n")
    k
  }

  def main(args: Array[String]) {
    var l = Array(3,2,2,2,4)
    val n = l.size
    val maxLineLength = 6
    solveWordWrap(l, n,maxLineLength)
  }


}
