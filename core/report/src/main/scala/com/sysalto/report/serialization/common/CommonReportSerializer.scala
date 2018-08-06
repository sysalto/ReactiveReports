package com.sysalto.report.serialization.common

import com.sysalto.report.RFontAttribute
import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes._
import com.sysalto.report.serialization._
import com.sysalto.report.serialization.common.ReportCommonProto._

object CommonReportSerializer {

	object ReportColorSerializer {
		def write(input: ReportColor): ReportColor_proto = {
			val builder = ReportColor_proto.newBuilder()
			builder.setR(input.r)
			builder.setG(input.g)
			builder.setB(input.b)
			builder.setOpacity(input.opacity)
			builder.build()
		}

		def read(input: ReportColor_proto): ReportColor = {
			new ReportColor(input.getR, input.getG, input.getB, input.getOpacity)
		}
	}

	object ReportTxtSerializer {
		def write(obj: ReportTxt): ReportTxt_proto = {
			val builder = ReportTxt_proto.newBuilder()
			builder.setTxt(obj.txt)
			builder.setFont(RFontSerializer.write(obj.font))
			builder.build()
		}

		def read(input: ReportTxt_proto): ReportTxt =
			ReportTxt(input.getTxt, RFontSerializer.read(input.getFont))
	}

	object RFontSerializer {
		def write(obj: RFont): RFont_proto = {
			val builder = RFont_proto.newBuilder()
			builder.setSize(obj.size)
			builder.setFontName(obj.fontName)
			builder.setAttribute(RFontAttributeSerializer.write(obj.attribute))
			builder.setColor(RColorSerializer.write(obj.color))
			builder.setExternalFont(OptionRFontFamilySerializer.write(obj.externalFont))
			builder.build()
		}

		def read(input: RFont_proto): RFont =
			RFont(input.getSize, input.getFontName, RFontAttributeSerializer.read(input.getAttribute), RColorSerializer.read(input.getColor),
				OptionRFontFamilySerializer.read(input.getExternalFont))
	}

	object OptionRFontFamilySerializer {
		def write(obj: Option[RFontFamily]): OptionRFontFamily_proto = {
			val builder = OptionRFontFamily_proto.newBuilder()
			if (obj.isEmpty) {
				builder.setNull(true)
			} else {
				builder.setNull(false)
				builder.setRFontFamily(RFontFamilySerializer.write(obj.get))
			}
			builder.build()
		}

		def read(input: OptionRFontFamily_proto): Option[RFontFamily] =
			if (input.getNull) None else Some(RFontFamilySerializer.read(input.getRFontFamily))

	}

	object RFontFamilySerializer {
		def write(obj: RFontFamily): RFontFamily_proto = {
			val builder = RFontFamily_proto.newBuilder()
			builder.setName(obj.name)
			builder.setRegular(obj.regular)
			builder.setBold(OptionStringSerializer.write(obj.bold))
			builder.setItalic(OptionStringSerializer.write(obj.italic))
			builder.setBoldItalic(OptionStringSerializer.write(obj.boldItalic))
			builder.build()
		}

		def read(input: RFontFamily_proto): RFontFamily =
			RFontFamily(input.getName, input.getRegular, OptionStringSerializer.read(input.getBold),
				OptionStringSerializer.read(input.getItalic), OptionStringSerializer.read(input.getBoldItalic))
	}

	object OptionStringSerializer {
		def write(obj: Option[String]): OptionString_proto = {
			val builder = OptionString_proto.newBuilder()
			if (obj.isEmpty) {
				builder.setNull(true)
			} else {
				builder.setNull(false)
				builder.setString(obj.get)
			}
			builder.build()
		}

		def read(input: OptionString_proto): Option[String] =
			if (input.getNull) None else Some(input.getString)

	}

	object RFontAttributeSerializer {
		def write(obj: RFontAttribute.Value): RFontAttribute_proto = {
			obj match {
				case RFontAttribute.NORMAL => RFontAttribute_proto.NORMAL
				case RFontAttribute.BOLD => RFontAttribute_proto.BOLD
				case RFontAttribute.ITALIC => RFontAttribute_proto.ITALIC
				case RFontAttribute.BOLD_ITALIC => RFontAttribute_proto.BOLD_ITALIC
			}
		}

		def read(input: RFontAttribute_proto): RFontAttribute.Value = input match {
			case RFontAttribute_proto.NORMAL => RFontAttribute.NORMAL
			case RFontAttribute_proto.BOLD => RFontAttribute.BOLD
			case RFontAttribute_proto.ITALIC => RFontAttribute.ITALIC
			case RFontAttribute_proto.BOLD_ITALIC => RFontAttribute.BOLD_ITALIC
			case _ => RFontAttribute.NORMAL
		}
	}

	object RColorSerializer {
		def write(obj: ReportColor): RColor_proto = {
			val builder = RColor_proto.newBuilder()
			builder.setR(obj.r)
			builder.setG(obj.g)
			builder.setB(obj.b)
			builder.setOpacity(obj.opacity)
			builder.build()
		}

		def read(input: RColor_proto): ReportColor =
			ReportColor(input.getR, input.getG, input.getB, input.getOpacity)
	}

	object BoundaryRectSerializer {
		def write(obj: BoundaryRect): BoundaryRect_proto = {
			val builder = BoundaryRect_proto.newBuilder()
			builder.setLeft(obj.left)
			builder.setBottom(obj.bottom)
			builder.setRight(obj.right)
			builder.setTop(obj.top)
			builder.build()
		}

		def read(input: BoundaryRect_proto): BoundaryRect =
			new BoundaryRect(input.getLeft, input.getBottom, input.getRight, input.getTop)
	}

	object OptionLineDashTypeSerializer {
		def write(obj: Option[LineDashType]): OptionLineDashType_proto = {
			val builder = OptionLineDashType_proto.newBuilder()
			if (obj.isEmpty) {
				builder.setNull(true)
			} else {
				builder.setNull(false)
				builder.setLineDashType(LineDashTypeSerializer.write(obj.get))
			}
			builder.build()
		}

		def read(input: OptionLineDashType_proto): Option[LineDashType] =
			if (input.getNull) None else Some(LineDashTypeSerializer.read(input.getLineDashType))

	}

	object LineDashTypeSerializer {
		def write(obj: LineDashType): LineDashType_proto = {
			val builder = LineDashType_proto.newBuilder()
			builder.setUnit(obj.unit)
			builder.setPhase(obj.phase)
			builder.build()
		}

		def read(input: LineDashType_proto): LineDashType =
			LineDashType(input.getUnit, input.getPhase)
	}

}
