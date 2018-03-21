package com.sysalto.render.util

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

object PageTree {
	private[this] val MAX_NBR = 25


	trait PageNode extends Serializable{
		var leafNbr = 0L

		def addChild(child: PageNode): Unit
	}


	def pageTree(input: List[PageNode])(newNode: () => PageNode): PageNode = {
		if (input.lengthCompare(1) == 0) {
			val node = newNode()
			node.addChild(input.head)
			node
		} else {
			pageTreeN(input)(newNode).head
		}
	}

	@tailrec
	private[this] def pageTreeN(input: List[PageNode])(newNode: () => PageNode): List[PageNode] = {
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


}
