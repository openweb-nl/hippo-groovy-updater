/*
 * Copyright 2019 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.openweb.hippo.groovy.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import nl.openweb.hippo.groovy.model.Constants;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element ref="{http://www.jcp.org/jcr/sv/1.0}node"/&gt;
 *         &lt;element ref="{http://www.jcp.org/jcr/sv/1.0}property"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"nodeOrProperty"})
@XmlRootElement(name = "node")
public class Node {

    @XmlElements({@XmlElement(name = "node", type = Node.class), @XmlElement(name = "property", type = Property.class)})
    protected List<Object> nodeOrProperty;

    @XmlTransient
    protected List<Node> subnodes;
    @XmlTransient
    protected List<Property> properties;

    @XmlAttribute(name = "name", namespace = "http://www.jcp.org/jcr/sv/1.0", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String name;

    /**
     * Gets the value of the nodeOrProperty property.
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the nodeOrProperty property.
     * </p>
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getNodeOrProperty().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Node }
     * {@link Property }
     * </p>
     *
     * @return List of nodes and properties
     */
    public List<Object> getNodeOrProperty() {
        if (nodeOrProperty == null) {
            nodeOrProperty = new ArrayList<>();
        }
        return this.nodeOrProperty;
    }

    /**
     * @param nodeName Name of a subnode to be fetched.
     * @return the fist subnode with of the given name and null if there is not any node with this name
     */
    public Node getSubnodeByName(String nodeName) {
        Node result = null;
        List<Node> subnodesList = getSubnodesByName(nodeName);
        if (!subnodesList.isEmpty()) {
            result = subnodesList.get(0);
        }
        return result;
    }

    /**
     * @param nodeName Name of a subnodes to be fetched.
     * @return returns a list of all subnodes of the given name and an empty list if there is not any.
     */
    public List<Node> getSubnodesByName(String nodeName) {
        List<Node> result = new ArrayList<>();
        List<Node> subnodesList = getSubnodes();
        for (Node node : subnodesList) {
            if (nodeName.equals(node.getName())) {
                result.add(node);
            }
        }
        return result;
    }

    public List<Node> getSubnodesByType(String nodeType) {
        List<Node> result = new ArrayList<>();
        List<Node> subnodesList = getSubnodes();
        for (Node node : subnodesList) {
            Property nodeTypeProperty = node.getPropertyByName(Constants.PropertyName.JCR_PRIMARY_TYPE);
            if (nodeType.equals(nodeTypeProperty.getSingleValue())) {
                result.add(node);
            }
        }
        return result;
    }

    public Property getPropertyByName(String propertyName) {
        Property result = null;
        List<Property> propertiesList = getProperties();
        for (Property property : propertiesList) {
            if (propertyName.equals(property.getName())) {
                result = property;
                break;
            }
        }
        return result;
    }

    public List<Node> getSubnodes() {
        if (subnodes == null) {
            subnodes = new ArrayList<>();
            List<Object> list = getNodeOrProperty();
            for (Object object : list) {
                if (object instanceof Node) {
                    subnodes.add((Node) object);
                }
            }
        }
        return this.subnodes;
    }

    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();
            List<Object> list = getNodeOrProperty();
            for (Object object : list) {
                if (object instanceof Property) {
                    properties.add((Property) object);
                }
            }
        }
        return this.properties;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Node node = (Node) o;

        return new EqualsBuilder()
                .append(nodeOrProperty, node.nodeOrProperty)
                .append(subnodes, node.subnodes)
                .append(properties, node.properties)
                .append(name, node.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nodeOrProperty)
                .append(subnodes)
                .append(properties)
                .append(name)
                .toHashCode();
    }
}
