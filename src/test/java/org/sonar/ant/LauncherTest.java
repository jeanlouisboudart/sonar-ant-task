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

import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.bootstrap.ProjectDefinition;

public class LauncherTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Launcher launcher;
  private ProjectDefinition projectDefinition;

  @Before
  public void setUp() {
    projectDefinition = ProjectDefinition.create();
    projectDefinition.setProperties(new Properties());
    projectDefinition.setWorkDir(new File("."));
    projectDefinition.setBaseDir(new File("."));
    launcher = new Launcher(projectDefinition,Project.MSG_INFO);
  }

  @Test
  public void defaultLogLevelShouldBeInfo() {
    assertThat(launcher.getLoggerLevel(), is("INFO"));
  }

  @Test
  public void shouldEnableVerboseMode() {
    projectDefinition.getProperties().put("sonar.verbose", "true");
    assertThat(launcher.getLoggerLevel(), is("DEBUG"));
  }

  @Test
  public void shouldDisableVerboseMode() {
    projectDefinition.getProperties().put("sonar.verbose", "false");
    assertThat(launcher.getLoggerLevel(), is("INFO"));
  }

  @Test
  public void testGetSqlLevel() throws Exception {
    assertThat(launcher.getSqlLevel(), is("WARN"));

    projectDefinition.getProperties().put("sonar.showSql", "true");
    assertThat(launcher.getSqlLevel(), is("DEBUG"));

    projectDefinition.getProperties().put("sonar.showSql", "false");
    assertThat(launcher.getSqlLevel(), is("WARN"));
  }

  @Test
  public void testGetSqlResultsLevel() throws Exception {
    assertThat(launcher.getSqlResultsLevel(), is("WARN"));
    
    projectDefinition.getProperties().put("sonar.showSqlResults", "true");
    assertThat(launcher.getSqlResultsLevel(), is("DEBUG"));

    projectDefinition.getProperties().put("sonar.showSqlResults", "false");
    assertThat(launcher.getSqlResultsLevel(), is("WARN"));
  }

}
