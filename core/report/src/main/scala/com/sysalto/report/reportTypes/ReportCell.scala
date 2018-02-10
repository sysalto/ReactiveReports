/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
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
import com.sysalto.report.{Report, WrapAlign}

/**
	* Created by marian on 3/4/17.
	*/
/*
class for wrapping text
 */
case class ReportCell(txt: List[RText], var margin: RMargin = RMargin(0, 0), var align: WrapAlign.Value = WrapAlign.NO_WRAP) {

	def this(txt: List[RText]) = {
		this(txt, RMargin(0, 0), WrapAlign.NO_WRAP)
	}

	def this(rtext: RText) = {
		this(List(rtext))
	}

	def this(rtext: RText, left: Float, right: Float) = {
		this(List(rtext), RMargin(left, right), WrapAlign.NO_WRAP)
	}

	/*
	align left
	 */
	def leftAlign(): ReportCell = {
		align = WrapAlign.WRAP_LEFT
		this
	}

	/*
	align center
	 */
	def centerAlign(): ReportCell = {
		align = WrapAlign.WRAP_CENTER
		this
	}

	/*
	align right
	 */
	def rightAlign(): ReportCell = {
		align = WrapAlign.WRAP_RIGHT
		this
	}

	/*
	define boundaries
	 */
	def inside(margin: RMargin): ReportCell = {
		this.margin = margin
		this
	}

	def inside(left: Float, right: Float): ReportCell = {
		this.margin = RMargin(left, right)
		this
	}


	/*
	define only left boundary
	 */
	def at(x: Float): ReportCell = {
		margin = RMargin(x, Float.MaxValue)
		this
	}

	def calculate(report: Report): WrapBox = report.wrap(txt, margin.left, report.getY, margin.right, Float.MaxValue, WrapAlign.WRAP_LEFT, simulate=true).get
}

object ReportCell {
	def apply(rtext: RText): ReportCell = ReportCell(List(rtext))

	def apply(list: RTextList): ReportCell = ReportCell(list.list.toList)
}

