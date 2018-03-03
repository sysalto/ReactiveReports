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
			//PdfNames(obj.getId,PdfDests_protoSerializer.read(obj.getDests))
			null
		}
	}





}
