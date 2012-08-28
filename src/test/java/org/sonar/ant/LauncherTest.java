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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Properties;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.batch.bootstrapper.ProjectDefinition;

public class LauncherTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Launcher launcher;

  @Before
  public void setUp() {
      ProjectDefinition projectDefinition = new ProjectDefinition(new File("."), new File("."),new Properties());
    launcher = new Launcher(projectDefinition,Project.MSG_INFO);
  }

  @Test
  public void defaultLogLevelShouldBeInfo() {
    assertThat(launcher.getLoggerLevel(new PropertiesConfiguration()), is("INFO"));
  }

  @Test
  public void shouldEnableVerboseMode() {
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty("sonar.verbose", "true");
    assertThat(launcher.getLoggerLevel(config), is("DEBUG"));
  }

  @Test
  public void shouldDisableVerboseMode() {
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty("sonar.verbose", "false");
    assertThat(launcher.getLoggerLevel(config), is("INFO"));
  }

  @Test
  public void testGetSqlLevel() throws Exception {
    Configuration conf = new BaseConfiguration();

    assertThat(Launcher.getSqlLevel(conf), is("WARN"));

    conf.setProperty("sonar.showSql", "true");
    assertThat(Launcher.getSqlLevel(conf), is("DEBUG"));

    conf.setProperty("sonar.showSql", "false");
    assertThat(Launcher.getSqlLevel(conf), is("WARN"));
  }

  @Test
  public void testGetSqlResultsLevel() throws Exception {
    Configuration conf = new BaseConfiguration();

    assertThat(Launcher.getSqlResultsLevel(conf), is("WARN"));

    conf.setProperty("sonar.showSqlResults", "true");
    assertThat(Launcher.getSqlResultsLevel(conf), is("DEBUG"));

    conf.setProperty("sonar.showSqlResults", "false");
    assertThat(Launcher.getSqlResultsLevel(conf), is("WARN"));
  }

}
