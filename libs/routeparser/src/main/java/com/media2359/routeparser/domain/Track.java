package com.media2359.routeparser.domain;

import java.util.List;

public class Track {
    private String name;
    private String desc;
    private List<Waypoint> points;

    public Track(String name, String desc, List<Waypoint> points) {
        this.name = name;
        this.desc = desc;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Track(List<Waypoint> points) {
        this.points = points;
    }

    public List<Waypoint> getPoints() {
        return points;
    }



    @Override
    public String toString() {
        return "Track{" +
                "points=" + points +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Track track = (Track) o;

        if (!points.equals(track.points)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }
}
