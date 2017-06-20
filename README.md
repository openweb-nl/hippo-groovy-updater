# Project for Hippo Updaters Maven Plugin
This project is designed for Hippo developers to have an easy way to create updaters in groovy, without being bothered with the bootstrap xml.

For use the updaters are located in their own submodule. Nicely separated from other bootstrap.
They are groovy scripts, so the editor can easily provide autocomplete and stuff, configured by Annotations.

The groovy scripts are by default located in:
```
/src/main/scripts
```
## Modules involved
* Groovy updater annotations
* Groovy updater bootstrap generator
* Groovy updater maven plugin
* Sample project for the maven plugin
* Synchronisation module for updater scripts

## Annotation use
Minimal use for the Updater definition
```groovy
@Updater(name = "Name", xpath = "//element(*, hst:mount)")
```
Bootstrap definition is optional 
```groovy
* @Bootstrap(reload = true)
```
## Use of the maven plugin
In the build section define (minimal) usage of the plugin
```xml
  <plugin>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-maven-plugin</artifactId>
    <version>1.2</version>
    <dependencies>
      <dependency>
        <groupId>org.onehippo.cms7</groupId>
        <artifactId>hippo-repository-dependencies</artifactId>
        <version>${hippo.repository.version}</version>
        <type>pom</type>
      </dependency>
    </dependencies>
    <executions>
      <execution>
        <id>default-resources</id>
        <phase>compile</phase>
        <goals>
          <goal>generate</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```
Or define (full) usage of the plugin
```xml
  <plugin>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-maven-plugin</artifactId>
    <version>1.2</version>
    <configuration>
      <sourceDir>${project.build.scriptSourceDirectory}</sourceDir>
      <targetDir>${project.build.outputDirectory}</targetDir>
      <initializeNamePrefix>sampleproject-update-</initializeNamePrefix>
    </configuration>
    <executions>
      <execution>
        <id>default-resources</id>
        <phase>compile</phase>
        <goals>
          <goal>generate</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```
## Commonly used dependencies for writing the groovy scripts
```xml
    <dependency>
      <groupId>org.onehippo.cms7</groupId>
      <artifactId>hippo-repository-api</artifactId>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>javax.jcr</groupId>
      <artifactId>jcr</artifactId>
      <scope>provided</scope>
    </dependency>
```
## Updater Script Synchronisation module
> This module has copied some logic and classes from the well known webfiles module.
 
The updater scripts add changed groovy scripts to the updaters registry.
the module is registered at:
  `/hippo:configuration/hippo:modules/groovyfiles`

Add this dependency to the cms, for local development only.
```xml
  <dependency>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-sync</artifactId>
    <version>1.2</version>
    <exclusions>
        <!-- Plugin uses other version numbers in project -->
        <exclusion>
          <groupId>org.onehippo.cms7</groupId>
          <artifactId>hippo-repository-dependencies</artifactId>
        </exclusion>
      </exclusions>
  </dependency>
```
Notes:
* The sample app only provides an example for the use of the maven plugin.
 It does not take into account usage of buildnumber for the updaters