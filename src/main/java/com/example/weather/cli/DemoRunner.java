package com.example.weather.cli;

import com.example.weather.history.HistoryEntry;
import com.example.weather.model.CurrentWeather;
import com.example.weather.model.Forecast;
import com.example.weather.parse.WeatherParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Runs a fully offline end-to-end demo from bundled sample JSON — no API key
 * and no network needed. Shows all three output formats (current, forecast,
 * history) so a single run demonstrates the whole app.
 */
public final class DemoRunner {

    private DemoRunner() {
    }

    public static int run(PrintStream out) {
        CurrentWeather current = WeatherParser.parseCurrent(resource("/demo/current.json"));
        Forecast forecast = WeatherParser.parseForecast(resource("/demo/forecast.json"));

        // A fixed, illustrative history so the demo also shows the history view.
        List<HistoryEntry> sampleHistory = List.of(
                new HistoryEntry(Instant.parse("2026-06-17T08:00:00Z"), "London", "GB", 12.5, "light rain"),
                new HistoryEntry(Instant.parse("2026-06-17T08:05:00Z"), "Paris", "FR", 19.0, "clear sky"),
                new HistoryEntry(Instant.parse("2026-06-17T08:10:00Z"), "Tokyo", "JP", 26.0, "few clouds"));

        out.println("=== Weather Dashboard CLI - offline demo (sample data) ===");
        out.println();
        out.println(WeatherFormatter.formatCurrent(current));
        out.println();
        out.println(WeatherFormatter.formatForecast(forecast));
        out.println();
        out.println(WeatherFormatter.formatHistory(sampleHistory));
        return 0;
    }

    private static String resource(String path) {
        try (InputStream in = DemoRunner.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("missing bundled demo resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
