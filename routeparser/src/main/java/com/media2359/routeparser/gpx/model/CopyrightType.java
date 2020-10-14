
package com.media2359.routeparser.gpx.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;


/**
 * CopyrightType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 * 
 */
@Root(name = "copyrightType", strict = false)
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class CopyrightType {

    //TODO:     <xsd:element name="license"		type="xsd:anyURI"	minOccurs="0">
//    @Element(name = "year", required = false)
//    private Object year;

    //TODO:    <xsd:attribute name="href" type="xsd:anyURI" use="required">
//    @Element(name = "license", required = false)
//    private Object license;

    @Attribute(name = "author", required = true)
    private String author;

    public CopyrightType() {
    }

//    public Object getYear() {
//        return year;
//    }
//
//    public void setYear(Object year) {
//        this.year = year;
//    }

//    public Object getLicense() {
//        return license;
//    }
//
//    public void setLicense(Object license) {
//        this.license = license;
//    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
