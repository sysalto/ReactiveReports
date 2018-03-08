package com.sysalto.render.serialization

import scala.collection.JavaConverters._
import RenderReportTypes._
import com.sysalto.render.serialization.RenderProto._
import com.sysalto.render.util.fonts.parsers.FontParser.{EmbeddedFontDescriptor, FontBBox, FontMetric, GlyphWidth}
import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, RFontFamily, ReportColor, ReportTxt}

import scala.collection.mutable.ListBuffer

object RenderReportSerializer {
	private[this] implicit val allItems = ListBuffer[PdfBaseItem]()

//
//	object StringString_protoSerializer {
//		def write(obj: (String, String)): StringString_proto = {
//			val builder = StringString_proto.newBuilder()
//			builder.setValue1(obj._1)
//			builder.setValue2(obj._2)
//			builder.build()
//		}
//
//		def read(obj: StringString_proto): (String, String) = {
//			(obj.getValue1, obj.getValue2)
//		}
//	}
//
//	object PdfDests_protoSerializer {
//		def write(obj: PdfDests): PdfDests_proto = {
//			val builder = PdfDests_proto.newBuilder()
//			builder.setId(obj.id)
//			obj.dests.foreach(item => {
//				builder.addDestsItem(StringString_protoSerializer.write(item))
//			})
//			builder.build()
//		}
//
//		def read(obj: PdfDests_proto): PdfDests = {
//			new PdfDests(obj.getId, obj.getDestsItemList.asScala.map(item => {
//				StringString_protoSerializer.read(item)
//			}).to[ListBuffer]
//			)
//		}
//	}
//
//	object PdfNames_protoSerializer {
//		def write(obj: PdfNames): PdfNames_proto = {
//			val builder = PdfNames_proto.newBuilder()
//			builder.setId(obj.id)
////			builder.setDests(PdfDests_protoSerializer.write(obj.dests))
//			builder.build()
//		}
//
//		def read(obj: PdfNames_proto): PdfNames = {
////			new PdfNames(obj.getId, PdfDests_protoSerializer.read(obj.getDests))
//			null
//		}
//	}
//
//	object OptionLong_protoSerializer {
//		def write(obj: Option[Long]): OptionLong_proto = {
//			val builder = OptionLong_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(obj.get)
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionLong_proto): Option[Long] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(obj.getValue)
//			}
//		}
//	}
//
//
//	object PdfPageList_protoSerializer {
//		def write(obj: PdfPageList): PdfPageList_proto = {
//			val builder = PdfPageList_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setParentId(OptionLong_protoSerializer.write(obj.parentId))
//			obj.pageList.foreach(item => {
//				builder.addPageListItem(item)
//			})
//			builder.build()
//		}
//
//		def read(obj: PdfPageList_proto): PdfPageList = {
//			new PdfPageList(obj.getId, OptionLong_protoSerializer.read(obj.getParentId), obj.getPageListItemList.asScala.
//				map(item => item.asInstanceOf[Long]).to[ListBuffer]
//			)
//		}
//	}
//
//	object OptionPdfNames_protoSerializer {
//		def write(obj: Option[PdfNames]): OptionPdfNames_proto = {
//			val builder = OptionPdfNames_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(PdfNames_protoSerializer.write(obj.get))
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionPdfNames_proto): Option[PdfNames] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(PdfNames_protoSerializer.read(obj.getValue))
//			}
//		}
//	}
//
//
//	object OptionPdfPageList_protoSerializer {
//		def write(obj: Option[PdfPageList]): OptionPdfPageList_proto = {
//			val builder = OptionPdfPageList_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(PdfPageList_protoSerializer.write(obj.get))
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionPdfPageList_proto): Option[PdfPageList] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(PdfPageList_protoSerializer.read(obj.getValue))
//			}
//		}
//	}
//
//
//	object PdfCatalog_protoSerializer {
//		def write(obj: PdfCatalog): PdfCatalog_proto = {
//			val builder = PdfCatalog_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setPdfPageList(OptionPdfPageList_protoSerializer.write(obj.pdfPageList))
//			builder.setPdfNames(OptionPdfNames_protoSerializer.write(obj.pdfNames))
//			builder.build()
//		}
//
//		def read(obj: PdfCatalog_proto): PdfCatalog = {
//			new PdfCatalog(obj.getId, OptionPdfPageList_protoSerializer.read(obj.getPdfPageList), OptionPdfNames_protoSerializer.read(obj.getPdfNames))
//		}
//	}
//
//	object ReportColor_protoSerializer {
//		def write(obj: ReportColor): ReportColor_proto = {
//			val builder = ReportColor_proto.newBuilder()
//			builder.setR(obj.r)
//			builder.setG(obj.g)
//			builder.setB(obj.b)
//			builder.setOpacity(obj.opacity)
//			builder.build()
//		}
//
//		def read(obj: ReportColor_proto): ReportColor = {
//			ReportColor(obj.getR, obj.getG, obj.getB, obj.getOpacity)
//		}
//	}
//
//
//	object PdfShaddingFctColor_protoSerializer {
//		def write(obj: PdfShaddingFctColor): PdfShaddingFctColor_proto = {
//			val builder = PdfShaddingFctColor_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setColor1(ReportColor_protoSerializer.write(obj.color1))
//			builder.setColor2(ReportColor_protoSerializer.write(obj.color2))
//			builder.build()
//		}
//
//		def read(obj: PdfShaddingFctColor_proto): PdfShaddingFctColor = {
//			new PdfShaddingFctColor(obj.getId, ReportColor_protoSerializer.read(obj.getColor1), ReportColor_protoSerializer.read(obj.getColor2))
//		}
//	}
//
//
//	object PdfColorShadding_protoSerializer {
//		def write(obj: PdfColorShadding): PdfColorShadding_proto = {
//			val builder = PdfColorShadding_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setX0(obj.x0)
//			builder.setY0(obj.y0)
//			builder.setX1(obj.x1)
//			builder.setY1(obj.y1)
//			builder.setPdfShaddingFctColor(PdfShaddingFctColor_protoSerializer.write(obj.pdfShaddingFctColor))
//			builder.build()
//		}
//
//		def read(obj: PdfColorShadding_proto): PdfColorShadding = {
//			new PdfColorShadding(obj.getId, obj.getX0, obj.getY0, obj.getX1, obj.getY1, PdfShaddingFctColor_protoSerializer.read(obj.getPdfShaddingFctColor))
//		}
//	}
//
//
//	object PdfGPattern_protoSerializer {
//		def write(obj: PdfGPattern): PdfGPattern_proto = {
//			val builder = PdfGPattern_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setPdfShadding(PdfColorShadding_protoSerializer.write(obj.pdfShadding))
//			builder.build()
//		}
//
//		def read(obj: PdfGPattern_proto): PdfGPattern = {
//			new PdfGPattern(obj.getId, PdfColorShadding_protoSerializer.read(obj.getPdfShadding))
//		}
//	}
//
//
//	object PdfImage_protoSerializer {
//		def write(obj: PdfImage): PdfImage_proto = {
//			val builder = PdfImage_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setFileName(obj.fileName)
//			builder.build()
//		}
//
//		def read(obj: PdfImage_proto): PdfImage = {
//			new PdfImage(obj.getId, obj.getFileName)
//		}
//	}
//
//
//	object PdfFont_protoSerializer {
//		def write(obj: PdfFont): PdfFont_proto = {
//			val builder = PdfFont_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setRefName(obj.refName)
//			builder.setFontKeyName(obj.fontKeyName)
//			builder.setEmbeddedDefOpt(OptionFontEmbeddedDef_protoSerializer.write(obj.embeddedDefOpt))
//			builder.build()
//		}
//
//		def read(obj: PdfFont_proto): PdfFont = {
//			new PdfFont(obj.getId, obj.getRefName, obj.getFontKeyName, OptionFontEmbeddedDef_protoSerializer.read(obj.getEmbeddedDefOpt))
//		}
//	}
//
//
//	object PdfPage_protoSerializer {
//		def write(obj: PdfPage): PdfPage_proto = {
//			val builder = PdfPage_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setParentId(obj.parentId)
//			builder.setPageWidth(obj.pageWidth)
//			builder.setPageHeight(obj.pageHeight)
//			obj.fontList.foreach(item => {
//				builder.addFontListItem(PdfFont_protoSerializer.write(item))
//			})
//			obj.pdfPatternList.foreach(item => {
//				builder.addPdfPatternListItem(PdfGPattern_protoSerializer.write(item))
//			})
//			obj.annotation.foreach(item => {
//				builder.addAnnotationItem(PdfAnnotation_protoSerializer.write(item))
//			})
//			obj.imageList.foreach(item => {
//				builder.addImageListItem(PdfImage_protoSerializer.write(item))
//			})
//			builder.setContentPage(OptionPdfPageContent_protoSerializer.write(obj.contentPage))
//			builder.build()
//		}
//
//		def read(obj: PdfPage_proto): PdfPage = {
//			new PdfPage(obj.getId, obj.getParentId, obj.getPageWidth, obj.getPageHeight, obj.getFontListItemList.asScala.map(item => {
//				PdfFont_protoSerializer.read(item)
//			}).to[List]
//				, obj.getPdfPatternListItemList.asScala.map(item => {
//					PdfGPattern_protoSerializer.read(item)
//				}).to[List]
//				, obj.getAnnotationItemList.asScala.map(item => {
//					PdfAnnotation_protoSerializer.read(item)
//				}).to[List]
//				, obj.getImageListItemList.asScala.map(item => {
//					PdfImage_protoSerializer.read(item)
//				}).to[ListBuffer]
//				, OptionPdfPageContent_protoSerializer.read(obj.getContentPage))
//		}
//	}
//
//	object PdfAnnotation_protoSerializer {
//		def write(obj: PdfAnnotation): PdfAnnotation_proto = {
//			val builder = PdfAnnotation_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.build()
//		}
//
//		def read(obj: PdfAnnotation_proto): PdfAnnotation = {
//			//new PdfAnnotation(obj.getId)
//			null
//		}
//	}
//
//
//	object OptionPdfPageContent_protoSerializer {
//		def write(obj: Option[PdfPageContent]): OptionPdfPageContent_proto = {
//			val builder = OptionPdfPageContent_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(PdfPageContent_protoSerializer.write(obj.get))
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionPdfPageContent_proto): Option[PdfPageContent] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(PdfPageContent_protoSerializer.read(obj.getValue))
//			}
//		}
//	}
//
//	object PdfPageContent_protoSerializer {
//		def write(obj: PdfPageContent): PdfPageContent_proto = {
//			val builder = PdfPageContent_proto.newBuilder()
//			builder.setId(obj.id)
//			obj.pageItemList.foreach(item => {
//				builder.addPageItemListItem(PdfPageItem_protoSerializer.write(item))
//			})
//			builder.setPdfCompression(obj.pdfCompression)
//			builder.build()
//		}
//
//		def read(obj: PdfPageContent_proto): PdfPageContent = {
//			new PdfPageContent(obj.getId, obj.getPageItemListItemList.asScala.map(item => {
//				PdfPageItem_protoSerializer.read(item)
//			}).to[List]
//				, obj.getPdfCompression)
//		}
//	}
//
//
//	object PdfPageItem_protoSerializer {
//		def write(obj: PdfPageItem): PdfPageItem_proto = {
//			val builder = PdfPageItem_proto.newBuilder()
//
//			builder.build()
//		}
//
//		def read(obj: PdfPageItem_proto): PdfPageItem = {
//			//new PdfPageItem()
//			null
//		}
//	}
//
//
//	object FontEmbeddedDef_protoSerializer {
//		def write(obj: FontEmbeddedDef): FontEmbeddedDef_proto = {
//			val builder = FontEmbeddedDef_proto.newBuilder()
//			builder.setPdfFontDescriptor(PdfFontDescriptor_protoSerializer.write(obj.pdfFontDescriptor))
//			builder.setPdfFontStream(PdfFontStream_protoSerializer.write(obj.pdfFontStream))
//			builder.build()
//		}
//
//		def read(obj: FontEmbeddedDef_proto): FontEmbeddedDef = {
//			new FontEmbeddedDef(PdfFontDescriptor_protoSerializer.read(obj.getPdfFontDescriptor), PdfFontStream_protoSerializer.read(obj.getPdfFontStream))
//		}
//	}
//
//
//	object PdfFontStream_protoSerializer {
//		def write(obj: PdfFontStream): PdfFontStream_proto = {
//			val builder = PdfFontStream_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setFontName(obj.fontName)
//			builder.setFontMetric(FontMetric_protoSerializer.write(obj.fontMetric))
//			builder.setPdfCompression(obj.pdfCompression)
//			builder.build()
//		}
//
//		def read(obj: PdfFontStream_proto): PdfFontStream = {
//			new PdfFontStream(obj.getId, obj.getFontName, FontMetric_protoSerializer.read(obj.getFontMetric), obj.getPdfCompression)
//		}
//	}
//
//
//	object PdfFontDescriptor_protoSerializer {
//		def write(obj: PdfFontDescriptor): PdfFontDescriptor_proto = {
//			val builder = PdfFontDescriptor_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.setPdfFontStream(PdfFontStream_protoSerializer.write(obj.pdfFontStream))
//			builder.setFontKeyName(obj.fontKeyName)
//			builder.build()
//		}
//
//		def read(obj: PdfFontDescriptor_proto): PdfFontDescriptor = {
//			new PdfFontDescriptor(obj.getId, PdfFontStream_protoSerializer.read(obj.getPdfFontStream), obj.getFontKeyName)
//		}
//	}
//
//	object FontMetric_protoSerializer {
//		def write(obj: FontMetric): FontMetric_proto = {
//			val builder = FontMetric_proto.newBuilder()
//			builder.setFontName(obj.fontName)
//			obj.fontMap.foreach { case (key, value) => builder.putFontMap(key, value) }
//			builder.setFontHeight(FloatFloat_protoSerializer.write(obj.fontHeight))
//			builder.setFontDescriptor(OptionEmbeddedFontDescriptor_protoSerializer.write(obj.fontDescriptor))
//			builder.build()
//		}
//
//		def read(obj: FontMetric_proto): FontMetric = {
//			new FontMetric(obj.getFontName, obj.getFontMapMap.asScala.map { case (key, value) => key.asInstanceOf[Int] -> value.toFloat }.toMap,
//				FloatFloat_protoSerializer.read(obj.getFontHeight),
//				OptionEmbeddedFontDescriptor_protoSerializer.read(obj.getFontDescriptor))
//		}
//	}
//
//	object FloatFloat_protoSerializer {
//		def write(obj: (Float, Float)): FloatFloat_proto = {
//			val builder = FloatFloat_proto.newBuilder()
//			builder.setValue1(obj._1)
//			builder.setValue2(obj._2)
//			builder.build()
//		}
//
//		def read(obj: FloatFloat_proto): (Float, Float) = {
//			(obj.getValue1, obj.getValue2)
//		}
//	}
//
//	object OptionEmbeddedFontDescriptor_protoSerializer {
//		def write(obj: Option[EmbeddedFontDescriptor]): OptionEmbeddedFontDescriptor_proto = {
//			val builder = OptionEmbeddedFontDescriptor_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(EmbeddedFontDescriptor_protoSerializer.write(obj.get))
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionEmbeddedFontDescriptor_proto): Option[EmbeddedFontDescriptor] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(EmbeddedFontDescriptor_protoSerializer.read(obj.getValue))
//			}
//		}
//	}
//
//	object EmbeddedFontDescriptor_protoSerializer {
//		def write(obj: EmbeddedFontDescriptor): EmbeddedFontDescriptor_proto = {
//			val builder = EmbeddedFontDescriptor_proto.newBuilder()
//			builder.setAscent(obj.ascent)
//			builder.setCapHeight(obj.capHeight)
//			builder.setDescent(obj.descent)
//			builder.setFontBBox(FontBBox_protoSerializer.write(obj.fontBBox))
//			builder.setItalicAngle(obj.italicAngle)
//			builder.setFlags(obj.flags)
//			builder.setGlyphWidth(GlyphWidth_protoSerializer.write(obj.glyphWidth))
//			builder.build()
//		}
//
//		def read(obj: EmbeddedFontDescriptor_proto): EmbeddedFontDescriptor = {
//			new EmbeddedFontDescriptor(obj.getAscent.asInstanceOf[Short], obj.getCapHeight.asInstanceOf[Short], obj.getDescent.asInstanceOf[Short],
//				FontBBox_protoSerializer.read(obj.getFontBBox), obj.getItalicAngle.asInstanceOf[Short],
//				obj.getFlags, GlyphWidth_protoSerializer.read(obj.getGlyphWidth))
//		}
//	}
//
//	object FontBBox_protoSerializer {
//		def write(obj: FontBBox): FontBBox_proto = {
//			val builder = FontBBox_proto.newBuilder()
//			builder.setLowerLeftX(obj.lowerLeftX)
//			builder.setLowerLeftY(obj.lowerLeftY)
//			builder.setUpperRightX(obj.upperRightX)
//			builder.setUpperRightY(obj.upperRightY)
//			builder.build()
//		}
//
//		def read(obj: FontBBox_proto): FontBBox = {
//			new FontBBox(obj.getLowerLeftX.asInstanceOf[Short], obj.getLowerLeftY.asInstanceOf[Short], obj.getUpperRightX.asInstanceOf[Short], obj.getUpperRightY.asInstanceOf[Short])
//		}
//	}
//
//	object GlyphWidth_protoSerializer {
//		def write(obj: GlyphWidth): GlyphWidth_proto = {
//			val builder = GlyphWidth_proto.newBuilder()
//			builder.setFirstChar(obj.firstChar)
//			builder.setLastChar(obj.lastChar)
//			obj.widthList.foreach(item => {
//				builder.addWidthListItem(item)
//			})
//			builder.build()
//		}
//
//		def read(obj: GlyphWidth_proto): GlyphWidth = {
//			new GlyphWidth(obj.getFirstChar.asInstanceOf[Short], obj.getLastChar.asInstanceOf[Short], obj.getWidthListItemList.asScala.map(item => {
//				item.asInstanceOf[Short]
//			}).to[List]
//			)
//		}
//	}
//
//	object PdfAction_protoSerializer {
//		def write(obj: PdfAction): PdfAction_proto = {
//			val builder = PdfAction_proto.newBuilder()
//			builder.setId(obj.id)
//			builder.build()
//		}
//
//		def read(obj: PdfAction_proto): PdfAction = {
//			//			new PdfAction(obj.getId)
//			null
//		}
//	}
//
//	object PdfText_protoSerializer {
//		def write(obj: PdfText): PdfText_proto = {
//			val builder = PdfText_proto.newBuilder()
//			obj.txtList.foreach(item => {
//				builder.addTxtListItem(PdfTxtChuck_protoSerializer.write(item))
//			})
//			builder.build()
//		}
//
//		def read(obj: PdfText_proto): PdfText = {
//			new PdfText(obj.getTxtListItemList.asScala.map(item => {
//				PdfTxtChuck_protoSerializer.read(item)
//			}).to[List]
//			)
//		}
//	}
//
//	object PdfTxtChuck_protoSerializer {
//		def write(obj: PdfTxtChuck): PdfTxtChuck_proto = {
//			val builder = PdfTxtChuck_proto.newBuilder()
//			builder.setX(obj.x)
//			builder.setY(obj.y)
//			builder.setRtext(ReportTxt_protoSerializer.write(obj.rtext))
//			builder.setFontRefName(obj.fontRefName)
//			builder.setPattern(OptionPatternDraw_protoSerializer.write(obj.pattern))
//			builder.build()
//		}
//
//		def read(obj: PdfTxtChuck_proto): PdfTxtChuck = {
//			new PdfTxtChuck(obj.getX, obj.getY, ReportTxt_protoSerializer.read(obj.getRtext), obj.getFontRefName, OptionPatternDraw_protoSerializer.read(obj.getPattern))
//		}
//	}
//
//	object ReportTxt_protoSerializer {
//		def write(obj: ReportTxt): ReportTxt_proto = {
//			val builder = ReportTxt_proto.newBuilder()
//			builder.setTxt(obj.txt)
//			builder.setFont(RFont_protoSerializer.write(obj.font))
//			builder.build()
//		}
//
//		def read(obj: ReportTxt_proto): ReportTxt = {
//			new ReportTxt(obj.getTxt, RFont_protoSerializer.read(obj.getFont))
//		}
//	}
//
//	object OptionPatternDraw_protoSerializer {
//		def write(obj: Option[PatternDraw]): OptionPatternDraw_proto = {
//			val builder = OptionPatternDraw_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(PatternDraw_protoSerializer.write(obj.get))
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionPatternDraw_proto): Option[PatternDraw] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(PatternDraw_protoSerializer.read(obj.getValue))
//			}
//		}
//	}
//
//
//	object RFont_protoSerializer {
//		def write(obj: RFont): RFont_proto = {
//			val builder = RFont_proto.newBuilder()
//			builder.setSize(obj.size)
//			builder.setFontName(obj.fontName)
//			builder.setAttribute(RFontAttribute_protoSerializer.write(obj.attribute))
//			builder.setColor(ReportColor_protoSerializer.write(obj.color))
//			builder.setExternalFont(OptionRFontFamily_protoSerializer.write(obj.externalFont))
//			builder.build()
//		}
//
//		def read(obj: RFont_proto): RFont = {
//			new RFont(obj.getSize, obj.getFontName, RFontAttribute_protoSerializer.read(obj.getAttribute), ReportColor_protoSerializer.read(obj.getColor), OptionRFontFamily_protoSerializer.read(obj.getExternalFont))
//		}
//	}
//
//	object PatternDraw_protoSerializer {
//		def write(obj: PatternDraw): PatternDraw_proto = {
//			val builder = PatternDraw_proto.newBuilder()
//			builder.setX1(obj.x1)
//			builder.setY1(obj.y1)
//			builder.setX2(obj.x2)
//			builder.setY2(obj.y2)
//			builder.setPattern(PdfGPattern_protoSerializer.write(obj.pattern))
//			builder.build()
//		}
//
//		def read(obj: PatternDraw_proto): PatternDraw = {
//			new PatternDraw(obj.getX1, obj.getY1, obj.getX2, obj.getY2, PdfGPattern_protoSerializer.read(obj.getPattern))
//		}
//	}
//
//
//	object RFontAttribute_protoSerializer {
//		def write(obj: RFontAttribute.Value): RFontAttribute_proto = {
//			obj match {
//				case RFontAttribute.NORMAL => RFontAttribute_proto.NORMAL
//				case RFontAttribute.BOLD => RFontAttribute_proto.BOLD
//				case RFontAttribute.ITALIC => RFontAttribute_proto.ITALIC
//				case RFontAttribute.BOLD_ITALIC => RFontAttribute_proto.BOLD_ITALIC
//			}
//		}
//
//		def read(obj: RFontAttribute_proto): RFontAttribute.Value = {
//			obj match {
//				case RFontAttribute_proto.NORMAL => RFontAttribute.NORMAL
//				case RFontAttribute_proto.BOLD => RFontAttribute.BOLD
//				case RFontAttribute_proto.ITALIC => RFontAttribute.ITALIC
//				case RFontAttribute_proto.BOLD_ITALIC => RFontAttribute.BOLD_ITALIC
//			}
//		}
//	}
//
//	object OptionRFontFamily_protoSerializer {
//		def write(obj: Option[RFontFamily]): OptionRFontFamily_proto = {
//			val builder = OptionRFontFamily_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(RFontFamily_protoSerializer.write(obj.get))
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionRFontFamily_proto): Option[RFontFamily] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(RFontFamily_protoSerializer.read(obj.getValue))
//			}
//		}
//	}
//
//	object RFontFamily_protoSerializer {
//		def write(obj: RFontFamily): RFontFamily_proto = {
//			val builder = RFontFamily_proto.newBuilder()
//			builder.setName(obj.name)
//			builder.setRegular(obj.regular)
//			builder.setBold(OptionString_protoSerializer.write(obj.bold))
//			builder.setItalic(OptionString_protoSerializer.write(obj.italic))
//			builder.setBoldItalic(OptionString_protoSerializer.write(obj.boldItalic))
//			builder.build()
//		}
//
//		def read(obj: RFontFamily_proto): RFontFamily = {
//			new RFontFamily(obj.getName, obj.getRegular, OptionString_protoSerializer.read(obj.getBold), OptionString_protoSerializer.read(obj.getItalic), OptionString_protoSerializer.read(obj.getBoldItalic))
//		}
//	}
//
//
//	object OptionString_protoSerializer {
//		def write(obj: Option[String]): OptionString_proto = {
//			val builder = OptionString_proto.newBuilder()
//			builder.setNotNull(obj.isDefined)
//			if (obj.isDefined) {
//				builder.setValue(obj.get)
//			}
//			builder.build()
//		}
//
//		def read(obj: OptionString_proto): Option[String] = {
//			if (!obj.getNotNull) {
//				None
//			} else {
//				Some(obj.getValue)
//			}
//		}
//	}

}
