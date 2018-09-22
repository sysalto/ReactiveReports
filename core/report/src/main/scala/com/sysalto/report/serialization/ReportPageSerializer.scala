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

package com.sysalto.report.serialization

import com.sysalto.report.{RFontAttribute, WrapAlign}

import scala.collection.JavaConverters._
import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes._
import com.sysalto.report.serialization.ReportProto.ReportItem_proto.FieldCase
import com.sysalto.report.serialization.ReportProto._
import com.sysalto.report.serialization.common.CommonReportSerializer._
import com.sysalto.report.serialization.common.ReportCommonProto._

import scala.collection.mutable.ListBuffer


private[serialization] object ReportLinkToPageSerializer {
	def write(obj: ReportLinkToPage): ReportLinkToPage_proto = {
		val builder = ReportLinkToPage_proto.newBuilder()
		builder.setBoundaryRect(BoundaryRectSerializer.write(obj.boundaryRect))
		builder.setPageNbr(obj.pageNbr)
		builder.setLeft(obj.left)
		builder.setTop(obj.top)
		builder.build()
	}

	def read(input: ReportLinkToPage_proto): ReportLinkToPage =
		new ReportLinkToPage(BoundaryRectSerializer.read(input.getBoundaryRect), input.getPageNbr, input.getLeft, input.getTop)
}

private[serialization] object ReportLinkToUrlSerializer {
	def write(obj: ReportLinkToUrl): ReportLinkToUrl_proto = {
		val builder = ReportLinkToUrl_proto.newBuilder()
		builder.setBoundaryRect(BoundaryRectSerializer.write(obj.boundaryRect))
		builder.setUrl(obj.url)
		builder.build()
	}

	def read(input: ReportLinkToUrl_proto): ReportLinkToUrl =
		new ReportLinkToUrl(BoundaryRectSerializer.read(input.getBoundaryRect), input.getUrl)
}


private[serialization] object ReportTextSerializer {
	def write(obj: ReportText): ReportText_proto = {
		val builder = ReportText_proto.newBuilder()
		builder.setTxt(ReportTxtSerializer.write(obj.txt))
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder.build()
	}

	def read(input: ReportText_proto): ReportText =
		new ReportText(ReportTxtSerializer.read(input.getTxt), input.getX, input.getY)
}


private[serialization] object ReportTextAlignedSerializer {
	def write(obj: ReportTextAligned): ReportTextAligned_proto = {
		val builder = ReportTextAligned_proto.newBuilder()
		builder.setRText(ReportTxtSerializer.write(obj.rText))
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder.setIndex(obj.index)
		builder.build()
	}

	def read(input: ReportTextAligned_proto): ReportTextAligned =
		new ReportTextAligned(ReportTxtSerializer.read(input.getRText), input.getX, input.getY, input.getIndex)
}


private[serialization] object ReportImageSerializer {
	def write(obj: ReportImage): ReportImage_proto = {
		val builder = ReportImage_proto.newBuilder()
		builder.setFile(obj.file)
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder.setWidth(obj.width)
		builder.setHeight(obj.height)
		builder.setOpacity(obj.opacity)
		builder.build()
	}

	def read(input: ReportImage_proto): ReportImage =
		new ReportImage(input.getFile, input.getX, input.getY, input.getWidth, input.getHeight, input.getOpacity)
}


private[serialization] object ReportLineSerializer {
	def write(obj: ReportLine): ReportLine_proto = {
		val builder = ReportLine_proto.newBuilder()
		builder.setX1(obj.x1)
		builder.setY1(obj.y1)
		builder.setX2(obj.x2)
		builder.setY2(obj.y2)
		builder.setLineWidth(obj.lineWidth)
		builder.setColor(RColorSerializer.write(obj.color))
		builder.setLineDashType(OptionLineDashTypeSerializer.write(obj.lineDashType))
		builder.build()
	}

	def read(input: ReportLine_proto): ReportLine =
		new ReportLine(input.getX1, input.getY1, input.getX2, input.getY2, input.getLineWidth, RColorSerializer.read(input.getColor), OptionLineDashTypeSerializer.read(input.getLineDashType))
}


