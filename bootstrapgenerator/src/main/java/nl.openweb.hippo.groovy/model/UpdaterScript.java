package nl.openweb.hippo.groovy.model;

import java.util.List;

import nl.openweb.hippo.groovy.XmlGenerator;
import nl.openweb.hippo.groovy.model.jaxb.Node;

public class UpdaterScript {
    private String name;
    private long batchSize = 10;
    private boolean dryRun = false;
    private String parameters = "";
    private String query = "";
    private String script = "";
    private long throttle = 1000;

    public UpdaterScript(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final long batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(final String parameters) {
        this.parameters = parameters;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public long getThrottle() {
        return throttle;
    }

    public void setThrottle(final long throttle) {
        this.throttle = throttle;
    }

    public Node toNode() {
        Node rootnode = XmlGenerator.createNode(name);
        List<Object> properties = rootnode.getNodeOrProperty();
        properties.add(XmlGenerator.createProperty(Constants.PropertyName.JCR_PRIMARY_TYPE, Constants.NodeType.HIPPOSYS_UPDATERINFO, "Name"));
        properties.add(XmlGenerator.createProperty(Constants.PropertyName.HIPPOSYS_BATCHSIZE, "10", "Long"));
        properties.add(XmlGenerator.createProperty(Constants.PropertyName.HIPPOSYS_DRYRUN, "false", "Boolean"));
        properties.add(XmlGenerator.createProperty(Constants.PropertyName.HIPPOSYS_PARAMETERS, "", "String"));
        properties.add(XmlGenerator.createProperty(Constants.PropertyName.HIPPOSYS_QUERY, "", "String"));
        properties.add(XmlGenerator.createProperty(Constants.PropertyName.HIPPOSYS_SCRIPT, "", "String"));
        properties.add(XmlGenerator.createProperty(Constants.PropertyName.HIPPOSYS_THROTTLE, "", "Long"));
        return rootnode;
    }
}
