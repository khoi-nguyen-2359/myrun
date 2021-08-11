package com.media2359.routeparser.gpx.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

/**
 * EmailType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 *
 */
@Root(name = "emailType")
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class EmailType {

    @Attribute(name = "id", required = true)
    private String id;
    @Attribute(name = "domain", required = true)
    private String domain;

    public EmailType() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

}