private[serialization] object OptionRColorSerializer {
	def write(obj: Option[ReportColor]): OptionRColor_proto = {
		val builder = OptionRColor_proto.newBuilder()
		if (obj.isEmpty) {
			builder.setNull(true)
		} else {
			builder.setNull(false)
			builder.setRColor(RColorSerializer.write(obj.get))
		}
		builder.build()
	}

	def read(input: OptionRColor_proto): Option[ReportColor] =
		if (input.getNull) None else Some(RColorSerializer.read(input.getRColor))

}

private[serialization] object ReportRectangleSerializer {
	def write(obj: ReportRectangle): ReportRectangle_proto = {
		val builder = ReportRectangle_proto.newBuilder()
		builder.setX1(obj.x1)
		builder.setY1(obj.y1)
		builder.setX2(obj.x2)
		builder.setY2(obj.y2)
		builder.setRadius(obj.radius)
		builder.setColor(OptionRColorSerializer.write(obj.color))
		builder.setFillColor(OptionRColorSerializer.write(obj.fillColor))
		builder.build()
	}

	def read(input: ReportRectangle_proto): ReportRectangle =
		new ReportRectangle(input.getX1, input.getY1, input.getX2, input.getY2, input.getRadius, OptionRColorSerializer.read(input.getColor), OptionRColorSerializer.read(input.getFillColor))
}


private[serialization] object DRectangleSerializer {
	def write(obj: DRectangle): DRectangle_proto = {
		val builder = DRectangle_proto.newBuilder()
		builder.setX1(obj.x1)
		builder.setY1(obj.y1)
		builder.setX2(obj.x2)
		builder.setY2(obj.y2)
		builder.setRadius(obj.radius)
		builder.build()
	}

	def read(input: DRectangle_proto): DRectangle =
		new DRectangle(input.getX1, input.getY1, input.getX2, input.getY2, input.getRadius)
}

private[serialization] object ReportVerticalShadeSerializer {
	def write(obj: ReportVerticalShade): ReportVerticalShade_proto = {
		val builder = ReportVerticalShade_proto.newBuilder()
		builder.setRectangle(DRectangleSerializer.write(obj.rectangle))
		builder.setFrom(RColorSerializer.write(obj.from))
		builder.setTo(RColorSerializer.write(obj.to))
		builder.build()
	}

	def read(input: ReportVerticalShade_proto): ReportVerticalShade =
		new ReportVerticalShade(DRectangleSerializer.read(input.getRectangle), RColorSerializer.read(input.getFrom), RColorSerializer.read(input.getTo))
}

private[serialization] object WrapAlignSerializer {
	def write(obj: WrapAlign.Value): WrapAlign_proto = {
		obj match {
			case WrapAlign.NO_WRAP => WrapAlign_proto.NO_WRAP
			case WrapAlign.WRAP_LEFT => WrapAlign_proto.WRAP_LEFT
			case WrapAlign.WRAP_RIGHT => WrapAlign_proto.WRAP_RIGHT
			case WrapAlign.WRAP_CENTER => WrapAlign_proto.WRAP_CENTER
			case WrapAlign.WRAP_JUSTIFIED => WrapAlign_proto.WRAP_JUSTIFIED
		}
	}

	def read(input: WrapAlign_proto): WrapAlign.Value = input match {
		case WrapAlign_proto.NO_WRAP => WrapAlign.NO_WRAP
		case WrapAlign_proto.WRAP_LEFT => WrapAlign.WRAP_LEFT
		case WrapAlign_proto.WRAP_RIGHT => WrapAlign.WRAP_RIGHT
		case WrapAlign_proto.WRAP_CENTER => WrapAlign.WRAP_CENTER
		case WrapAlign_proto.WRAP_JUSTIFIED => WrapAlign.WRAP_JUSTIFIED
		case _ => WrapAlign.NO_WRAP
	}
}

private[serialization] object OptionFloatSerializer {
	def write(obj: Option[Float]): OptionFloat_proto = {
		val builder = OptionFloat_proto.newBuilder()
		if (obj.isEmpty) {
			builder.setNull(true)
		} else {
			builder.setNull(false)
			builder.setFloat(obj.get)
		}
		builder.build()
	}

	def read(input: OptionFloat_proto): Option[Float] =
		if (input.getNull) None else Some(input.getFloat)

}

