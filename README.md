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
@Bootstrap(reload = true)
```
To exclude a groovy script from being bootstrapped:
```groovy
@Exclude 
```
To add mixin(s) (since 1.14):
```groovy
@Updater(name = "Name", mixin = "hippo:named, mix:referenceable")
```
## Use of the maven plugin
In the build section of the (new) module containing the groovy updater scripts, define the execution of the plugin  
*For Hippo 12 use the *'generate-yaml'* goal.*
```xml
  <plugin>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-maven-plugin</artifactId>
    <version>${groovy-updater-maven-plugin.version}</version>
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

#### These are the configuration keys of the plugin:
* **sourceDir** 
  *  the source of the groovy files  
    ```default: ${project.build.scriptSourceDirectory}```
* **targetDir** 
  *  where to generate the bootstrap  
     ```(default: ${project.build.outputDirectory})```
* **defaultContentRoot** (since 1.12)
  *  default contentroot value _(registry/queue)_   
     ```(default: queue)```
* **initializeNamePrefix** 
  *  prefix in the ecm-extension.xml nodenames   
     ```(default:hippo-updater-) ``` 
* **yamlContentPath** (since 1.13)
  *  relative path for the yaml queue bootstrap files
     ```(default: hcm-content/configuration/update)```
* **yamlConfigurationPath** (since 1.13)
  *  relative path for the yaml registry bootstrap files
     ```(default: hcm-config/configuration/update)```


When using a separate module in Hippo 12, don't forget to place an hcm-module.yaml in the project.
```yaml
group:
  name: hippoproject
project: hippoproject
module: 
  name: hippoproject-repository-data-updaters
  after: hippoproject-repository-data-content
```
### Commonly used dependencies for writing the groovy scripts
```xml
    <dependency>
      <groupId>nl.openweb.hippo.updater</groupId>
      <artifactId>groovy-updater-annotations</artifactId>
      <version>${groovy-updater-maven-plugin.version}</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>org.apache.jackrabbit</groupId>
      <artifactId>jackrabbit-core</artifactId>
      <version>${hippo.jackrabbit.version}</version>
      <scope>provided</scope>
    </dependency>
```
## Updater Script Synchronisation module
> This module has copied some logic and classes from the well known webfiles module.
 
The updater scripts add changed groovy scripts to the updaters registry.
the module is registered at:
  `/hippo:configuration/hippo:modules/groovyfiles`

Add this dependency to the cms or a dependency of the cms, like a separate 'updaters' module or 'content', use this for local development only.  
It is highly recommended to use a profile for it.
```xml
  <dependency>
    <groupId>nl.openweb.hippo.updater</groupId>
    <artifactId>groovy-updater-sync</artifactId>
    <version>${groovy-updater-maven-plugin.version}</version>
  </dependency>
```
By default the updater-sync plugin watches a module named 'updater', to use a different module as source for the scripts, 
set the system property `groovy.sync.watchedModules` in the cargo container.
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
### Log4j
Add an info logging level for nl.openweb.hippo.groovy to your development log4j configuration
```xml
    <logger name="nl.openweb.hippo.groovy" additivity="false">
      <level value="info"/>
      <appender-ref ref="messages"/>
    </logger>
```
**Log4j2:**
```xml
    <Logger name="nl.openweb.hippo.groovy" additivity="false" level="info">
      <AppenderRef ref="messages"/>
    </Logger>
```
