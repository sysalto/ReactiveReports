package example.financialReport

import java.util.Date


object FinancialReportData {
	val rand = scala.util.Random
	val regionList = List("Alabama", "Arizona", "California", "Colorado", "Florida", "Montana")

	val nameList=List("Aaren","Adelina","Agatha","Gerry","Gilda","Juliet","Maia","Phil")

	val tradeTypList=List("Sell","Buy")

	val statusList=List("Accepted","Cancel","Progress")

	val riskList=List("Low","Medium","High")

	val accountNameList=List("John Doe","Richard Roe","John Smith")

	val accountType=List("AWT","OQV","PWZ","RZQ")

	val occupationList=List("dev","qa","manager")



	case class Transaction(invCode: Int, invDescription: String,
	                       tradeType: String, th: String, grossAmount: BigDecimal, netAmmount: BigDecimal,
	                       price: BigDecimal, quantity: BigDecimal, commision: BigDecimal, orderStatus: String,
	                       entryDate: Date, tradeDate: Date, settleDate: Date, tradeAgent: String, lta: String, exchg: String, risk: String)

	case class Account(accountNbr: Long, name: String, planType: String, kycNbr: Long, dob: Date, occupation: String, employer: String, bussinessType: String,
	                   investKnowledge: String, indvIncome: String, persNetWorth: String, timeHorizon: String, riskTolerance: String, accntInvestObj: String)

	case class Trade(amount: Int, fee: Int)

	case class Agent(region: String, branch: Int, name: String, tranList: Seq[Transaction] = Seq(), accntList: Seq[Account] = Seq(), tradeList: Seq[Trade] = Seq())

	private def getRegion(): String = {
		val i = rand.nextInt(regionList.length)
		regionList(i)
	}

	private def getName(): String = {
		val i = rand.nextInt(nameList.length)
		nameList(i)
	}

	private def getTrade(): String = {
		val i = rand.nextInt(tradeTypList.length)
		tradeTypList(i)
	}

	private def getStatus(): String = {
		val i = rand.nextInt(statusList.length)
		statusList(i)
	}

	private def getRisk(): String = {
		val i = rand.nextInt(riskList.length)
		riskList(i)
	}

	private def getAccountType(): String = {
		val i = rand.nextInt(accountType.length)
		accountType(i)
	}

	private def getOccupation(): String = {
		val i = rand.nextInt(occupationList.length)
		occupationList(i)
	}

	private def getAccountName(): String = {
		val i = rand.nextInt(accountNameList.length)
		accountNameList(i)
	}

	def getData: Seq[Agent] = {

		val today = new Date()
		for (i <- 1 to 80) yield {
			val tranList = for (j <- 1 to rand.nextInt(20)+1) yield {
				Transaction(rand.nextInt(9999), s"Investment ${j}", getTrade, s"th${j}", 20 * j, 7 * j, 9 * j, 3 * j, 2 * j, getStatus, today, today, today,
					s"agent${j}", s"lta${j}", s"exchg${j}",getRisk)
			}
			val accntList = for (j <- 1 to rand.nextInt(30)) yield {
				Account(j, getAccountName, getAccountType, j, today,getOccupation, s"empl${j}", s"bussType${j}", s"inv know${j}", s"${j * 10000}", s"${j * 7000}",
					s"time${j}", s"risk${j}", s"obj${j}")
			}
			val tradeList = for (j <- 1 to rand.nextInt(80)) yield {
				Trade(rand.nextInt(1000), rand.nextInt(120))
			}
			Agent(getRegion, rand.nextInt(20), getName, tranList, accntList, tradeList)
		}
	}

}
