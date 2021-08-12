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

@Root(name = "Folders_t", strict = false)
@Namespace(reference = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2")
public class FoldersT {

    @Element(name = "History", required = false)
    protected HistoryT history;
    @Element(name = "Workouts", required = false)
    protected WorkoutsT workouts;
    @Element(name = "Courses", required = false)
    protected CoursesT courses;

    /**
     * Gets the value of the history property.
     * 
     * @return
     *     possible object is
     *     {@link HistoryT }
     *     
     */
    public HistoryT getHistory() {
        return history;
    }

    /**
     * Sets the value of the history property.
     * 
     * @param value
     *     allowed object is
     *     {@link HistoryT }
     *     
     */
    public void setHistory(HistoryT value) {
        this.history = value;
    }

    /**
     * Gets the value of the workouts property.
     * 
     * @return
     *     possible object is
     *     {@link WorkoutsT }
     *     
     */
    public WorkoutsT getWorkouts() {
        return workouts;
    }

    /**
     * Sets the value of the workouts property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkoutsT }
     *     
     */
    public void setWorkouts(WorkoutsT value) {
        this.workouts = value;
    }

    /**
     * Gets the value of the courses property.
     * 
     * @return
     *     possible object is
     *     {@link CoursesT }
     *     
     */
    public CoursesT getCourses() {
        return courses;
    }

    /**
     * Sets the value of the courses property.
     * 
     * @param value
     *     allowed object is
     *     {@link CoursesT }
     *     
     */
    public void setCourses(CoursesT value) {
        this.courses = value;
    }

}