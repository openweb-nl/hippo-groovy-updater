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
In the root pom, add the repositories:
```xml
 <repositories>
    ...
    <!-- needed for the groovy updater sync module --> 
    <repository>
      <id>openweb-maven-releases</id>
      <url>https://maven.open-web.nl/content/repositories/releases/</url>
    </repository>
  </repositories>
  <pluginRepositories>
    <!-- needed for the maven groovy updater plugin -->
    <pluginRepository>
      <id>openweb-maven-releases</id>
      <url>https://maven.open-web.nl/content/repositories/releases/</url>
    </pluginRepository>
  </pluginRepositories>
```
In the build section define (minimal) usage of the plugin
```xml
  <plugin>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-maven-plugin</artifactId>
    <version>1.5</version>
    <executions>
      <execution>
        <id>default-resources</id>
        <phase>compile</phase>
        <goals>
          <goal>generate-xml</goal>
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
    <version>1.5</version>
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
          <goal>generate-xml</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```
Since Hippo 12 use the yaml generating, you need th generate-yaml goal. \
```xml
  <plugin>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-maven-plugin</artifactId>
    <version>1.5</version>
    <configuration>
      <sourceDir>${project.build.scriptSourceDirectory}</sourceDir>
      <targetDir>${project.build.outputDirectory}</targetDir>
      <initializeNamePrefix>sampleproject-update-</initializeNamePrefix>
      <yamlPath>hcm-content/configuration/update</yamlPath>
    </configuration>
    <executions>
      <execution>
        <id>default-resources</id>
        <phase>compile</phase>
        <goals>
          <goal>generate-yaml</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```
Add the hcm-module.yaml in the project yourself if you choose to have a separate module for updaters.
```yaml
group:
  name: myhippoproject
project: myhippoproject
module: 
  name: myhippoproject-repository-data-updaters
  after: myhippoproject-repository-data-content
```
### Commonly used dependencies for writing the groovy scripts
```xml
    <dependency>
      <groupId>nl.openweb.hippo.updater</groupId>
      <artifactId>groovy-updater-annotations</artifactId>
      <version>1.5</version>
      <scope>provided</scope>
    </dependency>
```
## Updater Script Synchronisation module
> This module has copied some logic and classes from the well known webfiles module.
 
The updater scripts add changed groovy scripts to the updaters registry.
the module is registered at:
  `/hippo:configuration/hippo:modules/groovyfiles`

Add this dependency to the cms or a dependency of the cms, like the updaters module itself, for local development only. \
It is highly recommended to use a profile for it.
```xml
  <dependency>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-sync</artifactId>
    <version>1.5</version>
  </dependency>
```
By default the updater-sync plugin watches a module named 'updater', to use a different module as source for the scripts, 
add the system property `groovy.sync.watchedModules` in the cargo container.
```xml
  <plugin>
    <groupId>org.codehaus.cargo</groupId>
    <artifactId>cargo-maven2-plugin</artifactId>
    <configuration>
      ...
      <container>
        ...
        <systemProperties>
          ...
          <groovy.sync.watchedModules>updater</groovy.sync.watchedModules>
        </systemProperties>
      </container>
    </configuration>
  </plugin>
```
Notes:
* The sample app only provides an example for the use of the maven plugin. \
  It does not take into account usage of buildnumber for the updaters
* It is recommended to add an info logging level for nl.openweb.hippo.groovy to your development log4j configuration