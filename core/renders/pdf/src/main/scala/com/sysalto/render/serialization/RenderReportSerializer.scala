package com.sysalto.render.serialization


import scala.collection.JavaConverters._
import com.sysalto.render.serialization.RenderProto._
import com.sysalto.render.util.fonts.parsers.FontParser.{EmbeddedFontDescriptor, FontBBox, FontMetric, GlyphWidth}
import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, RFontFamily, ReportColor, ReportTxt}

import scala.collection.mutable.ListBuffer

class RenderReportSerializer(val renderReportTypes: RenderReportTypes) {

	object PdfCatalogSerializer {
		def write(input: renderReportTypes.PdfCatalog): PdfCatalog_proto = {
			val builder = PdfCatalog_proto.newBuilder()
			if (input.idPdfPageListOpt.isDefined) {
				builder.addIdPdfPageListOpt(input.idPdfPageListOpt.get)
			}
			if (input.idPdfNamesOpt.isDefined) {
				builder.addIdPdfNamesOpt(input.idPdfNamesOpt.get)
			}
			builder.setId(input.id)
			builder.setClassName(input.className)
			builder.build()
		}

		def read(input: PdfCatalog_proto): renderReportTypes.PdfCatalog = {
			val result = new renderReportTypes.PdfCatalog(input.getId)
			if (input.getIdPdfPageListOptCount() > 0) {
				result.idPdfPageListOpt = Some(input.getIdPdfPageListOptList.asScala.head)
			}
			if (input.getIdPdfNamesOptCount() > 0) {
				result.idPdfNamesOpt = Some(input.getIdPdfNamesOptList.asScala.head)
			}
			result
		}
	}


}
