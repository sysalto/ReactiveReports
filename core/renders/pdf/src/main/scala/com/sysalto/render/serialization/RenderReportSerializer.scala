package com.sysalto.render.serialization


import com.sysalto.render.serialization.RenderProto.PdfBaseItem_proto.FieldCase
import com.sysalto.render.serialization.RenderProto.PdfPageItem_proto.FieldItemCase

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
			if (input.id==5) {
				println("OK")
			}
			input match {
				case item: renderReportTypes.PdfCatalog =>
					builder.setPdfCatalogProto(PdfCatalogSerializer.write(item))
				case item: renderReportTypes.PdfPage =>
					builder.setPdfPageProto(PdfPageSerializer.write(item))
				case item: renderReportTypes.PdfFont =>
					builder.setPdfFontProto(PdfFontSerializer.write(item))
				case item: renderReportTypes.PdfPageContent =>
					builder.setPdfPageContentProto(PdfPageContentSerializer.write(item))
				case item: renderReportTypes.PdfPageList =>
					builder.setPdfPageListProto(PdfPageListSerializer.write(item))
				case item: renderReportTypes.PdfImage =>
					builder.setPdfImageProto(PdfImageSerializer.write(item))
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
				case FieldCase.PDFPAGECONTENTPROTO => {
					PdfPageContentSerializer.read(input.getId, input.getOffset, input.getPdfPageContentProto)
				}
				case FieldCase.PDFPAGELISTPROTO => {
					PdfPageListSerializer.read(input.getId, input.getOffset, input.getPdfPageListProto)
				}
				case FieldCase.PDFIMAGEPROTO => {
					PdfImageSerializer.read(input.getId, input.getOffset, input.getPdfImageProto)
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
			builder.setLeafNbr(input.leafNbr)
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
			result.leafNbr = input.getLeafNbr
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


	object PdfPageItemSerializer {
		def write(input: renderReportTypes.PdfPageItem): PdfPageItem_proto = {
			val builder = PdfPageItem_proto.newBuilder()
			input match {
				case item: renderReportTypes.PdfText => {
					builder.setPdfTextProto(PdfTextSerializer.write(item))
				}
				case item: renderReportTypes.PdfGraphic => {
					builder.setPdfGraphicProto(PdfGraphicSerializer.write(item))
				}
			}
			builder.build()
		}

		def read(input: PdfPageItem_proto): renderReportTypes.PdfPageItem = {
			input.getFieldItemCase match {
				case FieldItemCase.PDFTEXT_PROTO => {
					PdfTextSerializer.read(input.getPdfTextProto)
				}
				case FieldItemCase.PDFGRAPHIC_PROTO => {
					PdfGraphicSerializer.read(input.getPdfGraphicProto)
				}
			}
		}
	}

	object PdfGraphicSerializer {
		def write(input: renderReportTypes.PdfGraphic): PdfGraphic_proto = {
			val builder = PdfGraphic_proto.newBuilder()
			builder.build()
		}

		def read(input: PdfGraphic_proto): renderReportTypes.PdfGraphic = {
			new renderReportTypes.PdfGraphic(List())
		}
	}

	object ReportTxtSerializer {
		def write(input: ReportTxt): ReportTxt_proto = {
			val builder = ReportTxt_proto.newBuilder()
			builder.setTxt(input.txt)
			builder.build()
		}

		def read(input: ReportTxt_proto): ReportTxt = {
			val result = new ReportTxt(input.getTxt)
			result
		}
	}


	object PdfTxtFragmentSerializer {
		def write(input: renderReportTypes.PdfTxtFragment): PdfTxtFragment_proto = {
			val builder = PdfTxtFragment_proto.newBuilder()
			builder.setX(input.x)
			builder.setY(input.y)
			builder.setRtextProto(ReportTxtSerializer.write(input.rtext))
			builder.setFonttRefName(input.fontRefName)
			builder.build()
		}

		def read(input: PdfTxtFragment_proto): renderReportTypes.PdfTxtFragment = {
			new renderReportTypes.PdfTxtFragment(input.getX, input.getY, ReportTxtSerializer.read(input.getRtextProto), input.getFonttRefName)
		}
	}


	object PdfTextSerializer {
		def write(input: renderReportTypes.PdfText): PdfText_proto = {
			val builder = PdfText_proto.newBuilder()
			input.txtList.foreach(item => builder.addTxtList(PdfTxtFragmentSerializer.write(item)))
			builder.build()
		}

		def read(input: PdfText_proto): renderReportTypes.PdfText = {
			new renderReportTypes.PdfText(input.getTxtListList.asScala.map(item => PdfTxtFragmentSerializer.read(item)).toList)
		}
	}


	object PdfPageContentSerializer {
		def write(input: renderReportTypes.PdfPageContent): PdfPageContent_proto = {
			val builder = PdfPageContent_proto.newBuilder()
			input.pageItemList.foreach(item => builder.addPdfPageItemProto(PdfPageItemSerializer.write(item)))
			builder.setPdfCompression(input.pdfCompression)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfPageContent_proto): renderReportTypes.PdfPageContent = {
			val list = input.getPdfPageItemProtoList.asScala.map(item => PdfPageItemSerializer.read(item)).toList
			val result = new renderReportTypes.PdfPageContent(id, list, input.getPdfCompression)
			result.offset = offset
			result
		}
	}

	object PdfPageListSerializer {
		def write(input: renderReportTypes.PdfPageList): PdfPageList_proto = {
			val builder = PdfPageList_proto.newBuilder()
			input.parentId.foreach(item => builder.addParentId(item))
			input.pageList.foreach(item => builder.addPageList(item))
			builder.setLeafNbr(input.leafNbr)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfPageList_proto): renderReportTypes.PdfPageList = {
			val result = new renderReportTypes.PdfPageList(id)
			result.offset = offset
			if (input.getParentIdCount > 0) {
				result.parentId = Some(input.getParentId(0))
			}
			result.pageList ++= input.getPageListList.asScala.toList.map(id => id.asInstanceOf[Long])
			result.leafNbr = input.getLeafNbr
			result
		}
	}

	object PdfImageSerializer {
		def write(input: renderReportTypes.PdfImage): PdfImage_proto = {
			val builder = PdfImage_proto.newBuilder()
			builder.setFileName(input.fileName)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfImage_proto): renderReportTypes.PdfImage = {
			val result = new renderReportTypes.PdfImage(id, input.getFileName)
			result.offset = offset
			result
		}
	}

}
