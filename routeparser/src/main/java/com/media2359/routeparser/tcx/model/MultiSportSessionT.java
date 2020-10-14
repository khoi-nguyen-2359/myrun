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

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


@Root(name = "MultiSportSession_t", strict = false)
@Namespace(reference = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2")
public class MultiSportSessionT {

    // TODO
//    @Element(name = "Id", required = true)
//    protected XMLGregorianCalendar id;

    @Element(name = "FirstSport", required = true)
    protected FirstSportT firstSport;
    @ElementList(name = "NextSport", entry = "NextSport", required = false, inline = true)
    protected List<NextSportT> nextSport;
    @Element(name = "Notes", required = false)
    protected String notes;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
//    public XMLGregorianCalendar getId() {
//        return id;
//    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
//    public void setId(XMLGregorianCalendar value) {
//        this.id = value;
//    }

    /**
     * Gets the value of the firstSport property.
     * 
     * @return
     *     possible object is
     *     {@link FirstSportT }
     *     
     */
    public FirstSportT getFirstSport() {
        return firstSport;
    }

    /**
     * Sets the value of the firstSport property.
     * 
     * @param value
     *     allowed object is
     *     {@link FirstSportT }
     *     
     */
    public void setFirstSport(FirstSportT value) {
        this.firstSport = value;
    }

    /**
     * Gets the value of the nextSport property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nextSport property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNextSport().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NextSportT }
     * 
     * 
     */
    public List<NextSportT> getNextSport() {
        if (nextSport == null) {
            nextSport = new ArrayList<NextSportT>();
        }
        return this.nextSport;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

}