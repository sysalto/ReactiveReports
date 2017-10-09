package utility

import java.io.FileOutputStream
import java.nio.file.StandardOpenOption

import com.itextpdf.text.{Document, Font}
import com.itextpdf.text.pdf.{BaseFont, PdfWriter}

object FontUtil {
	val iText = true
	val fontFile = "/home/marian/transfer/font/Roboto-Black.ttf"

	def readTtfItext(): Unit = {
		val document = new Document()
		val writer = PdfWriter.getInstance(document, new FileOutputStream("good2.pdf"))
		writer.setPdfVersion(PdfWriter.VERSION_1_7)
		Document.compress = false
		document.open()
		val bf = BaseFont.createFont(fontFile, BaseFont.WINANSI, BaseFont.EMBEDDED)
		document.add(new com.itextpdf.text.Paragraph("Test23", new Font(bf, 12)))
		document.close()
	}

	def readTTf(): Unit = {

		def getCmapTbl(f: SyncFileUtil, tables: Map[String, (Int, Int)]): Map[Short, String] = {
			val tbl = tables.get("cmap").get
			val tblOffset = tbl._1.toLong
			f.seek(tblOffset+2)
			val nameRecordsCount = f.readShort()
			val ll=for (i <- 1 to nameRecordsCount) yield  {
				val platformId = f.readShort()
				val platformEncodeId = f.readShort()
				val offset=f.readInt()
				(platformId,platformEncodeId)->offset
			}
			val ll1=ll.toMap
			null
		}

		def getNameTbl(f: SyncFileUtil, tables: Map[String, (Int, Int)]): Map[Short, String] = {
			val nameTbl = tables.get("name").get
			val nameOffset = nameTbl._1.toLong
			f.seek(nameOffset + 2)
			val nameRecordsCount = f.readShort()
			val storageStart = f.readShort()
			val nameList = for (i <- 1 to nameRecordsCount) yield {
				val platformId = f.readShort()
				val encodingId = f.readShort()
				val languageId = f.readShort()
				val nameId = f.readShort()
				val stringLength = f.readShort()
				val stringOffset = f.readShort()
				val isUnicodeStr = platformId == 0 || platformId == 3 || platformId == 2 && encodingId == 1
				val offset = Some(nameOffset + storageStart + stringOffset)
				val str = if (isUnicodeStr) {
					f.readUnicodeString(stringLength, offset)
				}
				else {
					f.readString(stringLength, offset)
				}
				nameId -> str
			}
			val a1 = nameList.toMap
			nameList.toMap
		}

		val f = new SyncFileUtil(fontFile, StandardOpenOption.READ)
		f.skip(4)
		val numTables = f.readShort()
		f.skip(6)
		val tables1 = for (i <- 1 to numTables) yield {
			val tag = f.readString(4)
			f.skip(4)
			val offset = f.readInt()
			val length = f.readInt()
			tag -> (offset, length)
		}
		val tables = tables1.toMap
		val fontName = getNameTbl(f, tables)
		val cmap = getCmapTbl(f, tables)
		println(fontName)
		f.close
	}

	def ttf(): Unit = {
		if (iText) {
			readTtfItext()
		}
		readTTf()
	}

	def main(args: Array[String]): Unit = {
		ttf()
	}

}
