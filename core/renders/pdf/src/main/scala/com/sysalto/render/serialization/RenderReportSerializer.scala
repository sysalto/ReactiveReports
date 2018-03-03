package com.sysalto.render.serialization

import scala.collection.JavaConverters._
import RenderReportTypes._
import com.sysalto.render.serialization.RenderProto._

import scala.collection.mutable.ListBuffer

object RenderReportSerializer {
	private[this] implicit val allItems = ListBuffer[PdfBaseItem]()


	object StringString_protoSerializer {
		def write(obj: (String, String)): StringString_proto = {
			val builder = StringString_proto.newBuilder()
			builder.setValue1(obj._1)
			builder.setValue2(obj._2)
			builder.build()
		}

		def read(obj: StringString_proto): (String, String) = {
			(obj.getValue1, obj.getValue2)
		}
	}

	object PdfDests_protoSerializer {
		def write(obj: PdfDests): PdfDests_proto = {
			val builder = PdfDests_proto.newBuilder()
			builder.setId(obj.id)
			obj.dests.foreach(item => {
				builder.addDestsItem(StringString_protoSerializer.write(item))
			})
			builder.build()
		}

		def read(obj: PdfDests_proto): PdfDests = {
			new PdfDests(obj.getId, obj.getDestsItemList.asScala.map(item => {
				StringString_protoSerializer.read(item)
			}).to[ListBuffer]
			)
		}
	}

	object PdfNames_protoSerializer {
		def write(obj: PdfNames): PdfNames_proto = {
			val builder = PdfNames_proto.newBuilder()
			builder.setId(obj.id)
			builder.setDests(PdfDests_protoSerializer.write(obj.dests))
			builder.build()
		}

		def read(obj: PdfNames_proto): PdfNames = {
			new PdfNames(obj.getId,PdfDests_protoSerializer.read(obj.getDests))
		}
	}

	object OptionLong_protoSerializer {
		def write(obj: Option[Long]): OptionLong_proto = {
			val builder = OptionLong_proto.newBuilder()
			builder.setNotNull(obj.isDefined)
			if (obj.isDefined) {
				builder.setValue(obj.get)
			}
			builder.build()
		}

		def read(obj: OptionLong_proto): Option[Long] = {
			if (!obj.getNotNull) {
				None
			} else {
				Some(obj.getValue)
			}
		}
	}



	object PdfPageList_protoSerializer {
		def write(obj: PdfPageList): PdfPageList_proto = {
			val builder = PdfPageList_proto.newBuilder()
			builder.setId(obj.id)
			builder.setParentId(OptionLong_protoSerializer.write(obj.parentId))
			obj.pageList.foreach(item => {
				builder.addPageListItem(item)
			})
			builder.build()
		}

		def read(obj: PdfPageList_proto): PdfPageList = {
			new PdfPageList(obj.getId,OptionLong_protoSerializer.read(obj.getParentId),obj.getPageListItemList.asScala.
				map(item => item.asInstanceOf[Long]).to[ListBuffer]
			)
		}
	}

	object OptionPdfNames_protoSerializer {
		def write(obj: Option[PdfNames]): OptionPdfNames_proto = {
			val builder = OptionPdfNames_proto.newBuilder()
			builder.setNotNull(obj.isDefined)
			if (obj.isDefined) {
				builder.setValue(PdfNames_protoSerializer.write(obj.get))
			}
			builder.build()
		}

		def read(obj: OptionPdfNames_proto): Option[PdfNames] = {
			if (!obj.getNotNull) {
				None
			} else {
				Some(PdfNames_protoSerializer.read(obj.getValue))
			}
		}
	}


	object OptionPdfPageList_protoSerializer {
		def write(obj: Option[PdfPageList]): OptionPdfPageList_proto = {
			val builder = OptionPdfPageList_proto.newBuilder()
			builder.setNotNull(obj.isDefined)
			if (obj.isDefined) {
				builder.setValue(PdfPageList_protoSerializer.write(obj.get))
			}
			builder.build()
		}

		def read(obj: OptionPdfPageList_proto): Option[PdfPageList] = {
			if (!obj.getNotNull) {
				None
			} else {
				Some(PdfPageList_protoSerializer.read(obj.getValue))
			}
		}
	}


	object PdfCatalog_protoSerializer {
		def write(obj: PdfCatalog): PdfCatalog_proto = {
			val builder = PdfCatalog_proto.newBuilder()
			builder.setId(obj.id)
			builder.setPdfPageList(OptionPdfPageList_protoSerializer.write(obj.pdfPageList))
			builder.setPdfNames(OptionPdfNames_protoSerializer.write(obj.pdfNames))
			builder.build()
		}

		def read(obj: PdfCatalog_proto): PdfCatalog = {
			new PdfCatalog(obj.getId, OptionPdfPageList_protoSerializer.read(obj.getPdfPageList), OptionPdfNames_protoSerializer.read(obj.getPdfNames))
		}
	}


}
