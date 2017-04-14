package pdfGenerator

/**
  * Created by marian on 06/01/16.
  */
object PageTreeOld {
  val MAX_NBR = 25
  var currentObjNbr = 0L
  var leafList: List[Long] = List()

  def getNextNumber = {
    currentObjNbr += 1
    currentObjNbr
  }

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




  def main(args: Array[String]) {
    currentObjNbr = 1000L
    val listObj = (100L to 102L).toList
    val list = make(listObj)

    println("Levels size:" + list)
    display(listObj.size, List(), list) {
      () => getNextNumber
    } {
      (parent: Option[Long], nodeId: Long, children: List[Long], leafNbr: Long, isleaf: Boolean) => {
        println("-" * 30)
        if (isleaf) {
          println("LEAF")
        } else {
          println("NODE")
        }
        if (parent.isDefined) {
          print("PARENT:" + parent.get)
        }
        println(" ID:" + nodeId)
        if (!isleaf) {
          println("children:" + children)
          println("Leaf nbr:" + leafNbr)
        }
      }
    }
  }

}
