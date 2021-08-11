package com.media2359.routeparser.domain;

import java.util.List;

public class Route {
    private List<Track> tracks;

    public Route(List<Track> tracks) {
        this.tracks = tracks;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route route = (Route) o;

        return tracks.equals(route.tracks);
    }

    @Override
    public int hashCode() {
        return tracks.hashCode();
    }
}
