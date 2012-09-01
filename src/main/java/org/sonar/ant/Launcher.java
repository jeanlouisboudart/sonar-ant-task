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

import org.apache.tools.ant.Main;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.batch.bootstrapper.Batch;
import org.sonar.batch.bootstrapper.Batch.Builder;
import org.sonar.batch.bootstrapper.EnvironmentInformation;

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
        ProjectReactor reactor = new ProjectReactor(projectDefinition);
        EnvironmentInformation environmentInformation = new EnvironmentInformation("Ant",
                Main.getAntVersion());
        Builder builder = Batch.builder().setProjectReactor(reactor).setEnvironment(environmentInformation);
        Batch batch = builder.build();
        batch.getLoggingConfiguration().setRootLevel(getLoggerLevel());
        batch.getLoggingConfiguration().setSqlLevel(getSqlLevel());
        batch.getLoggingConfiguration().setSqlResultsLevel(getSqlResultsLevel());
        batch.execute();
    }

    String getLoggerLevel() {
        if (toBoolean(projectDefinition.getProperties().getProperty("sonar.verbose"))) {
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

    protected String getSqlLevel() {
        boolean showSql = toBoolean(projectDefinition.getProperties().getProperty("sonar.showSql"));
        return showSql ? DEBUG : WARN;
    }

    protected String getSqlResultsLevel() {
        boolean showSql = toBoolean(projectDefinition.getProperties().getProperty("sonar.showSqlResults"));
        return showSql ? DEBUG : WARN;
    }
    
    public static boolean toBoolean(String s) {
        return ("on".equalsIgnoreCase(s)
                || "true".equalsIgnoreCase(s)
                || "yes".equalsIgnoreCase(s));
    }

}