private[serialization] object ReportTextWrapSerializer {
	def write(obj: ReportTextWrap): ReportTextWrap_proto = {
		val builder = ReportTextWrap_proto.newBuilder()

		obj.text.foreach(item => {
			val result = ReportTxtSerializer.write(item)
			builder.addText(result)
		})

		builder.setX0(obj.x0)
		builder.setY0(obj.y0)
		builder.setX1(obj.x1)
		builder.setY1(obj.y1)
		builder.setWrapAlign(WrapAlignSerializer.write(obj.wrapAlign))
		builder.build()
	}

	def read(input: ReportTextWrap_proto): ReportTextWrap =
		new ReportTextWrap(input.getTextList.asScala.map(item => {
			ReportTxtSerializer.read(item)
		}).toList, input.getX0, input.getY0, input.getX1, input.getY1, WrapAlignSerializer.read(input.getWrapAlign))
}

private[serialization] object StringDoubleSerializer {
	def write(obj: (String, Double)): StringDouble_proto = {
		val builder = StringDouble_proto.newBuilder()
		builder.setValue1(obj._1)
		builder.setValue2(obj._2)
		builder.build()
	}

	def read(input: StringDouble_proto): (String, Double) =
		(input.getValue1, input.getValue2)
}

private[serialization] object DoubleStringStringSerializer {
	def write(obj: (Double, String, String)): DoubleStringString_proto = {
		val builder = DoubleStringString_proto.newBuilder()
		builder.setValue1(obj._1)
		builder.setValue2(obj._2)
		builder.setValue3(obj._3)
		builder.build()
	}

	def read(input: DoubleStringString_proto): (Double, String, String) =
		(input.getValue1, input.getValue2, input.getValue3)
}



private[serialization] object ReportBarChartSerializer {
	def write(obj: ReportBarChart): ReportBarChart_proto = {
		val builder = ReportBarChart_proto.newBuilder()
		builder.setTitle(obj.title)
		builder.setXLabel(obj.xLabel)
		builder.setYLabel(obj.yLabel)

		obj.data.foreach(item => {
			val result = DoubleStringStringSerializer.write(item)
			builder.addData(result)
		})

		builder.setX0(obj.x0)
		builder.setY0(obj.y0)
		builder.setWidth(obj.width)
		builder.setHeight(obj.height)
		builder.build()
	}

	def read(input: ReportBarChart_proto): ReportBarChart =
		new ReportBarChart(input.getTitle, input.getXLabel, input.getYLabel, input.getDataList.asScala.map(item => {
			DoubleStringStringSerializer.read(item)
		}).toList, input.getX0, input.getY0, input.getWidth, input.getHeight)
}


object DirectDrawMovePointSerializer {
	def write(obj: DirectDrawMovePoint): DirectDrawMovePoint_proto = {
		val builder = DirectDrawMovePoint_proto.newBuilder()
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder.build()
	}

	def read(input: DirectDrawMovePoint_proto): DirectDrawMovePoint =
		new DirectDrawMovePoint(input.getX, input.getY)

}


object DirectDrawLineSerializer {
	def write(obj: DirectDrawLine): DirectDrawLine_proto = {
		val builder = DirectDrawLine_proto.newBuilder()
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder.build()
	}

	def read(input: DirectDrawLine_proto): DirectDrawLine =
		new DirectDrawLine(input.getX, input.getY)
}


object DirectDrawSerializer {
	def write(obj: DirectDraw): DirectDraw_proto = {
		val builder = DirectDraw_proto.newBuilder()
		builder.setCode(obj.code)
		builder.build()
	}

	def read(input: DirectDraw_proto): DirectDraw =
		new DirectDraw(input.getCode)
}

object DirectDrawFillSerializer {
	def write(obj: DirectDrawFill): DirectDrawFill_proto = {
		val builder = DirectDrawFill_proto.newBuilder()
		builder.setColor(ReportColorSerializer.write(obj.reportColor))
		builder.build()
	}

	def read(input: DirectDrawFill_proto): DirectDrawFill = {
		val result = new DirectDrawFill(ReportColorSerializer.read(input.getColor))
		result
	}
}

object DirectDrawClosePathSerializer {
	def write(obj: DirectDrawClosePath): DirectDrawClosePath_proto = {
		val builder = DirectDrawClosePath_proto.newBuilder()
		builder.build()
	}

	def read(input: DirectDrawClosePath_proto): DirectDrawClosePath = {
		val result = new DirectDrawClosePath()
		result
	}
}

object DirectDrawStrokeSerializer {
	def write(obj: DirectDrawStroke): DirectDrawStroke_proto = {
		val builder = DirectDrawStroke_proto.newBuilder()
		builder.setColor(ReportColorSerializer.write(obj.reportColor))
		builder.build()
	}

