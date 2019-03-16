package com.sysalto.render.serialization

import com.sysalto.render.serialization.RenderReportSerializer.{ImageMetaDataSerializer, PdfBaseItemSerializer}
import com.sysalto.render.serialization.RenderReportTypes.{ImageMeta, ImageMetaData, PdfImage}
import com.sysalto.report.util.{PersistenceFactory, PersistenceUtil}
import proto.com.sysalto.render.serialization.RenderProto.{ImageMetaData_proto, PdfBaseItem_proto}

object ImageStore {
  var persistenceImageUtil: PersistenceUtil = null

  def apply(persistenceFactory: PersistenceFactory): Unit = {
    persistenceImageUtil = persistenceFactory.open()
  }

  def getPdfImage(fileName: String, nextId: => Long): PdfImage = {
    val key = "PdfImage:" + fileName
    val bytes = persistenceImageUtil.readObject(key)
    if (bytes != null) {
      val proto = PdfBaseItem_proto.parseFrom(bytes)
      PdfBaseItemSerializer.read(proto)(persistenceImageUtil).asInstanceOf[PdfImage]
    } else {
      val result = new PdfImage(nextId, fileName)
      val writeBytes = PdfBaseItemSerializer.write(result)
      persistenceImageUtil.writeObject(key, writeBytes.toByteArray)
      result
    }
  }

  def getNewImageMeta(fileName: String): ImageMetaData = {
    val key = "ImageData:" + fileName
    val bytes = persistenceImageUtil.readObject(key)
    if (bytes != null) {
      val proto = ImageMetaData_proto.parseFrom(bytes)
      ImageMetaDataSerializer.read(proto)
    } else {
      val imageData = ImageMeta.getNewFile(fileName)
      val writeBytes = ImageMetaDataSerializer.write(imageData)
      persistenceImageUtil.writeObject(key, writeBytes.toByteArray)
      imageData
    }
  }

  //  import scalaz._
  //
  //  val getNewImageMeta: String => ImageMetaData = Memo.immutableHashMapMemo {
  //    fileName => {
  //      println(s"new imageMeta $fileName")
  //      ImageMeta.getNewFile(fileName)
  //    }
  //  }
}
