//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.12.17 at 10:24:25 PM EET 
//

package com.media2359.routeparser.tcx.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "Plan_t", strict = false)
@Namespace(reference = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2")
public class PlanT {

    @Element(name = "Name", required = false)
    protected String name;
    @Element(name = "Extensions", required = false)
    protected ExtensionsT extensions;
    @Attribute(name = "Type", required = true)
    protected TrainingTypeT type;
    @Attribute(name = "IntervalWorkout", required = true)
    protected boolean intervalWorkout;

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return
     *     possible object is
     *     {@link ExtensionsT }
     *
     */
    public ExtensionsT getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value
     *     allowed object is
     *     {@link ExtensionsT }
     *
     */
    public void setExtensions(ExtensionsT value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link TrainingTypeT }
     *
     */
    public TrainingTypeT getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link TrainingTypeT }
     *
     */
    public void setType(TrainingTypeT value) {
        this.type = value;
    }

    /**
     * Gets the value of the intervalWorkout property.
     *
     */
    public boolean isIntervalWorkout() {
        return intervalWorkout;
    }

    /**
     * Sets the value of the intervalWorkout property.
     *
     */
    public void setIntervalWorkout(boolean value) {
        this.intervalWorkout = value;
    }

}