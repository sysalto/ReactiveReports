package com.sysalto.render.serialization


import com.sysalto.render.serialization.RenderProto.PdfBaseItem_proto.FieldCase

import scala.collection.JavaConverters._
import com.sysalto.render.serialization.RenderProto._
import com.sysalto.render.util.fonts.parsers.FontParser.{EmbeddedFontDescriptor, FontBBox, FontMetric, GlyphWidth}
import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, RFontFamily, ReportColor, ReportTxt}

import scala.collection.mutable.ListBuffer

class RenderReportSerializer(val renderReportTypes: RenderReportTypes) {

	object PdfBaseItemSerializer {
		def write(input: renderReportTypes.PdfBaseItem): PdfBaseItem_proto = {
			val builder = PdfBaseItem_proto.newBuilder()
			builder.setId(input.id)
			builder.setOffset(input.offset)
			input match {
				case catalog: renderReportTypes.PdfCatalog =>
					builder.setPdfCatalogProto(PdfCatalogSerializer.write(catalog))
				case pdfPage: renderReportTypes.PdfPage =>
					builder.setPdfPageProto(PdfPageSerializer.write(pdfPage))
				case pdfPage: renderReportTypes.PdfFont =>
					builder.setPdfFontProto(PdfFontSerializer.write(pdfPage))
			}
			builder.build()
		}

		def read(input: PdfBaseItem_proto): renderReportTypes.PdfBaseItem = {
			input.getFieldCase match {
				case FieldCase.PDFCATALOGPROTO => {
					PdfCatalogSerializer.read(input.getId, input.getOffset, input.getPdfCatalogProto)
				}
				case FieldCase.PDFPAGEPROTO => {
					PdfPageSerializer.read(input.getId, input.getOffset, input.getPdfPageProto)
				}
				case FieldCase.PDFFONTPROTO => {
					PdfFontSerializer.read(input.getId, input.getOffset, input.getPdfFontProto)
				}
			}
		}
	}

	object PdfCatalogSerializer {
		def write(input: renderReportTypes.PdfCatalog): PdfCatalog_proto = {
			val builder = PdfCatalog_proto.newBuilder()
			if (input.idPdfPageListOpt.isDefined) {
				builder.addIdPdfPageListOpt(input.idPdfPageListOpt.get)
			}
			if (input.idPdfNamesOpt.isDefined) {
				builder.addIdPdfNamesOpt(input.idPdfNamesOpt.get)
			}
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfCatalog_proto): renderReportTypes.PdfCatalog = {
			val result = new renderReportTypes.PdfCatalog(id)
			result.offset = offset
			if (input.getIdPdfPageListOptCount() > 0) {
				result.idPdfPageListOpt = Some(input.getIdPdfPageListOptList.asScala.head)
			}
			if (input.getIdPdfNamesOptCount() > 0) {
				result.idPdfNamesOpt = Some(input.getIdPdfNamesOptList.asScala.head)
			}
			result
		}
	}

	object PdfPageSerializer {
		def write(input: renderReportTypes.PdfPage): PdfPage_proto = {
			val builder = PdfPage_proto.newBuilder()
			builder.setParentId(input.parentId)
			builder.setPageWidth(input.pageWidth)
			builder.setPageHeight(input.pageHeight)
			input.idFontList.foreach(id => builder.addIdFontList(id))
			input.idPdfPatternList.foreach(id => builder.addIdPdfPatternList(id))
			input.idAnnotationList.foreach(id => builder.addIdAnnotationList(id))
			input.idImageList.foreach(id => builder.addIdImageList(id))
			if (input.idContentPageOpt.isDefined) {
				builder.addIdContentPageOpt(input.idContentPageOpt.get)
			}
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfPage_proto): renderReportTypes.PdfPage = {
			val result = new renderReportTypes.PdfPage(id, input.getParentId, input.getPageWidth, input.getPageHeight)
			result.offset = offset
			result.idFontList = input.getIdFontListList.asScala.map(id => id.asInstanceOf[Long]).toList
			result.idPdfPatternList = input.getIdPdfPatternListList.asScala.map(id => id.asInstanceOf[Long]).toList
			result.idAnnotationList = input.getIdAnnotationListList.asScala.map(id => id.asInstanceOf[Long]).toList
			result.idImageList ++= input.getIdImageListList.asScala.map(id => id.asInstanceOf[Long])
			if (input.getIdContentPageOptCount > 0) {
				result.idContentPageOpt = Some(input.getIdContentPageOpt(0))
			}
			result
		}
	}

	object PdfFontSerializer {
		def write(input: renderReportTypes.PdfFont): PdfFont_proto = {
			val builder = PdfFont_proto.newBuilder()
			builder.setRefName(input.refName)
			builder.setFontKeyName(input.fontKeyName)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfFont_proto): renderReportTypes.PdfFont = {
			val result = new renderReportTypes.PdfFont(id, input.getRefName, input.getFontKeyName)
			result.offset = offset
			result
		}
	}

}
