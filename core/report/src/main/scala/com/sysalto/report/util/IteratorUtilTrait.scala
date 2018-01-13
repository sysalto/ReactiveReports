package com.sysalto.report.util

import com.sysalto.report.util.IteratorUtil.IterableRecord

trait IteratorUtilTrait {
	implicit class ResultSetToGroup[T](iterator: Iterator[T]) {
		def toGroup: IteratorGroup[T] = IteratorUtil.toGroup[T](iterator)
	}
}

case class IteratorGroup[T](iterator: Iterator[T]) {
	private[this] val crtRecord = IterableRecord[T](None, None, None)

	def foreach(call: IterableRecord[T] => Unit): Unit = {
		while (iterator.hasNext) {
			if (crtRecord.crt.isEmpty) {
				crtRecord.crt = Some(iterator.next())
			} else {
				if (crtRecord.next.nonEmpty) {
					crtRecord.prev = crtRecord.crt
					crtRecord.crt = crtRecord.next
					crtRecord.next = Some(iterator.next())
					call(crtRecord)
				} else {
					crtRecord.next = Some(iterator.next())
					call(crtRecord)
				}
			}
		}
		crtRecord.prev = crtRecord.crt
		crtRecord.crt = crtRecord.next
		crtRecord.next = None
		call(crtRecord)
	}
}

object IteratorUtil {

	case class IterableRecord[T](var prev: Option[T], var crt: Option[T], var next: Option[T])

		def toGroup[T](it:Iterator[T]): IteratorGroup[T] = IteratorGroup[T](it)
}