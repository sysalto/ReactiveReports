package com.sysalto.report.reportTypes

import scala.annotation.varargs
import scala.collection.JavaConverters._

/**
	* Created by marian on 3/4/17.
	*/

class GroupUtil[T](groupList: List[Group[T]]) {
	@annotation.tailrec
	final def isHeader(name: String, rec: (Option[T], Option[T], Option[T])): Boolean = {
		rec match {
			case (prev: Option[T], crt: Option[T], _: Option[T]) =>
				if (prev.isEmpty) {
					return true
				}
				val findOpt = groupList.zipWithIndex.find { case (item1, _) => item1.name == name }
				if (findOpt.isEmpty) {
					throw new Exception(s"the group $name not found")
				}
				val (item, idx) = findOpt.get
				val fct = item.get
				if (idx == 0) {
					fct(prev.get) != fct(crt.get)
				} else {
					val result = fct(prev.get) != fct(crt.get)
					if (result) {
						result
					} else {
						isHeader(groupList(idx - 1).name, rec)
					}
				}
		}
	}

	@annotation.tailrec
	final def isFooter(name: String, rec: (Option[T], Option[T], Option[T])): Boolean = {
		rec match {
			case (_: Option[T], crt: Option[T], next: Option[T]) =>
				if (next.isEmpty) {
					return true
				}
				val findOpt = groupList.zipWithIndex.find { case (item1, _) => item1.name == name }
				if (findOpt.isEmpty) {
					throw new Exception(s"the group $name not found")
				}
				val (item, idx) = findOpt.get
				val fct = item.get
				if (idx == 0) {
					fct(next.get) != fct(crt.get)
				} else {
					val result = fct(next.get) != fct(crt.get)
					if (result) {
						result
					} else {
						isFooter(groupList(idx - 1).name, rec)
					}
				}
		}
	}


	def isHeader(name: String, rec: Any): Boolean = {
		assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
		isHeader(name, rec.asInstanceOf[(Option[T], Option[T], Option[T])])
	}

	def isFoter(name: String, rec: Any): Boolean = {
		assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
		isFooter(name, rec.asInstanceOf[(Option[T], Option[T], Option[T])])
	}

	//  def isFirstRecord(rec: Any): Boolean = {
	//    assert(rec.isInstanceOf[Tuple3[Any, Any, Any]])
	//    isFirstRecord(rec.asInstanceOf[Tuple3[Option[T], Option[T], Option[T]]])
	//  }
}

object GroupUtil {
	def instance = this

	@varargs def apply[T](list: Group[T]*): GroupUtil[T] = {
		new GroupUtil(list.toList)
	}

	def getRec[T](rec: (Option[T], Option[T], Option[T])): T = {
		rec match {
			case (_: Option[T], crt: Option[T], _: Option[T]) =>
				crt.get
		}
	}

	def getRec[T](rec: Any): T = {
		assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
		getRec(rec.asInstanceOf[(Option[T], Option[T], Option[T])])
	}

	def isFirstRecord[T](rec: (Option[T], Option[T], Option[T])): Boolean = {
		rec._1.isEmpty
	}

	def isLastRecord[T](rec: (Option[T], Option[T], Option[T])): Boolean = {
		rec._3.isEmpty
	}

	def isFirstRecord[T](rec: Any): Boolean = {
		assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
		isFirstRecord(rec.asInstanceOf[(Option[T], Option[T], Option[T])])
	}

	def isLastRecord[T](rec: Any): Boolean = {
		assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
		isLastRecord(rec.asInstanceOf[(Option[T], Option[T], Option[T])])
	}
}