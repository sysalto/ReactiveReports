package util.fonts.parsers

import java.io.FileOutputStream
import java.nio.file.StandardOpenOption

import util.SyncFileUtil

class TtfParser(fontFile: String) {

	private case class TtfTable(offset: Int, length: Int)

	private case class Head(f: SyncFileUtil, tables: Map[String, TtfTable]) {
		private val tbl = tables.get("head").get
		f.seek(tbl.offset + 18)
		val unitsPerEm = f.readShort()
	}

	private case class Hhea(f: SyncFileUtil, tables: Map[String, TtfTable]) {
		private val tbl = tables.get("hhea").get
		f.seek(tbl.offset + 34)
		val numOfLongHorMetrics = f.readShort()
	}


	private case class Hmtx(f: SyncFileUtil, tables: Map[String, TtfTable], size: Short, unitsPerEm: Short) {
		private val tbl = tables.get("hmtx").get
		f.seek(tbl.offset)
		val hMetrics = (for (i <- 0 until size) yield {
			val v = f.readShort()
			f.skip(2)
			(v * 1000 / unitsPerEm)
		}).toList
	}

	private case class CMap(f: SyncFileUtil) {
		def getGlyphList(hMetrics: List[Int]): Map[Char, Int] = {
			val f10 = cmapSubTables.get((1, 0)).get
			val glyphWidth = f10.map {
				case (char, id) => {
					char -> hMetrics(id)
				}
			}
			glyphWidth
		}


		private def getCMapSubTables(f: SyncFileUtil): Map[(Short, Short), Map[Char, Short]] = {
			val nameRecordsCount =
				f.readShort()
			val ll =
				for (i <- 1 to nameRecordsCount) yield {
					val platformId = f.readShort()
					val platformEncodeId = f.readShort()
					val offset = f.readInt()
					(platformId, platformEncodeId) -> offset
				}
			val ll1 =
				ll.toMap.filter { case ((platformId, platformEncodeId), _) => {
					(platformId == 1 && platformEncodeId == 0) || (platformId == 3 && (platformEncodeId == 0 || platformEncodeId == 1 || platformEncodeId == 10))
				}
				}
			ll1.map {
				case ((platformId, platformEncodeId), offset) => {
					(platformId, platformEncodeId) match {
						case (1, 0) => {
							f.seek(tblOffset + offset)
							val format = f.readShort()
							(platformId, platformEncodeId) -> readCMapFormat(format)
						}
						case _ => {
							(platformId, platformEncodeId) -> Map[Char, Short]()
						}
					}
				}
			}
		}

		private def readCMapFormat(format: Short): Map[Char, Short] = {
			format match {
				case 6 => {
					f.skip(4)
					val firstChar = f.readShort().toChar
					val charLength = f.readShort()
					(for (i <- 0 until charLength) yield {
						(firstChar + i).toChar -> f.readShort()
					}).toMap
				}
				case _ => Map[Char, Short]()
			}

		}


		private val tbl = tables.get("cmap").get
		private val tblOffset = tbl.offset.toLong
		f.seek(tblOffset + 2)
		val cmapSubTables = getCMapSubTables(f)

	}


	private case class Name(f: SyncFileUtil) {
		private val tbl = tables.get("name").get
		private val nameOffset = tbl.offset.toLong
		f.seek(nameOffset + 2)
		private val nameRecordsCount = f.readShort()
		private val storageStart = f.readShort()
		val nameList = (for (i <- 1 to nameRecordsCount) yield {
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
		}).toMap
	}

	private def getTables(f: SyncFileUtil): Map[String, TtfTable] = {
		val numTables = f.readShort()
		f.skip(6)
		val tables1 = for (i <- 1 to numTables) yield {
			val tag = f.readString(4)
			f.skip(4)
			val offset = f.readInt()
			val length = f.readInt()
			tag -> TtfTable(offset, length)
		}
		tables1.toMap
	}

	def getFontName: String = name.nameList.get(4).get

	def getWidths: List[Int] = hmtx.hMetrics //.map(f=>f+500)
	def getWidthsN: (Short,Short,List[Short]) = {
		val l1=cmap.getGlyphList(hmtx.hMetrics)
		val keyset=l1.keySet
		val min=keyset.min.toShort
		val max=keyset.max.toShort
		val l3 = l1.map {
			case (key, value) => (key.toInt, value)
		}.toList.sortBy {
			case (key, value) => key
		}.map {
			case (key, value) => value.toShort
		}
		(min,max,l3)
	}


	def test() {
		val f = new SyncFileUtil("/home/marian/workspace/GenSNew/good2.pdf", 271, StandardOpenOption.READ)
		f.skip(4)
		val tables = getTables(f)
		println(tables.mkString("\n"))
		val head = Head(f, tables)
		val hhea = Hhea(f, tables)
		val hmtx = Hmtx(f, tables, hhea.numOfLongHorMetrics, head.unitsPerEm)
		val cmap = CMap(f)
		val name = Name(f)
		println(hmtx.hMetrics.slice(37, 38).mkString("\n"))
	}


	private val f = new SyncFileUtil(fontFile, 0, StandardOpenOption.READ)
	f.skip(4)
	private val tables = getTables(f)
	private val head = Head(f, tables)
	private val hhea = Hhea(f, tables)
	private val hmtx = Hmtx(f, tables, hhea.numOfLongHorMetrics, head.unitsPerEm)
	private val cmap = CMap(f)
	private val name = Name(f)
}

object TtfParser {



	def test1(): Unit = {
		val name = "/home/marian/workspace/GenSNew/good2.pdf"
		//		val name="/home/marian/workspace/ReactiveReports/Test.pdf"
		//		val name="/home/marian/transfer/font/Roboto-Regular.ttf"
		val f = new SyncFileUtil(name, 271, StandardOpenOption.READ)
		f.skip(4)
		val numTables = f.readShort()
		f.skip(6)
		val tables1 = for (i <- 1 to numTables) yield {
			val tag = f.readString(4)
			f.skip(4)
			val offset = f.readInt()
			val length = f.readInt()
			println(tag)
		}
	}

	def main(args: Array[String]): Unit = {
		//test()
	}

}
