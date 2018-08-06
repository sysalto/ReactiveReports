package com.sysalto.report.example.data

import java.util.Date


object DailyTradingBlotterData {


	case class Transaction(invCode: String, invDescription: String,
	                       tradeType: String, th: String, grossAmount: BigDecimal, netAmmount: BigDecimal,
	                       price: BigDecimal, quantity: BigDecimal, commision: BigDecimal, orderStatus: String,
	                       entryDate: Date, tradeDate: Date, settleDate: Date, tradeAgent: String, lta: String, exchg: String, risk: String)

	case class Account(accountNbr: Long, name: String, planType: String, kycNbr: Long, dob: Date, occupation: String, employer: String, bussinessType: String,
	                   investKnowledge: String, indvIncome: String, persNetWorth: String, timeHorizon: String, riskTolerance: String, accntInvestObj: String)

	case class Trade(lta: String, poa: String, feeBased: String, loanCollateral: String, acctRestriction: String, marketValue: BigDecimal)

	case class Agent(region: String, branch: String, name: String, tranList: Seq[Transaction] = Seq(), accntList: Seq[Account] = Seq(), tradeList: Seq[Trade] = Seq())

	def getData: Seq[Agent] = {
		val rand = scala.util.Random
		val today = new Date()
		for (i <- 1 to 10) yield {
			val tranList = for (j <- 1 to rand.nextInt(20)) yield {
				Transaction(s"inv${j}", s"descr${j}", s"trade${j}", s"th${j}", 20 * j, 7 * j, 9 * j, 3 * j, 2 * j, s"status ${j}", today, today, today,
					s"agent${j}", s"lta${j}", s"exchg${j}", s"risk${j}")
			}
			val accntList = for (j <- 1 to rand.nextInt(30)) yield {
				Account(j, s"name${j}", s"plan${j}", j, today, s"occup${j}", s"empl${j}", s"bussType${j}", s"inv know${j}", s"${j * 10000}", s"${j * 7000}",
					s"time${j}", s"risk${j}", s"obj${j}")
			}
			val tradeList = for (j <- 1 to rand.nextInt(80)) yield {
				Trade(s"lta${j}", s"poa${j}", s"fee${j}", s"loan${j}", s"restr${j}", j * 1000)
			}
			Agent(s"Region ${i}", s"Branch ${i}", s"Name ${i}", tranList, accntList, tradeList)
		}
	}

}