	def read(input: DirectDrawStroke_proto): DirectDrawStroke = {
		val result = new DirectDrawStroke(ReportColorSerializer.read(input.getColor))
		result
	}
}

object DirectDrawCircleSerializer {
	def write(obj: DirectDrawCircle): DirectDrawCircle_proto = {
		val builder = DirectDrawCircle_proto.newBuilder()
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder.setRadius(obj.radius)
		builder.build()
	}

	def read(input: DirectDrawCircle_proto): DirectDrawCircle =
		new DirectDrawCircle(input.getX, input.getY, input.getRadius)
}

object DirectDrawArcSerializer {
	def write(obj: DirectDrawArc): DirectDrawArc_proto = {
		val builder = DirectDrawArc_proto.newBuilder()
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder.setRadius(obj.radius)
		builder.setStartAngle(obj.startAngle)
		builder.setEndAngle(obj.endAngle)
		builder.build()
	}

	def read(input: DirectDrawArc_proto): DirectDrawArc =
		new DirectDrawArc(input.getX, input.getY, input.getRadius, input.getStartAngle, input.getEndAngle)
}

private[serialization] object DirectFillStrokeSerializer {
	def write(obj: DirectFillStroke): DirectFillStroke_proto = {
		val builder = DirectFillStroke_proto.newBuilder()
		builder.setFill(obj.fill)
		builder.setStroke(obj.stroke)
		builder.build()
	}

	def read(input: DirectFillStroke_proto): DirectFillStroke =
		new DirectFillStroke(input.getFill, input.getStroke)
}

private[serialization] object DirectDrawRectangleSerializer {
	def write(obj: DirectDrawRectangle): DirectDrawRectangle_proto = {
		val builder = DirectDrawRectangle_proto.newBuilder()
		builder.setX1(obj.x1)
		builder.setY1(obj.y1)
		builder.setX2(obj.x2)
		builder.setY2(obj.y2)
		builder.build()
	}

	def read(input: DirectDrawRectangle_proto): DirectDrawRectangle =
		new DirectDrawRectangle(input.getX1, input.getY1, input.getX2, input.getY2)
}


object ReportPageSerializer {

	def write(page: ReportPage): Array[Byte] = {
		val builder = ReportPage_proto.newBuilder()
		page.items.foreach(item => {
			val builderItem = ReportItem_proto.newBuilder()
			builderItem.setDeltaY(item.deltaY)
			item match {
				case obj: ReportLinkToPage => {
					val result = ReportLinkToPageSerializer.write(obj)
					builderItem.setReportLinkToPage(result)
				}
				case obj: ReportLinkToUrl => {
					val result = ReportLinkToUrlSerializer.write(obj)
					builderItem.setReportLinkToUrl(result)
				}
				case obj: ReportText => {
					val result = ReportTextSerializer.write(obj)
					builderItem.setReportText(result)
				}
				case obj: ReportTextAligned => {
					val result = ReportTextAlignedSerializer.write(obj)
					builderItem.setReportTextAligned(result)
				}
				case obj: ReportImage => {
					val result = ReportImageSerializer.write(obj)
					builderItem.setReportImage(result)
				}
				case obj: ReportLine => {
					val result = ReportLineSerializer.write(obj)
					builderItem.setReportLine(result)
				}
				case obj: ReportRectangle => {
					val result = ReportRectangleSerializer.write(obj)
					builderItem.setReportRectangle(result)
				}
				case obj: ReportVerticalShade => {
					val result = ReportVerticalShadeSerializer.write(obj)
					builderItem.setReportVerticalShade(result)
				}
				case obj: ReportTextWrap => {
					val result = ReportTextWrapSerializer.write(obj)
					builderItem.setReportTextWrap(result)
				}
				case obj: ReportBarChart => {
					val result = ReportBarChartSerializer.write(obj)
					builderItem.setReportBarChart(result)
				}
				case obj: DirectDrawMovePoint => {
					val result = DirectDrawMovePointSerializer.write(obj)
					builderItem.setDirectDrawMovePoint(result)
				}
				case obj: DirectDrawLine => {
					val result = DirectDrawLineSerializer.write(obj)
					builderItem.setDirectDrawLine(result)
				}
				case obj: DirectDraw => {
					val result = DirectDrawSerializer.write(obj)
					builderItem.setDirectDraw(result)
				}
				case obj: DirectDrawCircle => {
					val result = DirectDrawCircleSerializer.write(obj)
					builderItem.setDirectDrawCircle(result)
				}
				case obj: DirectDrawArc => {
					val result = DirectDrawArcSerializer.write(obj)
					builderItem.setDirectDrawArc(result)
				}
				case obj: DirectFillStroke => {
					val result = DirectFillStrokeSerializer.write(obj)
					builderItem.setDirectFillStrokeProto(result)
				}
				case obj: DirectDrawRectangle => {
					val result = DirectDrawRectangleSerializer.write(obj)
					builderItem.setDirectDrawRectangleProto(result)
				}
				case obj: DirectDrawFill => {
					val result = DirectDrawFillSerializer.write(obj)
					builderItem.setDirectDrawFill(result)
				}
				case obj: DirectDrawClosePath => {
					val result = DirectDrawClosePathSerializer.write(obj)
					builderItem.setDirectDrawClosePathProto(result)
				}
				case obj: DirectDrawStroke => {
					val result = DirectDrawStrokeSerializer.write(obj)
					builderItem.setDirectDrawStroke(result)
				}
				case _ => {
					throw new Exception("Unimplemented :" + item)
				}
			}
			builder.addItem(builderItem.build())
		})
		builder.build().toByteArray
	}

