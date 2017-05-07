package nl.openweb.hippo.groovy.maven;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXB;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import nl.openweb.hippo.groovy.model.jaxb.Node;
import static java.util.stream.Collectors.toList;
import static nl.openweb.hippo.groovy.XmlGenerator.addClassPath;
import static nl.openweb.hippo.groovy.XmlGenerator.getEcmExtensionNode;
import static nl.openweb.hippo.groovy.XmlGenerator.getGroovyFiles;
import static nl.openweb.hippo.groovy.XmlGenerator.getUpdateScriptNode;
import static nl.openweb.hippo.groovy.XmlGenerator.getUpdateScriptXmlFilename;
import static nl.openweb.hippo.groovy.model.Constants.Files.ECM_EXTENSIONS_NAME;

@Mojo(name = "generate")
public class GroovyToUpdaterXML extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.resources.resource.directory}/resources", property = "sourceDir")
    private File sourceDir;
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "targetDir")
    private File targetDir;
    @Parameter(defaultValue = "hippo-updater-", property = "initializeNamePrefix")
    private String initializeNamePrefix;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("sources: " + sourceDir.getAbsolutePath());
        log.info("target: " + targetDir.getAbsolutePath());
        log.info("Add outputDirectory to classpath for project files: " + targetDir.getPath());
        addClassPath(targetDir.getPath());

        List<File> groovyFiles = getGroovyFiles(sourceDir);
        List<File> parsedGroovyFiles = processUpdateScripts(groovyFiles);
        writeEcmExtensions(parsedGroovyFiles);
    }

    private void writeEcmExtensions(final List<File> parsedGroovyFiles) throws MojoExecutionException {
        Node ecmExtensionNode = getEcmExtensionNode(sourceDir, parsedGroovyFiles, initializeNamePrefix);
        if(ecmExtensionNode == null){
            throw new MojoExecutionException("No input for " + ECM_EXTENSIONS_NAME);
        }
        targetDir.mkdirs();
        JAXB.marshal(ecmExtensionNode, new File(targetDir, ECM_EXTENSIONS_NAME));
    }

    private List<File> processUpdateScripts(final List<File> groovyFiles) {
        getLog().info("Converting " + groovyFiles.size() + " groovy scripts to updater xml");
        return groovyFiles.stream().filter(this::processUpdateScript).collect(toList());
    }

    private boolean processUpdateScript(File file){
        getLog().debug("Converting " + file.getAbsolutePath() + " to updater xml");
        Node updateScriptNode = getUpdateScriptNode(file);
        if(updateScriptNode == null){
            getLog().error("Unparsable file: " + file.getAbsolutePath());
            return false;
        }
        File targetFile = new File(targetDir, getUpdateScriptXmlFilename(sourceDir, file));
        targetFile.getParentFile().mkdirs();
        getLog().info("Write " + targetFile.getAbsolutePath());
        JAXB.marshal(updateScriptNode, targetFile);
        return true;
    }
}
