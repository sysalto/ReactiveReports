/*
 *  This file is part of the ReactiveReports project.
 *  Copyright (c) 2017 Sysalto Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Sysalto. Sysalto DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see https://www.gnu.org/licenses/agpl-3.0.en.html.
 */

package com.sysalto.report.akka.template

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.sysalto.report.akka.util.AkkaGroupUtil
import com.sysalto.report.util.ResultSetUtilTrail
import com.typesafe.config.{Config, ConfigFactory}


trait ReportAppAkka extends ResultSetUtilTrail with AkkaGroupUtil {

	val config: Config = ConfigFactory.parseString(
		"""akka.log-dead-letters=off
       akka.jvm-exit-on-fatal-error = true
      akka.log-dead-letters-during-shutdown=off """)
	implicit val system = ActorSystem("Sys", config)
	implicit val materializer = ActorMaterializer()
	// implicit val pdfITextFactory = new PdfITextFactory()
}
