package com.sysalto.report

import com.sysalto.report.akka.util.ResultSetStreamUtil

/**
	* Created by marian on 3/25/17.
	*/
object ImplicitsAkka extends ResultSetStreamUtil {
	val Source = _root_.akka.stream.scaladsl.Source
	val Sink = _root_.akka.stream.scaladsl.Sink

}
