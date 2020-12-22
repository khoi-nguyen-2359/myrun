package com.media2359.routeparser;

import com.media2359.routeparser.domain.Route;

import java.io.InputStream;

public interface RouteParser {
    Route parseInputStream(InputStream inputStream) throws Exception;
}
