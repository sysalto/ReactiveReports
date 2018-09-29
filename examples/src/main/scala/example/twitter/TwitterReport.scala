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

import scala.concurrent.ExecutionContext.Implicits.global
import com.sysalto.report.reportTypes.{GroupUtil, ReportPageOrientation}
import com.sysalto.report.util.PdfFactory

import scala.concurrent.Future


object TwitterReport extends ReportAppAkka with AkkaGroupUtil {
	val consumerToken = ConsumerToken(key = "R7Cym5Vd9d0WcoWKqV2NQwKKL", secret = "JCSjtKsqpdUDU5yDBvu0I4BCGuvIMR9oQ9Z8SFdLXu2IH5ePjs")
	val accessToken = AccessToken(key = "790679832128544769-fOdevJTxdsufyMCCjxBYYnaJTzYtsoi", secret = "cl5gmg4Dm7MNpFLUHwKy50lye0QB7zsxO4gaVZ19k9NeB")

	val client = TwitterStreamingClient(consumerToken, accessToken)
	val searchClient = TwitterRestClient(consumerToken, accessToken)

	def printTweetText: PartialFunction[StreamingMessage, Unit] = {
		case tweet: Tweet => {
			if (tweet.text.toLowerCase().contains("")) {
				println("Name: " + tweet.user.get.name + " text:" + tweet.text)
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
		//client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("#cat"))(printTweetText)
		//client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("canada"))(printTweetText)
		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("examples/src/main/scala/example/twitter/TwitterHashtag.pdf")

		var resultTweets = List[Tweet]()
		searchClient.searchTweet("#cats", count=10, result_type = ResultType.Recent, max_id = None).flatMap{
			ratedData =>
				val result = ratedData.data
				val tweets = result.statuses
				//resultTweets = tweets

				println("Size ==== " + tweets.length)
				tweets.foreach(tweet => {
					//print(tweet.text + "\n")

					//report.nextLine(2)
					report print tweet.text
				}

				)
				report.render()
				System.exit(0)
				Future(tweets.sortBy(_.created_at))
		} recover {
			case _ => Seq.empty
		}

		println("Size = " + resultTweets.length)
		resultTweets.foreach(tweet => {
			println(tweet)
		})

		//println(output)
		//report.render()
		//system.terminate()
		println("Report was generated in TwitterHashtag")
		//System.exit(0)
	}


	def test3(): Unit = {

		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("examples/src/main/scala/example/twitter/TwitterHashtag.pdf")

		val source1 = Source.queue[String](1000, OverflowStrategy.backpressure).take(100)

		val queue = source1.to(Sink foreach (
			txt => {
				if (report.lineLeft < 5) {
					report.nextPage()
					report.nextLine()
				}
				report.nextLine(2)
				report print txt at 10

			}
			)).run()


		//		val stream = client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("canada")) {
		val stream = client.sampleStatuses(languages = List(Language.English), stall_warnings = true,tracks = List("#fish")) {
			case tweet: Tweet => {
				println(tweet.user + " " + tweet.created_at + " " + tweet.text)
				queue offer tweet.text
			}
		}

		/*val tweetzz = searchClient.searchTweet("#cats", count=10, result_type = ResultType.Recent, max_id = None).flatMap {

			ratedData =>
				val result = ratedData.data
				val tweets = result.statuses
				//resultTweets = tweets

				println("Size ==== " + tweets.length)
				var i = 0

				tweets.foreach(tweet => {
					i = i + 1
					//queue offer tweet.text
					print(i + " " + tweet.text + "\n")

					report.nextLine(2)
					val text1 = "" + i + " " + tweet.text
					report print text1
				})
        report.render()
				Future(tweets.sortBy(_.created_at))
		} recover {
			case _ => Seq.empty
			report.render()
			system.terminate()

		}*/

		val result = queue.watchCompletion()
		//wait.ready(result, Duration.Inf)
		stream.map(st => st.close())
		report.render()
		system.terminate()
		println("Report was generated in TwitterHashtag")
		System.exit(0)
	}

	def test10(): Unit = {
//		client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("united states"))(printTweetText)
		client.filterStatuses(languages = List(Language.English), stall_warnings = true, tracks = List("canada"))(printTweetText)
	}

	def main(args: Array[String]): Unit = {
				test3()
//		test10
	}
}
