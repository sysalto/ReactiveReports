package com.sysalto.render.util.fonts.parsers.otf

import java.nio.file.StandardOpenOption

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{Offset32, Tag, Uint16, Uint32}

object OtfTest {


  def test1(): Unit = {
    parse("/home/marian/Downloads/Montserrat-Regular.otf")
    //    parse("/home/marian/Downloads/NotoSansSC-Regular.otf")
    //    parse("/home/marian/workspace/ReactiveReports/examples/src/main/scala/example/fonts/roboto/Roboto-Regular.ttf")
  }

  def test2(): Unit = {
    val parser=new OtfFontParser("/home/marian/Downloads/Montserrat-Regular.otf")
    val fontMetric=parser.getFontMetric()
    println(fontMetric)
  }

  def parse(file: String): Unit = {
    val f = new SyncFileUtil(file, 0, StandardOpenOption.READ)
    val ofsetTable=new OfsetTable(f)
    println("Tables nbr:"+ofsetTable.numTables)
    val tableRecordList=for (i<-1 to ofsetTable.numTables.value) yield new TableRecord(f)

    val tablesMap=tableRecordList.map(item=>item.tableTag.value->item.offset.value).toMap
    val hhea=new Hhea(f,tablesMap)
    val numberOfHMetrics=hhea.numberOfHMetrics.value
    println("Glyph Nbr:"+numberOfHMetrics)
    val hmtx=new Hmtx(f,tablesMap,numberOfHMetrics.toInt)
    val aWidth=hmtx.hMetrics(1).advanceWidth
    println("A width:"+aWidth)
    val cmap=new CMap(f,tablesMap)
    cmap.encodingRecordList.foreach(item=>{
      if (item.subTable.isDefined &&item.platformID.value==0) {
        val s1=item.subTable.get.asInstanceOf[CmapTableFormat4]
        println(s1.getGlympId('A'))
      }
    })
  }

  def main(args: Array[String]): Unit = {
    // sudo dnf install ttx
    //ttx ./NotoSansSC-Regular.otf
    //ttx  -d ./otf -s ./NotoSansSC-Regular.otf
    // https://stackoverflow.com/questions/11975349/glyph-width-in-open-type-font

    test2()
  }

}
