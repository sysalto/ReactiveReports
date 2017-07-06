/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
  *
 * Unless you have purchased a commercial license agreement from SysAlto
 * the following license terms apply:
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */



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

