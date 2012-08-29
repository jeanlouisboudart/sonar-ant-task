Analysing with ant task
=======================


Installation
------------

**Step 1** Download the Sonar Ant Task

**Step 2** Declare the Sonar Ant Task and define the configuration in a common Ant script file:
```xml
    ...  
      <!-- Add the Sonar task -->
      <taskdef uri="antlib:org.sonar.ant" resource="org/sonar/ant/antlib.xml">
        <classpath path="path/to/sonar/ant/task/lib" /> 
        <!-- This sonar Ant task library can also be put in the ${ANT_HOME\}/lib directory-->
        <!-- In such case this classpath node is no more mandatory -->
      </taskdef>
     
      <!-- Out-of-the-box those parameters are optional -->
      <!-- EXAMPLE FOR MYSQL -->
      <property name="sonar.jdbc.url"
                value="jdbc:mysql://localhost:3306/sonar?useUnicode=true&amp;characterEncoding=utf8" />
      <property name="sonar.jdbc.driverClassName" value="com.mysql.jdbc.Driver" />
      <property name="sonar.jdbc.username" value="sonar" />
      <property name="sonar.jdbc.password" value="sonar" />
     
      <!-- SERVER ON A REMOTE HOST -->
      <property name="sonar.host.url" value="http://myserver:1234" />
    ...
```

Analyzing a Project
-------------------

**Step 1** Define a new sonar Ant target in your Ant build script:

```xml
    <project name="Example" >
      
      <!-- Define the Sonar task if this hasn't been done in a common script -->
      <taskdef uri="antlib:org.sonar.ant" resource="org/sonar/ant/antlib.xml">
        <classpath path="path/to/sonar/ant/task/lib" />
      </taskdef>
      
      <!-- Add the target -->
      <target name="sonar">
        <!-- list of mandatories Sonar properties -->
        <property name="sonar.sources" value="list of source directories separated by a comma" />
     
        <!-- list of optional Sonar properties -->
        <property name="sonar.projectName" value="this value overrides the name defined in Ant root node" />
        <property name="sonar.binaries" value="list of directories which contain for example the Java bytecode" />
        <property name="sonar.tests" value="list of test source directories separated by a comma" />
        <property name="sonar.libraries" value="list of paths to libraries separated by a comma (These libraries are for example used by the Sonar Findbugs plugin)" />
        ...
     
        <sonar:sonar key="org.example:example" version="0.1-SNAPSHOT" xmlns:sonar="antlib:org.sonar.ant"/>
      </target>
    </project>
```

Declaring a XML namespace for Ant tasks is optional but always recommended if you mix tasks from different libraries.
Additional analysis parameters can be set within <property> nodes of the sonar Ant task.

Execute the following command from the project base directory:
    ant sonar

It's recommended to build the project before, usually to get bytecode and unit test reports.

Since version 1.2 you can specify sonar.libraries using path-like structure as following:
```xml
    <path id="sonar.libraries">
      <path refid="classpath"/>
    </path>
```

Analyzing a Multi-module Project
--------------------------------

Let's say that we have a project "Parent" containing two modules "Child1" and "Child2" and we want Sonar to be able to analyze the overall Parent multi-module project:

```
Parent
  build.xml
    |
    ------ dir1/Child1
    |             build.xml
    |             src
    |             bin
    ------ dir2/Child2
                  build.xml
                  src
                  bin
```

"Child1" and "Child2" nees to define the project by using "project-definition" task  :
```xml
<sonar:project-definition key="org.example:child2" version="0.1-SNAPSHOT" xmlns:sonar="antlib:org.sonar.ant" file="child2-sonar.xml"/>
```
project-definition task will produce a file with all metadata required for analysis.

[Additional analysis](http://docs.codehaus.org/display/SONAR/Analysis+Parameters) parameters can be set within <property> nodes of the sonar Ant task.

Then, you can invoke "sonar:sonar" task in your main project :
```xml
<sonar:sonar key="org.example:example" version="0.1-SNAPSHOT" xmlns:sonar="antlib:org.sonar.ant">
   <!-- give your project definition files here -->
   <submodules dir="${basedir}" includes="**/*-sonar.xml"/>
</sonar>
```
