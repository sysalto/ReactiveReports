package com.sysalto.report.examples

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Sink
import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ImplicitsAkka._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.akka.util.AkkaGroupUtil
import scala.concurrent.ExecutionContext.Implicits.global
import com.sysalto.report.reportTypes.{GroupUtil, ReportPageOrientation}
import com.sysalto.report.util.PdfFactory


object TwitterReport extends ReportAppAkka with AkkaGroupUtil {
	val consumerToken = ConsumerToken(key = "R7Cym5Vd9d0WcoWKqV2NQwKKL", secret = "JCSjtKsqpdUDU5yDBvu0I4BCGuvIMR9oQ9Z8SFdLXu2IH5ePjs")
	val accessToken = AccessToken(key = "790679832128544769-fOdevJTxdsufyMCCjxBYYnaJTzYtsoi", secret = "cl5gmg4Dm7MNpFLUHwKy50lye0QB7zsxO4gaVZ19k9NeB")

	val client = TwitterStreamingClient(consumerToken, accessToken)

	def printTweetText: PartialFunction[StreamingMessage, Unit] = {
		case tweet: Tweet => {
			if (tweet.text.toLowerCase().contains("")) {
				println(tweet.user.get.name + " " + tweet.text)
			}
		}
	}

	def test2(): Unit = {

		client.sampleStatuses(languages = List(Language.English), stall_warnings = true)(printTweetText)
	}


	def printHashtags(tweet: Tweet) = tweet.entities.map { e =>
		e.hashtags.foreach { h =>
			println(h.text)
		}
	}

	def filterTweetByHashtag(tweet: Tweet, myAwesomeHashtag: String): Option[Tweet] = tweet.entities.flatMap { e =>
		val hashtagTexts = e.hashtags.map(_.text.toUpperCase)
		if (hashtagTexts.contains(myAwesomeHashtag.toUpperCase)) Some(tweet)
		else None
		if (tweet.text.toLowerCase.contains(myAwesomeHashtag.toLowerCase)) Some(tweet)
		else None
		Some(tweet)
	}


	def test(): Unit = {
		client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("america"))(printTweetText)
		client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("canada"))(printTweetText)

		println("OK")

	}


	def test3(): Unit = {
		//		import com.sysalto.render.PdfNativeFactory
		//		import com.sysalto.report.Implicits._
		//		val headerColor = RColor(240, 250, 255)
		//		val config: Config = ConfigFactory.parseString(
		//			"""akka.log-dead-letters=off
		//       akka.jvm-exit-on-fatal-error = true
		//      akka.log-dead-letters-during-shutdown=off """)
		//		implicit val system = ActorSystem("Sys", config)
		//		implicit val materializer = ActorMaterializer()

		implicit val pdfITextFactory = new PdfNativeFactory()
		val report = Report("Twitter.pdf")

		val source1 = Source.queue[String](1000, OverflowStrategy.backpressure).take(100)

		val queue = source1.to(Sink foreach (
			txt => {
				if (report.lineLeft<5) {
					report.nextPage()
					report.nextLine()
				}
				report.nextLine(2)
				report print txt at 10

			}
			)).run()


//		val stream = client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("canada")) {
		val stream = client.sampleStatuses(languages = List(Language.English), stall_warnings = true) {
			case tweet: Tweet => {
				queue offer tweet.text
			}
		}


		val result = queue.watchCompletion()
		Await.ready(result, Duration.Inf)
		stream.map(st => st.close())
		report.render()
		system.terminate()
		println("Report was generated in Twitter.pdf")
	}

	def main(args: Array[String]): Unit = {
		test3()
	}
}
