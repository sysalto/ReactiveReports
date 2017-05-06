package util

/**
  * Created by marian on 5/6/17.
  */
object FontAfmParser {

  // private def getFileContent(file: String) = scala.io.Source.fromFile(file)("latin1").getLines().toList


  case class FontAfmMetric(maxHeight: Int, list: Map[String, Int])

  case class GlyphDef(list: Map[Int, String])

  def readFile(fileName: String): List[String] = {
    val stream = getClass.getClassLoader.getResourceAsStream(fileName)
    scala.io.Source.fromInputStream(stream)("latin1").getLines().toList
  }

  private def getValue(list: List[String], key: String): (Int, Int) = {
    val index: Int = list.indexWhere(line => line.startsWith(key))
    if (index == -1) {
      (index, 0)
    } else {
      val line = list(index)
      val tbl = line.trim.split("""\s""")
      (index, tbl(1).toInt)
    }
  }

  def parseGlyph(): GlyphDef = {
    val charMap = scala.collection.mutable.HashMap[String, Int]()
    val content = readFile("fonts/agl-aglfn-master/glyphlist.txt")
    val list1 = content.filter(s => !s.trim.startsWith("#"))
    val list2 = list1.map(line => {
      val tbl = line.split(";")
      val tbl1 = tbl(1).split(" ").toList
      tbl(0) -> tbl1.map(item => Integer.parseInt(item, 16))
    }).toMap
    val result = list2.filter { case (name, list) => {
      list.length == 1
    }
    }.map { case (name, list) => list.head -> name }
    GlyphDef(result)
  }

  def parseFont(fontName: String): FontAfmMetric = {
    val textList = readFile(s"fonts/${fontName}.afm")
    val upperHeight = getValue(textList, "CapHeight")._2
    val lowerHeight = getValue(textList, "XHeight")._2
    val metrics = getValue(textList, "StartCharMetrics")
    val lineNbr = metrics._1
    val itemNbr = metrics._2
    val list1 = textList.slice(lineNbr.toInt + 1, lineNbr.toInt + 1 + itemNbr)
    val charList = list1.map(line => {
      val regExpr1 ="""C\s+-?\d+\s+;\s+WX\s+(\d+)\s+;\s+N\s+(\S+).*""".r
      val regExpr1(width, name) = line.trim
      name -> width.toInt
    }).toMap
    FontAfmMetric(upperHeight, charList)
  }

  def getStringWidth(str: String, fontMetric: FontAfmMetric) (implicit glyphDef:GlyphDef): Int = {
0
  }

  def main(args: Array[String]): Unit = {
    implicit val glypList = parseGlyph()
    val fontMetric = parseFont("Helvetica")
    getStringWidth("aaa     bb cc d efg",fontMetric)
  }

}
