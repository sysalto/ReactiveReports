package com.sysalto.report.reportTypes

import com.sysalto.report.ReportTypes.WrapBox
import com.sysalto.report.{Report, WrapAllign, WrapOptions}

/**
	* Created by marian on 3/4/17.
	*/
/*
class for wrapping text
 */
case class RCell(txt: List[RText], var margin: RMargin = RMargin(0, 0), var allign: WrapAllign.Value = WrapAllign.NO_WRAP) {

	def this(txt: List[RText]) = {
		this(txt, RMargin(0, 0), WrapAllign.NO_WRAP)
	}

	def this(rtext: RText) = {
		this(List(rtext))
	}

	def this(rtext: RText, left: Float, right: Float) = {
		this(List(rtext), RMargin(left, right), WrapAllign.NO_WRAP)
	}

	/*
	allign left
	 */
	def leftAllign(): RCell = {
		allign = WrapAllign.WRAP_LEFT
		this
	}

	/*
	allign center
	 */
	def centerAllign(): RCell = {
		allign = WrapAllign.WRAP_CENTER
		this
	}

	/*
	allign right
	 */
	def rightAllign(): RCell = {
		allign = WrapAllign.WRAP_RIGHT
		this
	}

	/*
	define boundaries
	 */
	def between(margin: RMargin): RCell = {
		this.margin = margin
		this
	}

	def between(left: Float, right: Float): RCell = {
		this.margin = RMargin(left, right)
		this
	}


	/*
	define only left boundary
	 */
	def at(x: Float): RCell = {
		margin = RMargin(x, Float.MaxValue)
		this
	}

	def calculate(report: Report): WrapBox = report.wrap(txt, margin.left, report.getY, margin.right, Float.MaxValue, WrapOptions.LIMIT_TO_BOX, WrapAllign.WRAP_LEFT, true).get
}

object RCell {
	def apply(rtext: RText): RCell = RCell(List(rtext))

	def apply(list: RTextList): RCell = RCell(list.list.toList)
}

