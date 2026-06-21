package com.example.weather.api;

import java.io.IOException;
import java.util.Objects;

/**
 * Minimal HTTP GET abstraction.
 *
 * <p>Having the network behind this one-method interface is what lets the rest
 * of the app — and all of its tests — run without ever touching the real
 * internet: tests inject a fake that returns canned responses.
 */
public interface HttpFetcher {

    /** Performs a GET and returns the status code and response body. */
    Response get(String url) throws IOException;

    /** A bare HTTP response: status code plus body text. */
    record Response(int statusCode, String body) {
        public Response {
            Objects.requireNonNull(body, "body");
        }
    }
}
