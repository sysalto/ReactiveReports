/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
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




package com.sysalto.report.akka.template

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.sysalto.report.akka.util.AkkaGroupUtil
import com.sysalto.report.util.GroupUtilTrait
import com.typesafe.config.{Config, ConfigFactory}


trait ReportAppAkka extends GroupUtilTrait with AkkaGroupUtil {

	val config: Config = ConfigFactory.parseString(
		"""akka.log-dead-letters=off
       akka.jvm-exit-on-fatal-error = true
      akka.log-dead-letters-during-shutdown=off """)
	implicit val system = ActorSystem("Sys", config)
}
