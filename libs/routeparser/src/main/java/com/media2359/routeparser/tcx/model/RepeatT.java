//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.12.17 at 10:24:25 PM EET 
//


package com.media2359.routeparser.tcx.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;


@Root(name = "Repeat_t", strict = false)
@Namespace(reference = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2")
public class RepeatT
    extends AbstractStepT
{

    @Element(name = "Repetitions", required = false)
    protected int repetitions;
    @ElementList(name = "Child", entry = "Child", required = true, inline = true)
    protected List<AbstractStepT> child;

    /**
     * Gets the value of the repetitions property.
     * 
     */
    public int getRepetitions() {
        return repetitions;
    }

    /**
     * Sets the value of the repetitions property.
     * 
     */
    public void setRepetitions(int value) {
        this.repetitions = value;
    }

    /**
     * Gets the value of the child property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the child property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChild().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractStepT }
     * 
     * 
     */
    public List<AbstractStepT> getChild() {
        if (child == null) {
            child = new ArrayList<AbstractStepT>();
        }
        return this.child;
    }

}