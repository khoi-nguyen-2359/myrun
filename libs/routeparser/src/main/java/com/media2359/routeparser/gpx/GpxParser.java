package com.media2359.routeparser.gpx;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.media2359.routeparser.RouteParser;
import com.media2359.routeparser.domain.Route;
import com.media2359.routeparser.domain.Track;
import com.media2359.routeparser.domain.Waypoint;
import com.media2359.routeparser.gpx.model.Gpx;
import com.media2359.routeparser.gpx.model.TrkType;
import com.media2359.routeparser.gpx.model.TrksegType;
import com.media2359.routeparser.gpx.model.WptType;

public class GpxParser implements RouteParser {

    @Override
    public Route parseInputStream(InputStream inputStream) throws Exception {
        Serializer serializer = new Persister();
        Gpx gpx = serializer.read(Gpx.class, inputStream);
        return new Route(extractTracks(gpx));
    }

    private List<Track> extractTracks(Gpx gpx) {
        List<Track> tracks = new ArrayList<>();
        for (TrkType trackT : gpx.getTrk()) {
            tracks.add(new Track(extractPoints(trackT)));
        }
        return tracks;
    }

    private List<Waypoint> extractPoints(TrkType trackT) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (TrksegType segmentT : trackT.getTrkseg()) {
            waypoints.addAll(extractPoints(segmentT));
        }
        return waypoints;
    }

    private List<Waypoint> extractPoints(TrksegType segmentT) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (WptType waypointT : segmentT.getTrkpt()) {
            waypoints.add(new Waypoint(
                    waypointT.getLat() != null ? waypointT.getLat() : 0,
                    waypointT.getLon() != null ? waypointT.getLon() : 0,
                    waypointT.getEle() != null ? waypointT.getEle() : 0
            ));
        }
        return waypoints;
    }

}
