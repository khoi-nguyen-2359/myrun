//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.12.17 at 10:24:25 PM EET 
//


package com.media2359.routeparser.tcx.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;


@Root(name = "Distance_t", strict = false)
@Namespace(reference = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2")
public class DistanceT
    extends DurationT
{

    @Element(name = "Meters", required = false)
    protected int meters;

    /**
     * Gets the value of the meters property.
     * 
     */
    public int getMeters() {
        return meters;
    }

    /**
     * Sets the value of the meters property.
     * 
     */
    public void setMeters(int value) {
        this.meters = value;
    }

}
