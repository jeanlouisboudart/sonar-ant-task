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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.sonar.api.batch.bootstrap.ProjectDefinition;

import com.thoughtworks.xstream.XStream;

public class ProjectDefinitionTask extends SonarBaseTask {
    private File file;

    @Override
    public void execute() throws BuildException {
        if (file == null) {
            throw new BuildException("file attribute is mandatory");
        }
        ProjectDefinition projectDefinition = buildProjectDefinition();
        XStream xStream = new XStream();
        // avoid marchalling stuff dealing with classloader 
        // thread otherwise we would not able to unmarshall it
        // this occurs when serializing Project.class
        xStream.omitField(AntTypeDefinition.class, "classLoader");
        xStream.omitField(Project.class, "listeners");
        xStream.omitField(Project.class, "threadTasks");
        xStream.omitField(Project.class, "threadGroupTasks");
        try {
            xStream.toXML(projectDefinition, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
