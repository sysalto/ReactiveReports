package com.sysalto.report.examples

import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}

object TwitterReport {
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

		client.sampleStatuses(languages=List(Language.English),stall_warnings = true)(printTweetText)
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
		if (tweet.text.toLowerCase.contains(myAwesomeHashtag.toLowerCase)) Some (tweet)
		else None
		Some(tweet)
	}


	def test(): Unit = {
		client.filterStatuses(languages=List(Language.English),stall_warnings = true,tracks=List("trump"))(printTweetText)


	}


	def main(args: Array[String]): Unit = {
		test()
	}
}
