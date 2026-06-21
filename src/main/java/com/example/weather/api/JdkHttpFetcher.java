package com.example.weather.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Real {@link HttpFetcher} backed by the JDK's built-in {@link HttpClient}.
 *
 * <p>This is the single piece of the project that talks to the real network,
 * so it is intentionally thin and is exercised by live runs rather than unit
 * tests (which stay offline by injecting a fake fetcher instead).
 */
public class JdkHttpFetcher implements HttpFetcher {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    @Override
    public Response get(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Response(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request was interrupted", e);
        }
    }
}
