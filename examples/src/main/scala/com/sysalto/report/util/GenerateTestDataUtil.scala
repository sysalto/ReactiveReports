/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
  *
 * Unless you have purchased a commercial license agreement from SysAlto
 * the following license terms apply:
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */




package com.sysalto.report.util
import com.sysalto.report.Implicits._
import com.sysalto.report.ImplicitsAkka._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.akka.util.AkkaGroupUtil
import com.sysalto.report.reportTypes.GroupUtil
//
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
//
import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps
import scala.util.Random


object GenerateTestDataUtil extends  ReportAppAkka with AkkaGroupUtil{

  sealed trait DataType

  case object StringType extends DataType

  case object NumericType extends DataType

  case class DataField(name: String, dataType: DataType, length: Int = 0)

  case class DataGroup(name: String, grpNbr: Int)

  private def getRandomString(lg: Integer): String = {
    def getRandomChar = {
      val diff = 'Z' - 'A'
      val rndNbr = Math.abs(Random.nextInt(diff))
      ('A'.toInt + rndNbr).toChar
    }

    (1 to lg).toList.foldLeft("")((s1, _) => s1 + getRandomChar)
  }

  def generate(nbr: Integer, fieldList: List[DataField]): List[Map[String, String]] = {
    (1 to nbr).toList.map(_ => {
      fieldList.map(dataField => {
        val name = dataField.name
        val rndNbr = Math.abs(Random.nextInt())
        val rndSize = if (dataField.length == 0) 0 else Random.nextInt(dataField.length)
        val value = dataField.dataType match {
          case GenerateTestDataUtil.StringType => getRandomString(rndSize)
          case GenerateTestDataUtil.NumericType => rndNbr.toString
        }
        name -> value
      }).toMap
    })

  }

  def group(groupList: List[DataGroup], fieldList: List[DataField], detailLong: Int, isRandom: Boolean = true): List[Map[String, String]] = {
    def getSize = if (isRandom) Math.abs(Random.nextInt(detailLong)) else detailLong

    def grpFn(groupList: List[DataGroup], index: Int, grpMap: Map[String, String]): List[Map[String, String]] = {
      if (index == groupList.length) {
        generate(getSize, fieldList)
      } else {
        val groupItem = groupList(index)
        (1 to groupItem.grpNbr).flatMap(grpNbr => {
          if (index == groupList.length - 1) {
            generate(getSize, fieldList).map(item => grpMap ++ item + (groupItem.name -> grpNbr.toString))
          } else {
            grpFn(groupList, index + 1, grpMap + (groupItem.name -> grpNbr.toString))
          }
        }).toList
      }
    }

    grpFn(groupList, 0, Map())
  }

  implicit class DataGroupUtil1(fields: List[DataField]) {
    def withGroup(groupList: List[DataGroup]): (List[DataField], List[DataGroup]) = {
      (fields, groupList)
    }

    def havingLength(detailLong: Int, isRandom: Boolean = true): List[Map[String, String]] = {
      group(List(), fields, detailLong, isRandom)
    }
  }

  implicit class DataGroupUtil2(data: (List[DataField], List[DataGroup])) {
    def havingLength(detailLong: Int, isRandom: Boolean = true): List[Map[String, String]] = {
      group(data._2, data._1, detailLong, isRandom)
    }
  }

  def test(): Unit = {


    val fields1 = List(DataField("id", NumericType), DataField("name", StringType, 20))
    val fields2 = List(DataField("id1", NumericType), DataField("data1", StringType, 8))
    val resultSet = fields1 withGroup List(DataGroup("grp1", 2), DataGroup("grp2", 4)) havingLength 10

    val config = ConfigFactory.parseString(
      """akka.log-dead-letters=off
      akka.log-dead-letters-during-shutdown=off """)
    implicit val system = ActorSystem("Sys", config)
    implicit val materializer = ActorMaterializer()

    implicit val timeout = Timeout(5 minutes)
    val source1 = Source(resultSet)

    val listHdr1 = List(Group("grp1", (r: Map[String, String]) => r("grp1")), Group("grp2", (r: Map[String, String]) => r("grp2")))
    val listHdr2 = List(Group("GRP3", (r: Map[String, String]) => r("GRP3")), Group("GRP4", (r: Map[String, String]) => r("GRP4")))
    val groupUtil1 = new GroupUtil(listHdr1)
    val groupUtil2 = new GroupUtil(listHdr2)


    val result1 = source1.group.
      runWith(Sink.foreach(
        rec1 => {
          val crtRec = GroupUtil.getRec(rec1)
          if (groupUtil1.isHeader("grp1", rec1)) {
            println("Header grp1:" + crtRec("grp1"))
          }
          if (groupUtil1.isHeader("grp2", rec1)) {
            println("\tHeader grp2:" ++ crtRec("grp2"))
          }
          println("\t\t" + crtRec)
          val resultSet2 = fields2 withGroup List(DataGroup("GRP3", 2), DataGroup("GRP4", 2)) havingLength 20
          val source2 = Source(resultSet2)
          println("\t\t>>Details:")
          val result2 = source2.group.
            runWith(Sink.foreach(
              rec2 => {
                val crtRec2 = GroupUtil.getRec(rec2)
                if (groupUtil2.isHeader("GRP3", rec2)) {
                  println("\t\t\tHeader GRP3:" + crtRec2("GRP3"))
                }
                if (groupUtil2.isHeader("GRP4", rec2)) {
                  println("\t\t\t\tHeader GRP4:" + crtRec2("GRP4"))
                }
                println("\t\t\t\t\t" + crtRec2)
                if (groupUtil2.isFooter("GRP4", rec2)) {
                  println("\t\t\t\tFooter GRP4:" + crtRec2("GRP4"))
                }
                if (groupUtil2.isFooter("GRP3", rec2)) {
                  println("\t\t\tFooter GRP3:" + crtRec2("GRP3"))
                }

              }
            ))
          Await.ready(result2, Duration.Inf)
          println("\t\t<<Details:")

          if (groupUtil1.isFooter("grp2", rec1)) {
            println("\tFooter grp2:" ++ crtRec("grp2"))
          }
          if (groupUtil1.isFooter("grp1", rec1)) {
            println("Footer grp1:" ++ crtRec("grp1"))
          }
        }
      ))
    Await.ready(result1, Duration.Inf)

    system.terminate()

    //		resultSet.foreach(line => {
    //			println("-" * 50)
    //			println(line)
    //			val result2 = fields2 havingLength 20
    //			println("\t" + result2.mkString("\n\t"))
    //			println("\t"+"=" * 20)
    //			val result3 = fields3 havingLength 10
    //			println("\t" + result3.mkString("\n\t"))
    //		})

  }

  def main(args: Array[String]): Unit = {
    test()
  }

}
