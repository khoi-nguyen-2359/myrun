package com.media2359.routeparser.gpx.model;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

/**
 * Gpx<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 *
 */
@Root(name = "gpx", strict = false)
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class Gpx {

    @Element(name = "metadata", required = false)
    private MetadataType metadata;
    @ElementList(name = "wpt", entry = "wpt", inline = true, required = false)
    private List<WptType> wpt;
    @ElementList(name = "rte", entry = "rte", inline = true, required = false)
    private List<RteType> rte;
    @ElementList(name = "trk", entry = "trk", inline = true, required = false)
    private List<TrkType> trk;

    // TODO: <xsd:element name="extensions"	type="extensionsType"	minOccurs="0">
//    @Element(name = "extensions", required = false)
//    private ExtensionsType extensions;

    @Attribute(name = "creator", required = true)
    private String creator;
    @Attribute(name = "version", required = true)
    private String version;

    public Gpx() {
    }

    public MetadataType getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataType metadata) {
        this.metadata = metadata;
    }

    public List<WptType> getWpt() {
        return wpt;
    }

    public void setWpt(List<WptType> wpt) {
        this.wpt = wpt;
    }

    public List<RteType> getRte() {
        return rte;
    }

    public void setRte(List<RteType> rte) {
        this.rte = rte;
    }

    public List<TrkType> getTrk() {
        return trk;
    }

    public void setTrk(List<TrkType> trk) {
        this.trk = trk;
    }

//    public ExtensionsType getExtensions() {
//        return extensions;
//    }
//
//    public void setExtensions(ExtensionsType extensions) {
//        this.extensions = extensions;
//    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
