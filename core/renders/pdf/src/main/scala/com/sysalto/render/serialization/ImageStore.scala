package com.sysalto.render.serialization

import com.sysalto.render.serialization.RenderReportTypes._
import com.sysalto.report.util.serializers.ObjectSerialization
import com.sysalto.report.util.{PersistenceFactory, PersistenceUtil}

object ImageStore {
  var persistenceImageUtil: PersistenceUtil = null

  def apply(persistenceFactory: PersistenceFactory): Unit = {
    persistenceImageUtil = persistenceFactory.open()
  }

  def getPdfImage(fileName: String, nextId: => Long): PdfImage = {
    val key = "PdfImage:" + fileName
    val bytes = persistenceImageUtil.readObject(key)
    if (bytes != null) {
    ObjectSerialization.deserialize[PdfImage](bytes)
    } else {
      val result = new PdfImage(nextId, fileName)
      val data=ObjectSerialization.serialize(result)
      persistenceImageUtil.writeObject(key, data)
      result
    }
  }

  def getNewImageMeta(fileName: String): ImageMetaData = {
    val key = "ImageData:" + fileName
    val bytes = persistenceImageUtil.readObject(key)
    if (bytes != null) {
    ObjectSerialization.deserialize[ImageMetaData](bytes)
    } else {
      val imageData = ImageMeta.getNewFile(fileName)
      val data=ObjectSerialization.serialize(imageData)
      persistenceImageUtil.writeObject(key, data)
      imageData
    }
  }
}
