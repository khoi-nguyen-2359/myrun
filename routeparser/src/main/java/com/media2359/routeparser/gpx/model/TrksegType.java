
package com.media2359.routeparser.gpx.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.List;


/**
 * TrksegType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 * 
 */
@Root(name = "trksegType", strict = false)
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class TrksegType {

    @ElementList(name = "trkpt", entry = "trkpt", inline = true, required = false)
    private List<WptType> trkpt;

    // TODO: <xsd:element name="extensions"	type="extensionsType"	minOccurs="0">
//    @Element(name = "extensions", required = false)
//    private ExtensionsType extensions;

    public TrksegType() {
    }

    public List<WptType> getTrkpt() {
        return trkpt;
    }

    public void setTrkpt(List<WptType> trkpt) {
        this.trkpt = trkpt;
    }

//    public ExtensionsType getExtensions() {
//        return extensions;
//    }
//
//    public void setExtensions(ExtensionsType extensions) {
//        this.extensions = extensions;
//    }

}
