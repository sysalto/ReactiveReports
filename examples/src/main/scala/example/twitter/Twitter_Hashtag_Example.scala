package example.twitter

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Sink
import com.danielasfregola.twitter4s.entities.enums.{Language, ResultType}
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient, http}
import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ImplicitsAkka._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.akka.util.AkkaGroupUtil
import example.twitter.TwitterCredentials


import scala.concurrent.ExecutionContext.Implicits.global
import com.sysalto.report.reportTypes.{GroupUtil, ReportPageOrientation}
import com.sysalto.report.util.PdfFactory
import example.twitter.TwitterReport.{accessToken, client, consumerToken, searchClient, system}

import scala.concurrent.{Future, duration}

object Twitter_Hashtag_Example extends ReportAppAkka with AkkaGroupUtil {

	//		val searchClient = TwitterRestClient(consumerToken, accessToken)
	val client = TwitterStreamingClient(TwitterCredentials.consumerToken, TwitterCredentials.accessToken)

	def createReport(): Unit = {

		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("examples/src/main/scala/example/twitter/TwitterHashtag.pdf")
		import scala.concurrent.duration._
		val source1 = Source.queue[(String, String)](1000, OverflowStrategy.backpressure).takeWithin(120.second)

		val queue = source1.to(Sink foreach (
			txt => {
				if (report.lineLeft < 5) {
					report.nextPage()
					report.nextLine()
				}
				report.nextLine(1)

				val cell1 = ReportCell(txt._1) inside ReportMargin(20, 170)
				val cell2 = ReportCell(txt._2) inside ReportMargin(171, report.pageLayout.width - 20)
				report print cell1
				report print cell2

				// Get one row down
				val box_P1 = cell1.calculate(report)
				report.setYPosition(box_P1.currentY + report.lineHeight)
				report.nextLine(2)

			}
			)).run()


		val stream = client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("#food")) {
			case tweet: Tweet => {
				println(tweet.text)
				println("TwitterName:  " + tweet.user.get.name + ", Tweet: " + tweet.extended_tweet.get.full_text)
				queue offer (tweet.user.get.name, tweet.extended_tweet.get.full_text)
			}
		}

		Await.ready(queue.watchCompletion(), Duration.Inf)
		Await.result(stream, Duration.Inf).close()
		report.render()
		println("Report was generated in TwitterHashtag")
	}


	def main(args: Array[String]): Unit = {
		createReport()
		Await.ready(client.shutdown(), Duration.Inf)
		system.terminate()
	}

}