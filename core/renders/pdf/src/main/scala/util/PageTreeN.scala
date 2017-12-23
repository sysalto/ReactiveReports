package util

import com.sysalto.render.{PdfBaseItem, PdfPage, PdfPageList}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

object PageTreeN {
	private[this] val MAX_NBR = 25


	trait PageNode {
		var leafNbr = 0L

		def addChild(child: PageNode): Unit
	}


	def pageTree(input: List[PageNode])(newNode: () => PageNode): PageNode = {
		pageTreeN(input)(newNode)(0)
	}

	@tailrec
	private def pageTreeN(input: List[PageNode])(newNode: () => PageNode): List[PageNode] = {
		if (input.lengthCompare(1) == 0) {
			input
		} else {
			pageTreeN(input.grouped(MAX_NBR).map(items => {
				val node = newNode()
				items.foreach(item => node.addChild(item))
				node
			}).toList)(newNode)
		}
	}

	def testPageTree(): Unit = {
		var i = 0

		def newId() = {
			i = i + 1
			i
		}

		case class RPage() extends PageNode {
			var parent: Option[PageNode] = None
			val children: ListBuffer[PageNode] = ListBuffer()
			var id = 0L

			override def addChild(child: PageNode): Unit = {
				child match {
					case rPage: RPage => {
						rPage.parent = Some(this)
						children += child
						leafNbr += child.leafNbr
					}
				}
			}
		}

		def newNode(): PageNode = {
			val node = RPage()
			node.id = newId()
			node
		}


		val list = for (i <- 1 to 27) yield RPage()
		list.foreach(item => item.leafNbr = 1)
		val t1 = System.currentTimeMillis()
		val root = pageTree(list.toList) {
			() => {
				val node = RPage()
				node.id = newId()
				node
			}
		}
		val t2 = System.currentTimeMillis()
		println((t2 - t1) * 0.001)

	}


	def testPageTree1(): Unit = {
		implicit val result = new ListBuffer[PdfBaseItem]()
		var currentObjNbr = 1000L

		def getNextNumber() = {
			currentObjNbr += 1
			currentObjNbr
		}

		val pageList = for (i <- 1 to 26) yield new PdfPage(getNextNumber(), 0, 0, 0)

		val t1 = System.currentTimeMillis()
		val root = pageTree(pageList.toList) {
			() => {
				new PdfPageList(getNextNumber())
			}
		}
		val t2 = System.currentTimeMillis()
		println("Time:" + (t2 - t1) * 0.001)
	}

	def main(args: Array[String]) {

		testPageTree1()
	}
}
