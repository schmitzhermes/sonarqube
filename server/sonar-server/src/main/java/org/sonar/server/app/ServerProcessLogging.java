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
package org.sonar.server.app;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import java.util.logging.LogManager;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonar.api.utils.MessageException;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.process.LogbackHelper;
import org.sonar.process.Props;
import org.sonar.server.platform.ServerLogging;

public abstract class ServerProcessLogging {
  private static final String LOG_LEVEL_PROPERTY = "sonar.log.level";
  private static final String THREAD_ID_PLACEHOLDER = "ZZZZ";
  private static final String LOG_FORMAT = "%d{yyyy.MM.dd HH:mm:ss} %-5level [" + THREAD_ID_PLACEHOLDER + "][%logger{20}] %msg%n";
  private final String processName;
  private final String threadIdField;
  private final LogbackHelper helper = new LogbackHelper();

  protected ServerProcessLogging(String processName, String threadIdField) {
    this.processName = processName;
    this.threadIdField = threadIdField;
  }

  public LoggerContext configure(Props props) {
    LoggerContext ctx = helper.getRootContext();
    ctx.reset();

    helper.enableJulChangePropagation(ctx);
    configureRootLogger(ctx, props);
    configureLevels(props);

    // Configure java.util.logging, used by Tomcat, in order to forward to slf4j
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
    return ctx;
  }

  private void configureRootLogger(LoggerContext ctx, Props props) {
    String logFormat = LOG_FORMAT.replace(THREAD_ID_PLACEHOLDER, threadIdField);
    // configure appender
    LogbackHelper.RollingPolicy rollingPolicy = helper.createRollingPolicy(ctx, props, processName);
    FileAppender<ILoggingEvent> fileAppender = rollingPolicy.createAppender("file");
    fileAppender.setContext(ctx);
    PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
    fileEncoder.setContext(ctx);
    fileEncoder.setPattern(logFormat);
    fileEncoder.start();
    fileAppender.setEncoder(fileEncoder);
    fileAppender.start();

    // configure logger
    Logger rootLogger = ctx.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.addAppender(fileAppender);
    rootLogger.detachAppender("console");
  }

  private void configureLevels(Props props) {
    String levelCode = props.value(LOG_LEVEL_PROPERTY, "INFO");
    LoggerLevel level;
    if ("TRACE".equals(levelCode)) {
      level = LoggerLevel.TRACE;
    } else if ("DEBUG".equals(levelCode)) {
      level = LoggerLevel.DEBUG;
    } else if ("INFO".equals(levelCode)) {
      level = LoggerLevel.INFO;
    } else {
      throw MessageException.of(String.format("Unsupported log level: %s. Please check property %s", levelCode, LOG_LEVEL_PROPERTY));
    }
    ServerLogging.configureLevels(helper, level);
  }
}
