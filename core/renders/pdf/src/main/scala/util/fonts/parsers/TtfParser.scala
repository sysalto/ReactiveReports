package util.fonts.parsers

import java.io.FileOutputStream
import java.nio.file.StandardOpenOption

import util.SyncFileUtil

class TtfParser(fontFile: String) {

	case class FontBBox(lowerLeftX: Short, lowerLeftY: Short, upperRightX: Short, upperRightY: Short) {
		override def toString: String = {
			lowerLeftX+" "+lowerLeftY+" "+upperRightX+" "+upperRightY
		}
	}

	case class FontDescriptor(ascent: Short, capHeight: Short, descent: Short, fontBBox: FontBBox
	                          ,italicAngle:Short,flags:Int)

	case class GlyphWidth(firstChar: Short, lastChar: Short, widthList: List[Short])

	private[this] case class TtfTable(offset: Int, length: Int)

	private[this] case class Head(f: SyncFileUtil) {
		private[this] val tbl = tables("head")
		f.seek(tbl.offset + 18)
		val unitsPerEm: Short = f.readShort()
		f.skipBytes(16)
		val xMin = f.readShort()
		val yMin = f.readShort()
		val xMax = f.readShort()
		val yMax = f.readShort()
	}

	private[this] case class Hhea(f: SyncFileUtil) {
		private[this] val tbl = tables("hhea")
		f.seek(tbl.offset + 4)
		val ascent = f.readShort()
		f.seek(tbl.offset + 34)
		val numOfLongHorMetrics = f.readShort()
	}


	private[this] case class Post(f: SyncFileUtil) {
		private[this] val tbl = tables("post")
		f.seek(tbl.offset + 4)
		val italicAngle = f.readShort()
	}


	private[this] case class Os2(f: SyncFileUtil) {
		private[this] val tbl = tables("OS/2")
		f.seek(tbl.offset + 68)
		val sTypoAscender = f.readShort()
		val sTypoDescender = f.readShort()
		f.skipBytes(16)
		val sCapHeight = f.readShort()
	}



	private[this] case class Hmtx(f: SyncFileUtil) {
		val size = hhea.numOfLongHorMetrics
		private[this] val tbl = tables("hmtx")
		f.seek(tbl.offset)
		val hMetrics: List[Short] = (for (i <- 0 until size) yield {
			val v = f.readShort()
			f.skipBytes(2)
			convertToPdfUnits(v)
		}).toList
	}

	private[this] case class CMap(f: SyncFileUtil) {
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


	private[this] case class Name(f: SyncFileUtil) {
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

	private[this] def getTables(f: SyncFileUtil): Map[String, TtfTable] = {
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

	def getFontName: String = name.nameList(4)

	def getWidthsN: GlyphWidth = {
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


	private[this] def convertToPdfUnits(number: Short): Short = {
		(number * 1000 / head.unitsPerEm).toShort
	}



	private[this] val f = new SyncFileUtil(fontFile, 0, StandardOpenOption.READ)
	f.skipBytes(4)
	private[this] val tables = getTables(f)
		println(tables.mkString("\n"))
	private[this] val head = Head(f)
	private[this] val hhea = Hhea(f)
	private[this] val hmtx = Hmtx(f) //, hhea.numOfLongHorMetrics, head.unitsPerEm)
	private[this] val cmap = CMap(f)
	private[this] val name = Name(f)
	private[this] val os2 = Os2(f)
	private[this] val post = Post(f)
	val fontBBox = FontBBox(convertToPdfUnits(head.xMin), convertToPdfUnits(head.yMin), convertToPdfUnits(head.xMax), convertToPdfUnits(head.yMax))
	val fontDescriptor = FontDescriptor(convertToPdfUnits(os2.sTypoAscender), convertToPdfUnits(os2.sCapHeight),
		convertToPdfUnits(os2.sTypoDescender), fontBBox,post.italicAngle,1<<5)
}

object TtfParser {
	def main(args: Array[String]): Unit = {
		val ttfParser = new TtfParser("/home/marian/transfer/font/Roboto-Regular.ttf")
	}

}