	def read(array: Array[Byte]): ReportPage = {
		val obj = ReportPage_proto.parseFrom(array)
		val l1 = obj.getItemList.asScala
		val result = l1.map(item => {
			val reportItem: ReportItem = item.getFieldCase match {
				case FieldCase.REPORTLINKTOPAGE => ReportLinkToPageSerializer.read(item.getReportLinkToPage)
				case FieldCase.REPORTLINKTOURL => ReportLinkToUrlSerializer.read(item.getReportLinkToUrl)
				case FieldCase.REPORTTEXT => ReportTextSerializer.read(item.getReportText)
				case FieldCase.REPORTTEXTALIGNED => ReportTextAlignedSerializer.read(item.getReportTextAligned)
				case FieldCase.REPORTIMAGE => ReportImageSerializer.read(item.getReportImage)
				case FieldCase.REPORTLINE => ReportLineSerializer.read(item.getReportLine)
				case FieldCase.REPORTRECTANGLE => ReportRectangleSerializer.read(item.getReportRectangle)
				case FieldCase.REPORTVERTICALSHADE => ReportVerticalShadeSerializer.read(item.getReportVerticalShade)
				case FieldCase.REPORTTEXTWRAP => ReportTextWrapSerializer.read(item.getReportTextWrap)
				case FieldCase.REPORTBARCHART => ReportBarChartSerializer.read(item.getReportBarChart)
				case FieldCase.DIRECTDRAWMOVEPOINT => DirectDrawMovePointSerializer.read(item.getDirectDrawMovePoint)
				case FieldCase.DIRECTDRAWLINE => DirectDrawLineSerializer.read(item.getDirectDrawLine)
				case FieldCase.DIRECTDRAW => DirectDrawSerializer.read(item.getDirectDraw)
				case FieldCase.DIRECTFILLSTROKE_PROTO => DirectFillStrokeSerializer.read(item.getDirectFillStrokeProto)
				case FieldCase.DIRECTDRAWCIRCLE => DirectDrawCircleSerializer.read(item.getDirectDrawCircle)
				case FieldCase.DIRECTDRAWARC => DirectDrawArcSerializer.read(item.getDirectDrawArc)
				case FieldCase.DIRECT_DRAW_FILL => DirectDrawFillSerializer.read(item.getDirectDrawFill)
				case FieldCase.DIRECT_DRAW_CLOSE_PATH_PROTO => DirectDrawClosePathSerializer.read(item.getDirectDrawClosePathProto)
				case FieldCase.DIRECT_DRAW_STROKE => DirectDrawStrokeSerializer.read(item.getDirectDrawStroke)
				case FieldCase.DIRECTDRAWRECTANGLE_PROTO => DirectDrawRectangleSerializer.read(item.getDirectDrawRectangleProto)
				case _ => {
					throw new Exception("Unimplemented :" + item.getFieldCase)
					null
				}
			}
			reportItem.deltaY = item.getDeltaY
			reportItem
		})
		new ReportPage(ListBuffer(result.toList: _*))
	}

}
