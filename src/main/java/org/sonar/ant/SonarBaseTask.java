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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.sonar.api.CoreProperties;
import org.sonar.batch.bootstrapper.ProjectDefinition;

import com.thoughtworks.xstream.XStream;

public class SonarBaseTask extends Task {

    private static final String SONAR_SOURCES_PROPERTY = "sonar.sources";
    private File workDir;
    private File baseDir;
    private Properties properties = new Properties();
    private String key;
    private String version;
    private Path sources;
    private Path tests;
    private Path binaries;
    private Path libraries;

    private List<FileSet> submodulesFileSet;

    public SonarBaseTask() {
        super();
    }

    protected ProjectDefinition buildProjectDefinition() {
        Properties properties = new Properties();
        ProjectDefinition definition = new ProjectDefinition(getBaseDir(),
                getWorkDir(), properties);

        definition.addContainerExtension(getProject());

        // Properties from task attributes
        properties.setProperty(CoreProperties.PROJECT_KEY_PROPERTY, getKey());
        properties.setProperty(CoreProperties.PROJECT_VERSION_PROPERTY,
                getVersion());
        // Properties from project attributes
        if (!properties.containsKey(CoreProperties.PROJECT_NAME_PROPERTY)
                && getProject().getName() != null) {
            properties.setProperty(CoreProperties.PROJECT_NAME_PROPERTY,
                    getProject().getName());
        }
        if (!properties
                .containsKey(CoreProperties.PROJECT_DESCRIPTION_PROPERTY)
                && getProject().getDescription() != null) {
            properties.setProperty(CoreProperties.PROJECT_DESCRIPTION_PROPERTY,
                    getProject().getDescription());
        }
        // Properties from task
        properties.putAll(getProperties());
        // Properties from Ant
        properties.putAll(getProject().getProperties());
        setPathProperties(properties, getProject());

        // Source directories
        for (String dir : getPathAsList(createSources())) {
            definition.addSourceDir(dir);
        }
        // Test directories
        for (String dir : getPathAsList(createTests())) {
            definition.addTestDir(dir);
        }
        // Binary directories
        for (String dir : getPathAsList(createBinaries())) {
            definition.addBinaryDir(dir);
        }
        // Files with libraries
        for (String file : getPathAsList(createLibraries())) {
            definition.addLibrary(file);
        }
        defineSubProject(definition);
        return definition;
    }

    private void defineSubProject(ProjectDefinition parentProjectDefinition) {
        for (FileSet fileSet : submodulesFileSet) {
            String[] submodules = fileSet.getDirectoryScanner()
                    .getIncludedFiles();
            for (String curentSubModule : submodules) {
                XStream xStream = new XStream();
                File f = new File(curentSubModule);
                try {
                    ProjectDefinition projectDefinition = (ProjectDefinition) xStream
                            .fromXML(new FileInputStream(f));

                    parentProjectDefinition.addModule(projectDefinition);
                } catch (FileNotFoundException e) {
                    new BuildException("Can't parse project definition at "
                            + curentSubModule, e);
                }
            }
        }
    }

    private List<String> getPathAsList(Path path) {
        List<String> result = new ArrayList<String>();
        for (Iterator<?> i = path.iterator(); i.hasNext();) {
            Resource resource = (Resource) i.next();
            if (resource instanceof FileResource) {
                File fileResource = ((FileResource) resource).getFile();
                result.add(fileResource.getAbsolutePath());
            }
        }
        return result;
    }

    /**
     * @since 1.2
     */
    private void setPathProperties(Properties properties, Project antProject) {
        setPathProperty(properties, antProject, "sonar.libraries");
    }

    /**
     * @since 1.2
     */
    private void setPathProperty(Properties properties, Project antProject,
            String refid) {
        if (antProject.getReference(refid) == null) {
            return;
        }
        Object reference = antProject.getReference(refid);
        properties
                .setProperty(
                        refid,
                        Utils.convertResourceCollectionToString((ResourceCollection) reference));
    }

    /**
     * @return work directory, default is ".sonar" in project directory
     */
    public File getWorkDir() {
        if (workDir == null) {
            workDir = new File(getBaseDir(), ".sonar");
        }
        return workDir;
    }

    /**
     * @since 1.1
     */
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * @return base directory, default is the current project base directory
     * @since 1.1
     */
    public File getBaseDir() {
        if (baseDir == null) {
            baseDir = getProject().getBaseDir();
        }
        return baseDir;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    protected void checkMandatoryProperties() {
        Collection<String> missingProps = new ArrayList<String>();
        if (isEmpty(key)) {
            missingProps.add("\n  - task attribute 'key'");
        }
        if (isEmpty(version)) {
            missingProps.add("\n  - task attribute 'version'");
        }

        if (isNotFound("sonar.modules") && isSourceInfoMissing()) {
            missingProps
                    .add("\n  - task attribute 'sources' or property 'sonar.sources'");
        }
        if (!missingProps.isEmpty()) {
            StringBuilder message = new StringBuilder(
                    "\nThe following mandatory information is missing:");
            for (String prop : missingProps) {
                message.append(prop);
            }
            throw new IllegalArgumentException(message.toString());
        }
    }

    private boolean isNotFound(String string) {
        String systemProp = System.getProperty(string);
        String projectProp = getProject().getProperty(string);
        String taskProp = getProperties().getProperty(string);
        return isEmpty(systemProp) && isEmpty(projectProp) && isEmpty(taskProp);
    }

    private boolean isSourceInfoMissing() {
        return sources == null && isNotFound(SONAR_SOURCES_PROPERTY);
    }

    private boolean isEmpty(String string) {
        return string == null || "".equals(string);
    }

    /**
     * Note that name of this method is important - see
     * http://ant.apache.org/manual/develop.html#nested-elements
     */
    public void addConfiguredProperty(Environment.Variable property) {
        properties.setProperty(property.getKey(), property.getValue());
    }

    public Properties getProperties() {
        return properties;
    }

    public Path createSources() {
        if (sources == null) {
            sources = new Path(getProject());
        }
        return sources;
    }

    public Path createTests() {
        if (tests == null) {
            tests = new Path(getProject());
        }
        return tests;
    }

    public Path createBinaries() {
        if (binaries == null) {
            binaries = new Path(getProject());
        }
        return binaries;
    }

    public Path createLibraries() {
        if (libraries == null) {
            libraries = new Path(getProject());
        }
        return libraries;
    }

    /**
     * Adds a set of files to be deleted.
     * 
     * @param set
     *            the set of files to be deleted
     */
    public void addSubmodules(FileSet set) {
        submodulesFileSet.add(set);
    }

}