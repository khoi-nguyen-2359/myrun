package com.media2359.routeparser.gpx.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

/**
 * PersonType<br>
 * Generated using Android JAXB<br>
 * @link https://github.com/yeshodhan/android-jaxb
 *
 */
@Root(name = "personType")
@Namespace(reference = "http://www.topografix.com/GPX/1/1")
public class PersonType {

    @Element(name = "name", required = false)
    private String name;
    @Element(name = "email", required = false)
    private EmailType email;
    @Element(name = "link", required = false)
    private LinkType link;

    public PersonType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmailType getEmail() {
        return email;
    }

    public void setEmail(EmailType email) {
        this.email = email;
    }

    public LinkType getLink() {
        return link;
    }

    public void setLink(LinkType link) {
        this.link = link;
    }

}
