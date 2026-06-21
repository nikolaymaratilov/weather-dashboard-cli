package com.example.weather.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Verifies the offline demo runs end-to-end from bundled resources. */
class DemoRunnerTest {

    @Test
    void run_printsCurrentForecastAndHistorySections() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        int rc = DemoRunner.run(out);
        String text = buffer.toString(StandardCharsets.UTF_8);

        assertEquals(0, rc);
        assertTrue(text.contains("offline demo"), text);
        assertTrue(text.contains("Current weather - London, GB"), text);
        assertTrue(text.contains("5-day forecast - London, GB"), text);
        assertTrue(text.contains("2026-06-21"), text);     // 5th forecast day present
        assertTrue(text.contains("Search history"), text);
        assertTrue(text.contains("Tokyo, JP"), text);      // sample history rendered
    }
}
