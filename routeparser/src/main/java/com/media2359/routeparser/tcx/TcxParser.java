package com.media2359.routeparser.tcx;

import com.media2359.routeparser.RouteParser;
import com.media2359.routeparser.domain.Route;
import com.media2359.routeparser.domain.Track;
import com.media2359.routeparser.domain.Waypoint;
import com.media2359.routeparser.tcx.model.*;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TcxParser implements RouteParser {

    @Override
    public Route parseInputStream(InputStream inputStream) throws Exception {
        Serializer serializer = new Persister();
        TrainingCenterDatabaseT tcx = serializer.read(TrainingCenterDatabaseT.class, inputStream);
        return new Route(extractTracks(tcx));
    }

    private List<Track> extractTracks(TrainingCenterDatabaseT database) {
        List<Track> tracks = new ArrayList<>();
        if(database.getActivities() != null){
            for (ActivityT activityT : database.getActivities().getActivity()) {
                extractActivityTracks(tracks, activityT);
            }
        }

        if(database.getCourses() != null){
            for (CourseT courseT : database.getCourses().getCourse()) {
                extractCourseTracks(tracks, courseT);
            }
        }

        return tracks;
    }

    private void extractCourseTracks(List<Track> tracks, CourseT courseT) {
        for (TrackT trackT : courseT.getTrack()) {
            tracks.add(new Track(extractGpsTrackPoints(trackT)));
        }
    }

    private void extractActivityTracks(List<Track> tracks, ActivityT activityT) {
        for (ActivityLapT lap : activityT.getLap()) {
            extractLapTracks(tracks, lap);
        }
    }

    private void extractLapTracks(List<Track> tracks, ActivityLapT lap) {
        for (TrackT trackT : lap.getTrack()) {
            tracks.add(new Track(extractGpsTrackPoints(trackT)));
        }
    }

    private List<Waypoint> extractGpsTrackPoints(TrackT trackT) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (TrackpointT pointT : trackT.getTrackpoint()) {
            PositionT positionT = pointT.getPosition();
            if (positionT != null) {
                waypoints.add(new Waypoint(
                        positionT.getLatitudeDegrees(),
                        positionT.getLongitudeDegrees(),
                        pointT.getAltitudeMeters()
                ));
            }
        }
        return waypoints;
    }

}
