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
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.bootstrap.ProjectDefinition;

public class SonarBaseTaskTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private Project antProject;
    private SonarTask task;

    @Before
    public void setUp() {
      antProject = new Project();
      antProject.setBaseDir(new File("."));
      task = new SonarTask();
      task.setProject(antProject);
    }

    @Test
    public void shouldFailIfMandatoryPropertiesMissing() {
        task.setProject(new Project());
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The following mandatory information is missing:");
        thrown.expectMessage("- task attribute 'key'");
        thrown.expectMessage("- task attribute 'version'");
        thrown.expectMessage("- task attribute 'sources' or nested 'submodules' element");

        task.checkMandatoryProperties();
    }

    @Test
    public void shouldNotFailIfMandatoryPropertiesPresentWithSystemProp() {
        task.setProject(new Project());
        task.setKey("foo");
        task.setVersion("2");
        System.setProperty("sonar.sources", "src");

        task.checkMandatoryProperties();

        System.clearProperty("sonar.sources");
    }

    @Test
    public void shouldNotFailIfMandatoryPropertiesPresentWithProjectProp() {
        antProject.setProperty("sonar.sources", "src");
        task.setProject(antProject);
        task.setKey("foo");
        task.setVersion("2");

        task.checkMandatoryProperties();
    }

    @Test
    public void shouldNotFailIfMandatoryPropertiesPresentWithTaskProp() {
        task.getProperties().put("sonar.sources", "src");
        task.setProject(new Project());
        task.setKey("foo");
        task.setVersion("2");

        task.checkMandatoryProperties();
    }

    @Test
    public void shouldNotFailIfMandatoryPropertiesNotPresentButMultiModules() {
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File("."));
        fileSet.setIncludes("**/*-sonar.xml");
        
        task.addSubmodules(fileSet);
        task.setProject(new Project());
        task.setKey("foo");
        task.setVersion("2");

        task.checkMandatoryProperties();
    }
    
    @Test
    public void defaultValues() {
      antProject.setName("My project");
      antProject.setDescription("My description");
      task.setKey("org.example:example");
      task.setVersion("0.1-SNAPSHOT");

      ProjectDefinition sonarProject = task.buildProjectDefinition();

      assertThat(sonarProject.getBaseDir(), is(antProject.getBaseDir()));
      assertThat(sonarProject.getWorkDir(), is(task.getWorkDir()));
      Properties sonarProperties = sonarProject.getProperties();
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_KEY_PROPERTY), is("org.example:example"));
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_VERSION_PROPERTY), is("0.1-SNAPSHOT"));
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_NAME_PROPERTY), is("My project"));
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_DESCRIPTION_PROPERTY), is("My description"));
    }

    @Test
    public void overrideDefaultValues() {
      antProject.setProperty(CoreProperties.PROJECT_BRANCH_PROPERTY, "branch");
      task.setKey("org.example:example");
      task.setVersion("0.1-SNAPSHOT");
      File newBaseDir = new File("newBaseDir");
      task.setBaseDir(newBaseDir);

      setProperty(task, CoreProperties.PROJECT_NAME_PROPERTY, "My project");
      setProperty(task, CoreProperties.PROJECT_DESCRIPTION_PROPERTY, "My description");
      setProperty(task, CoreProperties.PROJECT_BRANCH_PROPERTY, "Not used");

      ProjectDefinition sonarProject = task.buildProjectDefinition();

      Properties sonarProperties = sonarProject.getProperties();
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_KEY_PROPERTY), is("org.example:example"));
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_VERSION_PROPERTY), is("0.1-SNAPSHOT"));
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_NAME_PROPERTY), is("My project"));
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_DESCRIPTION_PROPERTY), is("My description"));
      assertThat(sonarProperties.getProperty(CoreProperties.PROJECT_BRANCH_PROPERTY), is("branch"));
      assertThat(sonarProject.getBaseDir(), is(newBaseDir));
    }
    
    private void setProperty(SonarTask task, String key, String value) {
        Environment.Variable var = new Environment.Variable();
        var.setKey(key);
        var.setValue(value);
        task.addConfiguredProperty(var);
      }

}
