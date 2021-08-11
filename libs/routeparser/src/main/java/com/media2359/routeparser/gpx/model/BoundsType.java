package com.media2359.routeparser.gpx.model;

import java.math.BigDecimal;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "boundsType", strict = false)
public class BoundsType {

    @Attribute(name = "minlat", required = true)
    protected Double minlat;
    @Attribute(name = "minlon", required = true)
    protected Double minlon;
    @Attribute(name = "maxlat", required = true)
    protected Double maxlat;
    @Attribute(name = "maxlon", required = true)
    protected Double maxlon;

    /**
     * Gets the value of the minlat property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public Double getMinlat() {
        return minlat;
    }

    /**
     * Sets the value of the minlat property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setMinlat(Double value) {
        this.minlat = value;
    }

    /**
     * Gets the value of the minlon property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public Double getMinlon() {
        return minlon;
    }

    /**
     * Sets the value of the minlon property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setMinlon(Double value) {
        this.minlon = value;
    }

    /**
     * Gets the value of the maxlat property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public Double getMaxlat() {
        return maxlat;
    }

    /**
     * Sets the value of the maxlat property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setMaxlat(Double value) {
        this.maxlat = value;
    }

    /**
     * Gets the value of the maxlon property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public Double getMaxlon() {
        return maxlon;
    }

    /**
     * Sets the value of the maxlon property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setMaxlon(Double value) {
        this.maxlon = value;
    }

}
