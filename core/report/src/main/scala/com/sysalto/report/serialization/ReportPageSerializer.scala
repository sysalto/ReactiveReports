package com.sysalto.report.serialization

import scala.collection.JavaConverters._
import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes._
import com.sysalto.report.serialization.Report.ReportItem_proto.FieldCase
import com.sysalto.report.serialization.Report._

import scala.collection.mutable.ListBuffer

private[serialization] object OptionStringSerializer {
	def write(obj: Option[String]): OptionString.Builder = {
		val builder = OptionString.newBuilder()
		if (obj.isEmpty) {
			builder.setNull(true)
		} else {
			builder.setNull(false)
			builder.setValue(obj.get)
		}
		builder
	}

	def read(input: OptionString): Option[String] =
		if (input.getNull) None else Some(input.getValue)
}


private[serialization] object OptionFloatSerializer {
	def write(obj: Option[Float]): OptionFloat.Builder = {
		val builder = OptionFloat.newBuilder()
		if (obj.isEmpty) {
			builder.setNull(true)
		} else {
			builder.setNull(false)
			builder.setValue(obj.get)
		}
		builder
	}

	def read(input: OptionFloat): Option[Float] =
		if (input.getNull) None else Some(input.getValue)
}

private[serialization] object OptionRColorSerializer {
	def write(obj: Option[RColor]): OptionRColor.Builder = {
		val builder = OptionRColor.newBuilder()
		if (obj.isEmpty) {
			builder.setNull(true)
		} else {
			builder.setNull(false)
			builder.setValue(RColorSerializer.write(obj.get))
		}
		builder
	}

	def read(input: OptionRFontFamily): Option[RFontFamily] =
		if (input.getNull) None else Some(RFontFamilySerializer.read(input.getValue))
}


private[serialization] object OptionLineDashTypeSerializer {
	def write(obj: Option[LineDashType]): OptionLineDashType.Builder = {
		val builder = OptionLineDashType.newBuilder()
		if (obj.isEmpty) {
			builder.setNull(true)
		} else {
			builder.setNull(false)
			builder.setValue(LineDashTypeSerializer.write(obj.get))
		}
		builder
	}

	def read(input: OptionLineDashType): Option[LineDashType] =
		if (input.getNull) None else Some(LineDashTypeSerializer.read(input.getValue))
}


private[serialization] object OptionRFontFamilySerializer {
	def write(obj: Option[RFontFamily]): OptionRFontFamily.Builder = {
		val builder = OptionRFontFamily.newBuilder()
		if (obj.isEmpty) {
			builder.setNull(true)
		} else {
			builder.setNull(false)
			builder.setValue(RFontFamilySerializer.write(obj.get))
		}
		builder
	}

	def read(input: OptionRFontFamily): Option[RFontFamily] =
		if (input.getNull) None else Some(RFontFamilySerializer.read(input.getValue))
}


private[serialization] object RColorSerializer {
	def write(obj: RColor): RColor_proto.Builder = {
		val builder = RColor_proto.newBuilder()
		builder.setR(obj.r)
		builder.setG(obj.g)
		builder.setB(obj.b)
		builder.setOpacity(obj.opacity)
		builder
	}

	def read(input: RColor_proto): RColor =
		RColor(input.getR, input.getG, input.getB, input.getOpacity)
}

private[serialization] object LineDashTypeSerializer {
	def write(obj: LineDashType): LineDashType_proto.Builder = {
		val builder = LineDashType_proto.newBuilder()
		builder.setUnit(obj.unit)
		builder.setPhase(obj.phase)
		builder
	}

	def read(input: LineDashType_proto): LineDashType =
		LineDashType(input.getUnit, input.getPhase)
}

