package util.fonts.parsers

import java.nio.file.StandardOpenOption

import util.SyncFileUtil
import util.fonts.parsers.FontParser.{EmbeddedFontDescriptor, FontBBox, FontMetric, GlyphWidth}

class TtfParser(fontFile: String) extends FontParser(fontFile) {
	private[this] type Tables = Map[String, TtfTable]

	private[this] case class TtfTable(offset: Int, length: Int)

	private[this] case class Head(f: SyncFileUtil, tables: Tables) {
		private[this] val tbl = tables("head")
		f.seek(tbl.offset + 18)
		val unitsPerEm: Short = f.readShort()
		f.skipBytes(16)
		val xMin: Short = f.readShort()
		val yMin: Short = f.readShort()
		val xMax: Short = f.readShort()
		val yMax: Short = f.readShort()
	}

	private[this] case class Hhea(f: SyncFileUtil, tables: Tables) {
		private[this] val tbl = tables("hhea")
		f.seek(tbl.offset + 4)
		val ascent = f.readShort()
		f.seek(tbl.offset + 34)
		val numOfLongHorMetrics = f.readShort()
	}


	private[this] case class Post(f: SyncFileUtil, tables: Tables) {
		private[this] val tbl = tables("post")
		f.seek(tbl.offset + 4)
		val italicAngle = f.readShort()
	}


	private[this] case class Os2(f: SyncFileUtil, tables: Tables) {
		private[this] val tbl = tables("OS/2")
		f.seek(tbl.offset + 68)
		val sTypoAscender = f.readShort()
		val sTypoDescender = f.readShort()
		f.skipBytes(16)
		val sCapHeight = f.readShort()
	}


	private[this] case class Hmtx(f: SyncFileUtil, tables: Tables, head: Head, hhea: Hhea) {
		val size = hhea.numOfLongHorMetrics
		private[this] val tbl = tables("hmtx")
		f.seek(tbl.offset)
		val hMetrics: List[Short] = (for (i <- 0 until size) yield {
			val v = f.readShort()
			f.skipBytes(2)
			convertToPdfUnits(v, head)
		}).toList
	}

	private[this] case class CMap(f: SyncFileUtil, tables: Tables) {
		def getGlyphList(hMetrics: List[Short]): Map[Char, Short] = {
			val f10 = cmapSubTables((1, 0))
			val glyphWidth = f10.map {
				case (char, id) =>
					char -> hMetrics(id)
			}
			glyphWidth
		}


		private[this] def getCMapSubTables(f: SyncFileUtil): Map[(Short, Short), Map[Char, Short]] = {
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
				case ((platformId, platformEncodeId), offset) =>
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

		private[this] def readCMapFormat(format: Short): Map[Char, Short] = {
			format match {
				case 6 => {
					f.skipBytes(4)
					val firstChar = f.readShort().toChar
					val charLength = f.readShort()
					(for (i <- 0 until charLength) yield {
						(firstChar + i).toChar -> f.readShort()
					}).toMap
				}
				case _ => Map[Char, Short]()
			}

		}


		private[this] val tbl = tables("cmap")
		private[this] val tblOffset = tbl.offset.toLong
		f.seek(tblOffset + 2)
		val cmapSubTables = getCMapSubTables(f)

	}


	private[this] case class Name(f: SyncFileUtil, tables: Tables) {
		private[this] val tbl = tables("name")
		private[this] val nameOffset = tbl.offset.toLong
		f.seek(nameOffset + 2)
		private[this] val nameRecordsCount = f.readShort()
		private[this] val storageStart = f.readShort()
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

	private[this] def getTables(f: SyncFileUtil): Tables = {
		val numTables = f.readShort()
		f.skipBytes(6)
		val tables1 = for (i <- 1 to numTables) yield {
			val tag = f.readString(4)
			f.skipBytes(4)
			val offset = f.readInt()
			val length = f.readInt()
			tag -> TtfTable(offset, length)
		}
		tables1.toMap
	}


	private[this] def convertToPdfUnits(number: Short, head: Head): Short = {
		(number * 1000 / head.unitsPerEm).toShort
	}


	//
	//	private[this] val f = new SyncFileUtil(fontFile, 0, StandardOpenOption.READ)
	//	f.skipBytes(4)
	//	private[this] val tables = getTables(f)
	//	private[this] val head = Head(f)
	//	private[this] val hhea = Hhea(f)
	//	private[this] val hmtx = Hmtx(f) //, hhea.numOfLongHorMetrics, head.unitsPerEm)
	//	private[this] val cmap = CMap(f)
	//	private[this] val name = Name(f)
	//	private[this] val os2 = Os2(f)
	//	private[this] val post = Post(f)
	//	val fontBBox = FontBBox(convertToPdfUnits(head.xMin), convertToPdfUnits(head.yMin), convertToPdfUnits(head.xMax), convertToPdfUnits(head.yMax))
	//	val fontDescriptor = FontDescriptor(convertToPdfUnits(os2.sTypoAscender), convertToPdfUnits(os2.sCapHeight),
	//		convertToPdfUnits(os2.sTypoDescender), fontBBox, post.italicAngle, 1 << 5)


	override protected[this] def parseFont(): FontMetric = {
		val f = new SyncFileUtil(fontFile, 0, StandardOpenOption.READ)
		f.skipBytes(4)
		val tables = getTables(f)
		val head = Head(f, tables)
		val hhea = Hhea(f, tables)
		val hmtx = Hmtx(f, tables, head, hhea) //, hhea.numOfLongHorMetrics, head.unitsPerEm)
		val cmap = CMap(f, tables)
		val name = Name(f, tables)
		val os2 = Os2(f, tables)
		val post = Post(f, tables)
		val fontBBox = FontBBox(convertToPdfUnits(head.xMin, head), convertToPdfUnits(head.yMin, head),
			convertToPdfUnits(head.xMax, head), convertToPdfUnits(head.yMax, head))

		def getWidths: GlyphWidth = {
			val l1 = cmap.getGlyphList(hmtx.hMetrics)
			val keyset = l1.keySet
			val min = keyset.min.toShort
			val max = keyset.max.toShort
			val l3 = l1.map {
				case (key, value) => (key.toInt, value)
			}.toList.sortBy {
				case (key, value) => key
			}.map {
				case (key, value) => value.toShort
			}
			GlyphWidth(min, max, l3)
		}

		val fontDescriptor = EmbeddedFontDescriptor(convertToPdfUnits(os2.sTypoAscender, head),
			convertToPdfUnits(os2.sCapHeight, head),
			convertToPdfUnits(os2.sTypoDescender, head), fontBBox, post.italicAngle, 1 << 5,getWidths)



		def getFontMetric(): FontMetric = {
			val l1 = cmap.getGlyphList(hmtx.hMetrics).map {
				case (char, lg) => char.toByte.toInt -> (lg.toFloat*0.001).toFloat
			}
			FontMetric(name.nameList(4), l1, Some(fontDescriptor))
		}

		getFontMetric()
	}
}

object TtfParser {
	def main(args: Array[String]): Unit = {
		val ttfParser = new TtfParser("/home/marian/transfer/font/Roboto-Regular.ttf")
	}

}
