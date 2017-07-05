package util

import com.sysalto.render._

import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 06/01/16.
  */
object PageTree {

  val MAX_NBR = 25
  var leafList: List[Long] = List()


  def make(list: List[Long]): List[Long] = {
    leafList = list
    build(list.size)
  }

  def build(n: Long): List[Long] = {
    val n1 = n / MAX_NBR.toInt
    val level_nbr = if (n % MAX_NBR == 0) n1 else n1 + 1
    if (level_nbr > 1) {
      build(level_nbr) ::: List(level_nbr)
    } else {
      List(level_nbr)
    }
  }


  def displayRow(leafNbr: Long, parentList: List[Long], objNbrList: List[Long], childrenList: List[Long],
                 isLeaf: Boolean, level: Long)
                (writeNode: (Option[Long], Long, List[Long], Long, Boolean) => Unit): Unit = {
    val l1 = objNbrList.zipWithIndex.groupBy {
      case (value, index) =>
        index / MAX_NBR

    }
    val l2 = l1.map { case (index, list) =>
      (if (index > parentList.size - 1) None else Some(parentList(index)), list)
    }.toList
    val l3 = l2.sortBy(f => f._1)


    val childrenList1 = childrenList.zipWithIndex.groupBy {
      case (value, index) =>
        index / MAX_NBR

    }


    val childrenList2 = childrenList1.map { case (index, list) =>
      objNbrList(index) -> list.map(f => f._1)
    }

    val l4 = l3.flatMap { case (parent, list) => list.map(item => (parent, item)) }
    val totalChildNbr = l4.size
    var leafRest = leafNbr
    l4.foreach {
      case (parent, item) => {
        val isRightNode = (totalChildNbr - 1) == item._2
        val leafNodeNbr = getLeafNbr(leafRest, level, totalChildNbr, item._2, isRightNode)
        leafRest = leafRest - leafNodeNbr
        writeNode(parent, item._1, childrenList2.getOrElse(item._1, List()), leafNodeNbr, isLeaf)
      }
    }
  }


  def display(leafNbr: Long, parentList: List[Long], list: List[Long], objectNbrList: Option[List[Long]] = None)
             (getNextNumberFct: () => Long)(writeNode: (Option[Long], Long, List[Long], Long, Boolean) => Unit): Unit = {
    if (list.isEmpty) {
      val objNbrList = leafList
      displayRow(leafNbr, parentList, objNbrList, List(), true, list.size)(writeNode)
      return
    }
    val head = list.head
    val objNbrList = if (objectNbrList.isDefined) objectNbrList.get
    else (for {
      i <- 1L to head
      crtObjNbr = getNextNumberFct()
    } yield crtObjNbr).toList

    val childrenListNbr = if (list.tail.isEmpty) 0 else list.tail.head
    val childrenList1 = if (childrenListNbr == 0) leafList
    else (for {
      i <- 1L to childrenListNbr
      crtObjNbr = getNextNumberFct()
    } yield crtObjNbr).toList


    displayRow(leafNbr, parentList, objNbrList, childrenList1, false, list.size)(writeNode)

    display(leafNbr, objNbrList, list.tail, Some(childrenList1))(getNextNumberFct)(writeNode)
  }

  def getLeafNbr(leafNbr: Long, level: Long, totalChildNbr: Long, childNbr: Long, isRightNode: Boolean): Long = {
    if (!isRightNode) Math.pow(MAX_NBR.toDouble, level.toDouble).toLong
    else {
      leafNbr
    }
  }


  class Node(var id: Long, var parent: Option[Node] = None, var children: List[Node] = List())


  def generatePdfCode(pageList: List[PdfPage])(getNextNumberFct: () => Long)(implicit result: ListBuffer[PdfBaseItem]): PdfPageList = {
    var root: PdfPageList = null
    val listObj = pageList.map(pg => pg.id)
    val list = make(listObj)
    val pageMap = pageList.map(page => (page.id -> page)).toMap

    display(listObj.size, List(), list)(getNextNumberFct) {
      (parent: Option[Long], nodeId: Long, children: List[Long], leafNbr: Long, isleaf: Boolean) => {
        if (!isleaf) {
          val pageList = new PdfPageList(nodeId, parent, children)
          if (parent.isEmpty) {
            root = pageList
          }
        } else {
          if (pageMap.get(nodeId).isDefined) {
            pageMap.get(nodeId).get.pdfPageListId = parent.get
          }
        }
      }
    }


    root
  }


  def main(args: Array[String]) {
//    var currentObjNbr = 0L
//
//    def getNextNumber = {
//      currentObjNbr += 1
//      currentObjNbr
//    }
//
//    implicit val result = new ListBuffer[PdfBaseItem]()
//    currentObjNbr = 1000L
//    val pageList = for (i <- 1 to 2) yield new PdfPage(getNextNumber)
//    val root = generatePdfCode(pageList.toList) {
//      () => getNextNumber
//    }(result)
//
//    println(result.mkString("\n"))
//    println("ROOT:" + root)
  }

}