private[serialization] object RFontFamilySerializer {
	def write(obj: RFontFamily): RFontFamily_proto.Builder = {
		val builder = RFontFamily_proto.newBuilder()
		builder.setName(obj.name)
		builder.setRegular(obj.regular)
		builder.setBold(OptionStringSerializer.write(obj.bold))
		builder.setItalic(OptionStringSerializer.write(obj.italic))
		builder.setBoldItalic(OptionStringSerializer.write(obj.boldItalic))
		builder
	}

	def read(input: RFontFamily_proto): RFontFamily =
		RFontFamily(input.getName, input.getRegular, OptionStringSerializer.read(input.getBold),
			OptionStringSerializer.read(input.getItalic), OptionStringSerializer.read(input.getBoldItalic))
}


private[serialization] object BoundaryRectSerializer {
	def write(obj: BoundaryRect): BoundaryRect_proto.Builder = {
		val builder = BoundaryRect_proto.newBuilder()
		builder.setLeft(obj.left)
		builder.setBottom(obj.bottom)
		builder.setRight(obj.right)
		builder.setTop(obj.top)
		builder
	}

	def read(input: BoundaryRect_proto): BoundaryRect =
		BoundaryRect(input.getLeft, input.getBottom, input.getRight, input.getTop)

}

private[serialization] object ReportLinkToPageSerializer {
	def write(obj: ReportLinkToPage): ReportLinkToPage_proto.Builder = {
		val builder = ReportLinkToPage_proto.newBuilder()
		builder.setBoundaryRect(BoundaryRectSerializer.write(obj.boundaryRect))
		builder.setPageNbr(obj.pageNbr)
		builder.setLeft(obj.left)
		builder.setTop(obj.top)
		builder
	}

	def read(input: ReportLinkToPage_proto): ReportLinkToPage =
		ReportLinkToPage(BoundaryRectSerializer.read(input.getBoundaryRect), input.getPageNbr, input.getLeft, input.getTop)
}

private[serialization] object ReportLinkToUrlSerializer {
	def write(obj: ReportLinkToUrl): ReportLinkToUrl_proto.Builder = {
		val builder = ReportLinkToUrl_proto.newBuilder()
		builder.setBoundaryRect(BoundaryRectSerializer.write(obj.boundaryRect))
		builder.setUrl(obj.url)
		builder
	}

	def read(input: ReportLinkToUrl_proto): ReportLinkToUrl =
		ReportLinkToUrl(BoundaryRectSerializer.read(input.getBoundaryRect), input.getUrl)
}


private[serialization] object RFontSerializer {
	def write(obj: RFont): RFont_proto.Builder = {
		val builder = RFont_proto.newBuilder()
		builder.setSize(obj.size)
		builder.setFontName(obj.fontName)
		//builder.setAttribute(obj.attribute)
		builder.setColor(RColorSerializer.write(obj.color))
		builder.setExternalFont(OptionRFontFamilySerializer.write(obj.externalFont))
		builder
	}

	def read(input: RFont_proto): RFont = null

	//		RFont (input.getSize,input.getFontName,input.getAttribute,input.getColor,input.getExternalFont)
}

private[serialization] object RTextSerializer {
	def write(obj: RText): RText_proto.Builder = {
		val builder = RText_proto.newBuilder()
		builder.setTxt(obj.txt)
		builder.setFont(RFontSerializer.write(obj.font))
		builder
	}

	def read(input: RText_proto): RText =
		RText(input.getTxt, RFontSerializer.read(input.getFont))
}

private[serialization] object ReportTextSerializer {
	def write(obj: ReportText): ReportText_proto.Builder = {
		val builder = ReportText_proto.newBuilder()
		builder.setTxt(RTextSerializer.write(obj.txt))
		builder.setX(obj.x)
		builder.setY(obj.y)
		builder
	}

	def read(input: ReportText_proto): ReportText =
		ReportText(RTextSerializer.read(input.getTxt), input.getX, input.getY)
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
			}
			builder.addItem(builderItem)
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
			}
			reportItem.deltaY = item.getDeltaY
			reportItem
		})
		ReportPage(ListBuffer(result.toList: _*))
	}


	def main(args: Array[String]): Unit = {

	}
}
