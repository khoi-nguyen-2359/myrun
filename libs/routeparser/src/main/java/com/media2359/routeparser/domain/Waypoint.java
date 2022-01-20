package com.media2359.routeparser.domain;

public class Waypoint {
    private final double latitude;
    private final double longitude;
    private final double altitude;

    /**
     * Defines track point
     *
     * @param latitude  - latitude in degrees
     * @param longitude - longitude in degrees
     * @param altitude  - altitude in meters
     */
    public Waypoint(final double latitude, final double longitude, final Double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;

    }

    public Waypoint(double maxLatitude, double minLongitude) {
        latitude = maxLatitude;
        longitude = minLongitude;
        altitude = 0;
    }

    @Override
    public String toString() {
        return "Waypoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                '}';
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }
}
