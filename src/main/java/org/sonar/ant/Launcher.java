/*
 * Sonar Ant Task
 * Copyright (C) 2011 SonarSource
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.ant;

import java.io.InputStream;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.Main;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.SonarException;
import org.sonar.batch.Batch;
import org.sonar.batch.bootstrapper.EnvironmentInformation;
import org.sonar.batch.bootstrapper.ProjectDefinition;
import org.sonar.batch.bootstrapper.Reactor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Launcher {

    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String DEBUG = "DEBUG";
    public static final String TRACE = "TRACE";
    private ProjectDefinition projectDefinition;
    private int antLogLevel;

    public Launcher(ProjectDefinition projectDefinition, Integer antLogLevel) {
        this.projectDefinition = projectDefinition;
        this.antLogLevel = antLogLevel;
    }

    /**
     * This method invoked from {@link SonarTask}.
     */
    public void execute() {
        Reactor reactor = new Reactor(projectDefinition);
        Configuration config = getInitialConfiguration(projectDefinition);
        initLogging(config);
        Batch batch = new Batch(config, new EnvironmentInformation("Ant",
                Main.getAntVersion()), reactor);
        batch.execute();
    }

    /**
     * TODO This method should use the component
     * org.sonar.batch.bootstrapper.LoggingConfiguration created in sonar 2.14.
     * It requires that the minimum supported version of sonar is 2.14, but it's
     * currently 2.8.
     */
    private void initLogging(Configuration config) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        InputStream input = Batch.class
                .getResourceAsStream("/org/sonar/batch/logback.xml");
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            lc.putProperty("ROOT_LOGGER_LEVEL", getLoggerLevel(config));
            lc.putProperty("SQL_LOGGER_LEVEL", getSqlLevel(config));// since
                                                                    // 2.14.
                                                                    // Ignored
                                                                    // on
                                                                    // previous
                                                                    // versions.
            lc.putProperty("SQL_RESULTS_LOGGER_LEVEL",
                    getSqlResultsLevel(config));// since 2.14. Ignored on
                                                // previous versions.
            configurator.doConfigure(input);
        } catch (JoranException e) {
            throw new SonarException("Can not initialize logging", e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    String getLoggerLevel(Configuration config) {
        if (config.getBoolean("sonar.verbose", false)) {
            return DEBUG;
        }

        switch (antLogLevel) {
        case 3:
            return DEBUG;
        case 4:
            return TRACE;
        default:
            return INFO;
        }
    }

    private Configuration getInitialConfiguration(ProjectDefinition project) {
        CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());
        configuration.addConfiguration(new EnvironmentConfiguration());
        configuration.addConfiguration(new MapConfiguration(project
                .getProperties()));
        return configuration;
    }

    protected static String getSqlLevel(Configuration config) {
        boolean showSql = config.getBoolean("sonar.showSql", false);
        return showSql ? DEBUG : WARN;
    }

    protected static String getSqlResultsLevel(Configuration config) {
        boolean showSql = config.getBoolean("sonar.showSqlResults", false);
        return showSql ? DEBUG : WARN;
    }

}
