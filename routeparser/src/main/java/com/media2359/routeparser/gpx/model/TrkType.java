
package com.media2359.routeparser.gpx.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.List;


/**
 * TrkType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 * 
 */
@Root(name = "trkType", strict = false)
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class TrkType {

    @Element(name = "name", required = false)
    private String name;
    @Element(name = "cmt", required = false)
    private String cmt;
    @Element(name = "desc", required = false)
    private String desc;
    @Element(name = "src", required = false)
    private String src;
    @ElementList(name = "link", entry = "link", inline = true, required = false)
    private List<LinkType> link;
    @Element(name = "number", required = false)
    private Integer number;
    @Element(name = "type", required = false)
    private String type;

    // TODO: <xsd:element name="extensions"	type="extensionsType"	minOccurs="0">
//    @Element(name = "extensions", required = false)
//    private ExtensionsType extensions;

    @ElementList(name = "trkseg", entry = "trkseg", inline = true, required = false)
    private List<TrksegType> trkseg;

    public TrkType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCmt() {
        return cmt;
    }

    public void setCmt(String cmt) {
        this.cmt = cmt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<LinkType> getLink() {
        return link;
    }

    public void setLink(List<LinkType> link) {
        this.link = link;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    public ExtensionsType getExtensions() {
//        return extensions;
//    }
//
//    public void setExtensions(ExtensionsType extensions) {
//        this.extensions = extensions;
//    }

    public List<TrksegType> getTrkseg() {
        return trkseg;
    }

    public void setTrkseg(List<TrksegType> trkseg) {
        this.trkseg = trkseg;
    }

}
