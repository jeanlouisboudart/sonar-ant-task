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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.taskdefs.Delete;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.test.TestUtils;

public class ProjectDefinitionTaskTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private Project antProject;

    @Before
    public void setUp() {
      antProject = new Project();
      antProject.setBaseDir(new File("."));
     }
    
    @After
    public void tearDown() {
        //clean project definition before each test
        Delete deleteTask = new Delete();
        deleteTask.setProject(antProject);
        deleteTask.setDir(new File("."));
        deleteTask.setIncludes("root.xml, child1-sonar.xml, child2-sonar.xml");
        deleteTask.execute();
         
    }

    @Test
    public void singleproject() {
        antProject.init();
        ProjectHelper.configureProject(antProject, TestUtils.getResource("build.xml"));
        antProject.executeTarget("init-singleproject");
        assertThat(new File(".","root.xml").exists(), is(true));
        
    }
    
    @Test
    public void submodules() {
        antProject.init();
        ProjectHelper.configureProject(antProject, TestUtils.getResource("build.xml"));
        antProject.executeTarget("init-submodules");
        assertThat(new File(".","root.xml").exists(), is(true));
        assertThat(new File(".","child1-sonar.xml").exists(), is(true));
        assertThat(new File(".","child2-sonar.xml").exists(), is(true));
        
    }
    
}
