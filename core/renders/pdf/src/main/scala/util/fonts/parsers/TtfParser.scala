package util.fonts.parsers

import java.io.FileOutputStream
import java.nio.file.StandardOpenOption

import util.SyncFileUtil

class TtfParser(fontFile: String) {

	private case class TtfTable(offset: Int, length: Int)

	private case class Head() {
		private val tbl = tables.get("head").get
		f.seek(tbl.offset + 18)
		val unitsPerEm = f.readShort()
	}

	private case class Hhea() {
		private val tbl = tables.get("hhea").get
		f.seek(tbl.offset + 34)
		val numOfLongHorMetrics = f.readShort()
	}


	private case class Hmtx(size: Short, unitsPerEm: Short) {
		private val tbl = tables.get("hmtx").get
		f.seek(tbl.offset)
		val hMetrics = (for (i <- 0 until size) yield {
			val v = f.readShort()
			f.skip(2)
			(v * 1000 / unitsPerEm).toInt
		}).toList
	}

	private case class CMap() {
		def getGlyphList(hMetrics: List[Int]): Map[Char, List[Short]] = {
			val f10 = cmapSubTables.get((1, 0)).get
			val glyphWidth = f10.map { case (char, id) => {
				char -> (id, hMetrics(id))
			}
			}
			println("ok1")
			null
		}

		private def getCMapSubTables(): Map[(Short, Short), Map[Char, Short]] = {
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
		val cmapSubTables = getCMapSubTables()

	}


	private case class Name() {
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

	private def getTables(): Map[String, TtfTable] = {
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

	def readTTf(): Unit = {


		//		val ll = cmap.getGlyphList(hmtx.hMetrics)
		//		println(ll)
		//		println("OK")
		println("NAME:" + getFontName)
		println("NR:"+hhea.numOfLongHorMetrics)
		println("W:"+hmtx.hMetrics.size)

		//		val fontName = getNameTbl(f, tables)
		//		val head = getHeadTbl(f, tables)
		//		val htmx = getHtmxTbl(f, tables)
		//		val cmap = getCmapTbl(f, tables)
		//		println(fontName)
		f.close
	}


	private val f = new SyncFileUtil(fontFile, StandardOpenOption.READ)
	f.skip(4)
	private val tables = getTables()
	private val head = Head()
	private val hhea = Hhea()
	private val hmtx = Hmtx(hhea.numOfLongHorMetrics, head.unitsPerEm)
	private val cmap = CMap()
	private val name = Name()
}

object TtfParser {

	def test(): Unit = {
		val ttfParser = new TtfParser("/home/marian/transfer/font/Roboto-Black.ttf")
		ttfParser.readTTf()
	}

	def main(args: Array[String]): Unit = {
		test()
	}

}
