/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.search;

import ch.qos.logback.classic.LoggerContext;
import org.sonar.process.LogbackHelper;
import org.sonar.process.Props;

import static org.sonar.process.LogbackHelper.RootLoggerConfig.newRootLoggerConfigBuilder;

public class SearchLogging {

  private LogbackHelper helper = new LogbackHelper();

  public LoggerContext configure(Props props) {
    LoggerContext ctx = helper.getRootContext();
    ctx.reset();

    helper.configureRootLogger(ctx, props, newRootLoggerConfigBuilder().setProcessName("es").setFileName("es").build());

    return ctx;
  }
}
