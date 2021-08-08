package com.media2359.routeparser.gpx.model;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

/**
 * WptType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 *
 */
@Root(name = "wptType", strict = false)
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class WptType {

    @Element(name = "ele", required = false)
    private Double ele;

    // TODO:      <xsd:element name="time"		type="xsd:dateTime"		minOccurs="0">
//    @Element(name = "time", required = false)
//    private Date time;

    @Element(name = "magvar", required = false)
    private Double magvar;
    @Element(name = "geoidheight", required = false)
    private Double geoidheight;
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
    @Element(name = "sym", required = false)
    private String sym;
    @Element(name = "type", required = false)
    private String type;

    // TODO: type "fixType"
//    @Element(name = "fix", required = false)
//    private Object fix;

    @Element(name = "sat", required = false)
    private Integer sat;
    @Element(name = "hdop", required = false)
    private Double hdop;
    @Element(name = "vdop", required = false)
    private Double vdop;
    @Element(name = "pdop", required = false)
    private Double pdop;
    @Element(name = "ageofdgpsdata", required = false)
    private Double ageofdgpsdata;
    @Element(name = "dgpsid", required = false)
    private Integer dgpsid;

    // TODO: <xsd:element name="extensions"	type="extensionsType"	minOccurs="0">
//    @Element(name = "extensions", required = false)
//    private ExtensionsType extensions;

    @Attribute(name = "lat", required = true)
    private Double lat;
    @Attribute(name = "lon", required = true)
    private Double lon;

    public WptType() {
    }

    public Double getEle() {
        return ele;
    }

    public void setEle(Double ele) {
        this.ele = ele;
    }

//    public Date getTime() {
//        return time;
//    }
//
//    public void setTime(Date time) {
//        this.time = time;
//    }

    public Double getMagvar() {
        return magvar;
    }

    public void setMagvar(Double magvar) {
        this.magvar = magvar;
    }

    public Double getGeoidheight() {
        return geoidheight;
    }

    public void setGeoidheight(Double geoidheight) {
        this.geoidheight = geoidheight;
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

    public String getSym() {
        return sym;
    }

    public void setSym(String sym) {
        this.sym = sym;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    public Object getFix() {
//        return fix;
//    }
//
//    public void setFix(Object fix) {
//        this.fix = fix;
//    }

    public Integer getSat() {
        return sat;
    }

    public void setSat(Integer sat) {
        this.sat = sat;
    }

    public Double getHdop() {
        return hdop;
    }

    public void setHdop(Double hdop) {
        this.hdop = hdop;
    }

    public Double getVdop() {
        return vdop;
    }

    public void setVdop(Double vdop) {
        this.vdop = vdop;
    }

    public Double getPdop() {
        return pdop;
    }

    public void setPdop(Double pdop) {
        this.pdop = pdop;
    }

    public Double getAgeofdgpsdata() {
        return ageofdgpsdata;
    }

    public void setAgeofdgpsdata(Double ageofdgpsdata) {
        this.ageofdgpsdata = ageofdgpsdata;
    }

    public Integer getDgpsid() {
        return dgpsid;
    }

    public void setDgpsid(Integer dgpsid) {
        this.dgpsid = dgpsid;
    }

//    public ExtensionsType getExtensions() {
//        return extensions;
//    }
//
//    public void setExtensions(ExtensionsType extensions) {
//        this.extensions = extensions;
//    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

}
