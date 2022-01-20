package com.media2359.routeparser.gpx.model;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

/**
 * MetadataType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 *
 */
@Root(name = "metadataType", strict = false)
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class MetadataType {

    @Element(name = "name", required = false)
    private String name;
    @Element(name = "desc", required = false)
    private String desc;
    @Element(name = "author", required = false)
    private PersonType author;
    @Element(name = "copyright", required = false)
    private CopyrightType copyright;
    @ElementList(name = "link", entry = "link", inline = true, required = false)
    private List<LinkType> link;
    @Element(name = "time", required = false)

    // TODO: <xsd:element name="time"		type="xsd:dateTime"		minOccurs="0">
//    private Object time;
//    @Element(name = "keywords", required = false)

    private String keywords;

    // TODO:      <xsd:element name="bounds"		type="boundsType"		minOccurs="0">
//    @Element(name = "bounds", required = false)
//    private BoundsType bounds;

    // TODO: <xsd:element name="extensions"	type="extensionsType"	minOccurs="0">
//    @Element(name = "extensions", required = false)
//    private ExtensionsType extensions;

    public MetadataType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public PersonType getAuthor() {
        return author;
    }

    public void setAuthor(PersonType author) {
        this.author = author;
    }

    public CopyrightType getCopyright() {
        return copyright;
    }

    public void setCopyright(CopyrightType copyright) {
        this.copyright = copyright;
    }

    public List<LinkType> getLink() {
        return link;
    }

    public void setLink(List<LinkType> link) {
        this.link = link;
    }

//    public Object getTime() {
//        return time;
//    }
//
//    public void setTime(Object time) {
//        this.time = time;
//    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

//    public BoundsType getBounds() {
//        return bounds;
//    }
//
//    public void setBounds(BoundsType bounds) {
//        this.bounds = bounds;
//    }

//    public ExtensionsType getExtensions() {
//        return extensions;
//    }
//
//    public void setExtensions(ExtensionsType extensions) {
//        this.extensions = extensions;
//    }

}
