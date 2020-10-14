
package com.media2359.routeparser.gpx.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;


/**
 * LinkType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 * 
 */
@Root(name = "linkType", strict = false)
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class LinkType {

    @Element(name = "text", required = false)
    private String text;
    @Element(name = "type", required = false)
    private String type;

    //TODO: <xsd:attribute name="href" type="xsd:anyURI" use="required">
//    @Attribute(name = "href", required = true)
//    private Object href;

    public LinkType() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    public Object getHref() {
//        return href;
//    }
//
//    public void setHref(Object href) {
//        this.href = href;
//    }

}
