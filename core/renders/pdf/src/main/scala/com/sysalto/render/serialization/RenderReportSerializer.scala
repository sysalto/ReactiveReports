package com.sysalto.render.serialization


import com.sysalto.render.PdfDraw.{DrawLine, DrawStroke, PdfGraphicFragment}
import com.sysalto.render.serialization.RenderProto.PdfBaseItem_proto.FieldCase
import com.sysalto.render.serialization.RenderProto.PdfPageItem_proto.FieldItemCase

import scala.collection.JavaConverters._
import com.sysalto.render.serialization.RenderProto._
import com.sysalto.render.util.fonts.parsers.FontParser.{EmbeddedFontDescriptor, FontBBox, FontMetric, GlyphWidth}
import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, RFontFamily, ReportColor, ReportTxt}
import com.sysalto.report.serialization.common.ReportCommonProto.DirectDrawMovePoint_proto

import scala.collection.mutable.ListBuffer

class RenderReportSerializer(val renderReportTypes: RenderReportTypes) {

	object PdfBaseItemSerializer {
		def write(input: renderReportTypes.PdfBaseItem): PdfBaseItem_proto = {
			val builder = PdfBaseItem_proto.newBuilder()
			builder.setId(input.id)
			builder.setOffset(input.offset)
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
				case item: renderReportTypes.PdfShaddingFctColor =>
					builder.setPdfShaddingFctColorProto(PdfShaddingFctColorSerializer.write(item))
				case item: renderReportTypes.PdfColorShadding =>
					builder.setPdfColorShaddingProto(PdfColorShaddingSerializer.write(item))
				case item: renderReportTypes.PdfGPattern =>
					builder.setPdfGPatternProto(PdfGPatternSerializer.write(item))
				case item: renderReportTypes.PdfFontStream =>
					builder.setPdfFontStreamProto(PdfFontStreamSerializer.write(item))
				case item: renderReportTypes.PdfFontDescriptor =>
					builder.setPdfFontDescriptorProto(PdfFontDescriptorSerializer.write(item))


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
				case FieldCase.PDFSHADDINGFCTCOLOR_PROTO => {
					PdfShaddingFctColorSerializer.read(input.getId, input.getOffset, input.getPdfShaddingFctColorProto)
				}
				case FieldCase.PDFCOLORSHADDING_PROTO => {
					PdfColorShaddingSerializer.read(input.getId, input.getOffset, input.getPdfColorShaddingProto)
				}
				case FieldCase.PDFGPATTERN_PROTO => {
					PdfGPatternSerializer.read(input.getId, input.getOffset, input.getPdfGPatternProto)
				}
				case FieldCase.PDFFONTSTREAM_PROTO => {
					PdfFontStreamSerializer.read(input.getId, input.getOffset, input.getPdfFontStreamProto)
				}
				case FieldCase.PDFFONTDESCRIPTOR_PROTO => {
					PdfFontDescriptorSerializer.read(input.getId, input.getOffset, input.getPdfFontDescriptorProto)
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

	object FontEmbeddedDefSerializer {
		def write(input: renderReportTypes.FontEmbeddedDef): FontEmbeddedDef_proto = {
			val builder = FontEmbeddedDef_proto.newBuilder()
			builder.setIdPdfFontDescriptor(input.idPdfFontDescriptor)
			builder.setIdPdfFontStream(input.idPdfFontStream)
			builder.build()
		}

		def read(input: FontEmbeddedDef_proto): renderReportTypes.FontEmbeddedDef = {
			new renderReportTypes.FontEmbeddedDef(input.getIdPdfFontDescriptor, input.getIdPdfFontStream)
		}
	}

	object PdfFontSerializer {
		def write(input: renderReportTypes.PdfFont): PdfFont_proto = {
			val builder = PdfFont_proto.newBuilder()
			builder.setRefName(input.refName)
			builder.setFontKeyName(input.fontKeyName)
			if (input.embeddedDefOpt.isDefined) {
				builder.addFontEmbeddedDef(FontEmbeddedDefSerializer.write(input.embeddedDefOpt.get))
			}
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfFont_proto): renderReportTypes.PdfFont = {
			val embeddedDefOpt = if (input.getFontEmbeddedDefCount == 0) None else Some(FontEmbeddedDefSerializer.read(input.getFontEmbeddedDef(0)))
			val result = new renderReportTypes.PdfFont(id, input.getRefName, input.getFontKeyName, embeddedDefOpt)
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
			input.items.foreach(item =>
				builder.addPdfGraphicFragmentProto(PdfGraphicFragmentSerializer.write(item)))
			builder.build()
		}

		def read(input: PdfGraphic_proto): renderReportTypes.PdfGraphic = {
			val list = input.getPdfGraphicFragmentProtoList.asScala.map(item => PdfGraphicFragmentSerializer.read(item)).toList
			new renderReportTypes.PdfGraphic(list)
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

	object PdfShaddingFctColorSerializer {
		def write(input: renderReportTypes.PdfShaddingFctColor): PdfShaddingFctColor_proto = {
			val builder = PdfShaddingFctColor_proto.newBuilder()
			builder.setColor1(ReportColorSerializer.write(input.color1))
			builder.setColor2(ReportColorSerializer.write(input.color2))
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfShaddingFctColor_proto): renderReportTypes.PdfShaddingFctColor = {
			val result = new renderReportTypes.PdfShaddingFctColor(id, ReportColorSerializer.read(input.getColor1),
				ReportColorSerializer.read(input.getColor2))
			result.offset = offset
			result
		}
	}

	object PdfColorShaddingSerializer {
		def write(input: renderReportTypes.PdfColorShadding): PdfColorShadding_proto = {
			val builder = PdfColorShadding_proto.newBuilder()
			builder.setX0(input.x0)
			builder.setY0(input.y0)
			builder.setX1(input.x1)
			builder.setY1(input.y1)
			builder.setIdPdfShaddingFctColor(input.idPdfShaddingFctColor)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfColorShadding_proto): renderReportTypes.PdfColorShadding = {
			val result = new renderReportTypes.PdfColorShadding(id, input.getX0, input.getY0, input.getX1,
				input.getY1, input.getIdPdfShaddingFctColor)
			result.offset = offset
			result
		}
	}

	object PdfGPatternSerializer {
		def write(input: renderReportTypes.PdfGPattern): PdfGPattern_proto = {
			val builder = PdfGPattern_proto.newBuilder()
			builder.setIdPdfShadding(input.idPdfShadding)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfGPattern_proto): renderReportTypes.PdfGPattern = {
			val result = new renderReportTypes.PdfGPattern(id, input.getIdPdfShadding)
			result.offset = offset
			result
		}
	}


	object DrawLineSerializer {
		def write(input: DrawLine): DrawLine_proto = {
			val builder = DrawLine_proto.newBuilder()
			builder.setX1(input.x1)
			builder.setY1(input.y1)
			builder.setX2(input.x2)
			builder.setY2(input.y2)
			builder.setVlineWidth(input.vlineWidth)
			builder.setColor(ReportColorSerializer.write(input.color))
			builder.build()
		}

		def read(input: DrawLine_proto): DrawLine = {
			new DrawLine(input.getX1, input.getY1, input.getX2, input.getY2, input.getVlineWidth, ReportColorSerializer.read(input.getColor), None)
		}
	}

	object PdfGraphicFragmentSerializer {
		def write(input: PdfGraphicFragment): PdfGraphicFragment_proto = {
			val builder = PdfGraphicFragment_proto.newBuilder()
			builder.setContent(input.content)
			input match {
				case item: DrawLine =>
					builder.setDrawLineProto(DrawLineSerializer.write(item))
				case item: renderReportTypes.PdfRectangle1 =>
					builder.setPdfRectangleProto(PdfRectangleSerializer.write(item))
				case item: DrawStroke =>
					builder.setDrawStrokeProto(DrawStrokeSerializer.write(item))
				case item: renderReportTypes.PdfDrawImage =>
					builder.setPdfDrawImageProto(PdfDrawImageSerializer.write(item))
				case item: renderReportTypes.DrawPieChart1 =>
					builder.setDrawPieChartProto(DrawPieChartSerializer.write(item))
				case item: renderReportTypes.DirectDrawMovePoint =>
					builder.setDirectDrawMovePointProto(DirectDrawMovePointSerializer.write(item))

			}
			builder.build()
		}

		def read(input: PdfGraphicFragment_proto): PdfGraphicFragment = {
			input.getFieldCase match {
				case PdfGraphicFragment_proto.FieldCase.DRAWLINE_PROTO => {
					DrawLineSerializer.read(input.getDrawLineProto)
				}
				case PdfGraphicFragment_proto.FieldCase.PDFRECTANGLE_PROTO => {
					PdfRectangleSerializer.read(input.getPdfRectangleProto)
				}
				case PdfGraphicFragment_proto.FieldCase.DRAWSTROKE_PROTO => {
					DrawStrokeSerializer.read(input.getDrawStrokeProto)
				}
				case PdfGraphicFragment_proto.FieldCase.PDFDRAWIMAGE_PROTO => {
					PdfDrawImageSerializer.read(input.getPdfDrawImageProto)
				}
				case PdfGraphicFragment_proto.FieldCase.DRAWPIECHART_PROTO => {
					DrawPieChartSerializer.read(input.getContent, input.getDrawPieChartProto)
				}
				case PdfGraphicFragment_proto.FieldCase.DIRECTDRAWMOVEPOINT_PROTO=> {
					DirectDrawMovePointSerializer.read(input.getDirectDrawMovePointProto)
				}
			}
		}
	}

	object PdfRectangleSerializer {
		def write(input: renderReportTypes.PdfRectangle1): PdfRectangle_proto = {
			val builder = PdfRectangle_proto.newBuilder()
			builder.setX1(input.x1)
			builder.setY1(input.y1)
			builder.setX2(input.x2)
			builder.setY2(input.y2)
			builder.setRadius(input.radius)
			if (input.borderColor.isDefined) {
				builder.addBorderColor(ReportColorSerializer.write(input.borderColor.get))
			}
			if (input.fillColor.isDefined) {
				builder.addFillColor(ReportColorSerializer.write(input.fillColor.get))
			}
			if (input.idPatternColor.isDefined) {
				builder.addIdPatternColor(input.idPatternColor.get)
			}
			builder.build()
		}

		def read(input: PdfRectangle_proto): renderReportTypes.PdfRectangle1 = {
			val borderColor = if (input.getBorderColorCount == 0) None else Some(ReportColorSerializer.read(input.getBorderColor(0)))
			val fillColor = if (input.getFillColorCount == 0) None else Some(ReportColorSerializer.read(input.getFillColor(0)))
			val idPatternColor = if (input.getIdPatternColorCount == 0) None else Some(input.getIdPatternColor(0))
			new renderReportTypes.PdfRectangle1(input.getX1, input.getY1, input.getX2, input.getY2,
				input.getRadius, borderColor, fillColor, idPatternColor)
		}
	}


	object DrawStrokeSerializer {
		def write(input: DrawStroke): DrawStroke_proto = {
			val builder = DrawStroke_proto.newBuilder()
			builder.build()
		}

		def read(input: DrawStroke_proto): DrawStroke = {
			new DrawStroke()
		}
	}


	object PdfDrawImageSerializer {
		def write(input: renderReportTypes.PdfDrawImage): PdfDrawImage_proto = {
			val builder = PdfDrawImage_proto.newBuilder()
			builder.setIdPdfImage(input.idPdfImage)
			builder.setX(input.x)
			builder.setY(input.y)
			builder.setScale(input.scale)
			builder.build()
		}

		def read(input: PdfDrawImage_proto): renderReportTypes.PdfDrawImage = {
			new renderReportTypes.PdfDrawImage(input.getIdPdfImage, input.getX, input.getY, input.getScale, None)
		}
	}


	object DrawPieChartSerializer {
		def write(input: renderReportTypes.DrawPieChart1): DrawPieChart_proto = {
			val builder = DrawPieChart_proto.newBuilder()
			builder.setTitle(input.title)
			input.data.foreach(item =>
				builder.addData(StringDoubleSerializer.write(item)))
			builder.setX(input.x)
			builder.setY(input.y)
			builder.setWidth(input.width)
			builder.setHeight(input.height)
			builder.setFont(RFontSerializer.write(input.font))
			builder.build()
		}

		def read(content: String, input: DrawPieChart_proto): renderReportTypes.DrawPieChart1 = {
			val dataList = input.getDataList.asScala.map(item => StringDoubleSerializer.read(item)).toList
			val result = new renderReportTypes.DrawPieChart1(RFontSerializer.read(input.getFont), input.getTitle, dataList, input.getX, input.getY, input.getWidth, input.getHeight)
			result.contentStr = content
			result
		}
	}


	private[serialization] object DirectDrawMovePointSerializer {
		def write(obj: renderReportTypes.DirectDrawMovePoint): DirectDrawMovePoint_proto = {
			val builder = DirectDrawMovePoint_proto.newBuilder()
			builder.setX(obj.x)
			builder.setY(obj.y)
			builder.build()
		}

		def read( input: DirectDrawMovePoint_proto): renderReportTypes.DirectDrawMovePoint = {
			val result = new renderReportTypes.DirectDrawMovePoint(input.getX, input.getY)
			result
		}
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

	object RFontSerializer {
		def write(obj: RFont): RFont_proto = {
			val builder = RFont_proto.newBuilder()
			builder.setSize(obj.size)
			builder.setFontName(obj.fontName)
			builder.setAttribute(RFontAttributeSerializer.write(obj.attribute))
			builder.setColor(RColorSerializer.write(obj.color))
			builder.build()
		}

		def read(input: RFont_proto): RFont =
			RFont(input.getSize, input.getFontName, RFontAttributeSerializer.read(input.getAttribute), RColorSerializer.read(input.getColor))
	}

	object StringDoubleSerializer {
		def write(input: (String, Double)): StringDouble_proto = {
			val builder = StringDouble_proto.newBuilder()
			builder.setValue1(input._1)
			builder.setValue2(input._2)
			builder.build()
		}

		def read(input: StringDouble_proto): (String, Double) = {
			(input.getValue1, input.getValue2)
		}
	}


	object PdfFontStreamSerializer {
		def write(input: renderReportTypes.PdfFontStream): PdfFontStream_proto = {
			val builder = PdfFontStream_proto.newBuilder()
			builder.setFontName(input.fontName)
			builder.setFontMetric(FontMetricSerializer.write(input.fontMetric))
			builder.setPdfCompression(input.pdfCompression)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfFontStream_proto): renderReportTypes.PdfFontStream = {
			val result = new renderReportTypes.PdfFontStream(id, input.getFontName, FontMetricSerializer.read(input.getFontMetric), input.getPdfCompression)
			result.offset = offset
			result
		}
	}

	object FontMetricSerializer {
		def write(input: FontMetric): FontMetric_proto = {
			val builder = FontMetric_proto.newBuilder()
			builder.setFontName(input.fontName)
			input.fontMap.keys.foreach(key => {
				val value = input.fontMap(key)
				builder.putFontMap(key, value)
			})
			builder.setFontHeight(FloatFloatSerializer.write(input.fontHeight))
			if (input.fontDescriptor.isDefined) {
				builder.addFontDescriptor(EmbeddedFontDescriptorSerializer.write(input.fontDescriptor.get))
			}
			builder.build()
		}

		def read(input: FontMetric_proto): FontMetric = {
			val fontMap = input.getFontMapMap.asScala.map(item => (item._1.toInt, item._2.toFloat)).toMap
			val fontDescription = if (input.getFontDescriptorCount == 0) None else Some(EmbeddedFontDescriptorSerializer.read(input.getFontDescriptor(0)))
			new FontMetric(input.getFontName, fontMap, FloatFloatSerializer.read(input.getFontHeight), fontDescription)
		}
	}


	object FloatFloatSerializer {
		def write(input: (Float, Float)): FloatFloat_proto = {
			val builder = FloatFloat_proto.newBuilder()
			builder.setValue1(input._1)
			builder.setValue2(input._2)
			builder.build()
		}

		def read(input: FloatFloat_proto): (Float, Float) = (input.getValue1, input.getValue2)
	}


	object EmbeddedFontDescriptorSerializer {
		def write(input: EmbeddedFontDescriptor): EmbeddedFontDescriptor_proto = {
			val builder = EmbeddedFontDescriptor_proto.newBuilder()
			builder.setGlyphWidth(GlyphWidthSerializer.write(input.glyphWidth))
			builder.setCapHeight(input.capHeight)
			builder.setFontBBox(FontBBoxSerializer.write(input.fontBBox))
			builder.setAscent(input.ascent)
			builder.setFlags(input.flags)
			builder.setItalicAngle(input.italicAngle)
			builder.setDescent(input.descent)
			builder.build()
		}

		def read(input: EmbeddedFontDescriptor_proto): EmbeddedFontDescriptor = {
			new EmbeddedFontDescriptor(input.getAscent.toShort, input.getCapHeight.toShort, input.getDescent.toShort,
				FontBBoxSerializer.read(input.getFontBBox), input.getItalicAngle.toShort, input.getFlags,
				GlyphWidthSerializer.read(input.getGlyphWidth))
		}
	}


	object GlyphWidthSerializer {
		def write(input: GlyphWidth): GlyphWidth_proto = {
			val builder = GlyphWidth_proto.newBuilder()
			builder.setFirstChar(input.firstChar)
			builder.setLastChar(input.lastChar)
			input.widthList.foreach(item => builder.addWidthList(item))
			builder.build()
		}

		def read(input: GlyphWidth_proto): GlyphWidth = {
			val widthList = input.getWidthListList.asScala.map(item => item.toShort).toList
			new GlyphWidth(input.getFirstChar.toShort, input.getLastChar.toShort, widthList)
		}
	}


	object FontBBoxSerializer {
		def write(input: FontBBox): FontBBox_proto = {
			val builder = FontBBox_proto.newBuilder()
			builder.setLowerLeftX(input.lowerLeftX)
			builder.setLowerLeftY(input.lowerLeftY)
			builder.setUpperRightX(input.upperRightX)
			builder.setUpperRightY(input.upperRightY)
			builder.build()
		}

		def read(input: FontBBox_proto): FontBBox = {
			new FontBBox(input.getLowerLeftX.toShort, input.getLowerLeftY.toShort, input.getUpperRightX.toShort, input.getUpperRightY.toShort)
		}
	}


	object PdfFontDescriptorSerializer {
		def write(input: renderReportTypes.PdfFontDescriptor): PdfFontDescriptor_proto = {
			val builder = PdfFontDescriptor_proto.newBuilder()
			builder.setIdPdfFontStream(input.idPdfFontStream)
			builder.setFontKeyName(input.fontKeyName)
			builder.build()
		}

		def read(id: Long, offset: Long, input: PdfFontDescriptor_proto): renderReportTypes.PdfFontDescriptor = {
			val result = new renderReportTypes.PdfFontDescriptor(id, input.getIdPdfFontStream, input.getFontKeyName)
			result.offset = offset
			result
		}
	}

}
