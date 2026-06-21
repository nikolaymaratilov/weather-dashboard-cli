package com.example.weather.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.weather.api.CityNotFoundException;
import com.example.weather.api.WeatherProvider;
import com.example.weather.history.HistoryEntry;
import com.example.weather.history.HistoryStore;
import com.example.weather.model.CurrentWeather;
import com.example.weather.model.DailyForecast;
import com.example.weather.model.Forecast;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

/** Tests the command surface end-to-end with a fake provider and a temp history file. */
class CliAppTest {

    private static final Instant NOW = Instant.parse("2026-06-17T10:15:30Z");
    private static final Clock FIXED = Clock.fixed(NOW, ZoneOffset.UTC);

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void weather_printsResultAndRecordsHistory(@TempDir Path dir) {
        FakeProvider provider = new FakeProvider();
        provider.current = new CurrentWeather("London", "GB", 12.5, 11.0, 80, 4.1, "light rain");
        HistoryStore history = new HistoryStore(dir.resolve("history.json"));
        CliApp app = new CliApp(provider, history, out, FIXED);

        int rc = app.run(new String[] {"weather", "London"});

        assertEquals(0, rc);
        assertTrue(output().contains("Current weather - London, GB"), output());

        List<HistoryEntry> recorded = history.all();
        assertEquals(1, recorded.size());
        assertEquals("London", recorded.get(0).city());
        assertEquals(NOW, recorded.get(0).timestamp());
    }

    @Test
    void weather_joinsMultiWordCity(@TempDir Path dir) {
        FakeProvider provider = new FakeProvider();
        provider.current = new CurrentWeather("New York", "US", 20.0, 19.0, 50, 2.0, "clear sky");
        CliApp app = new CliApp(provider, new HistoryStore(dir.resolve("h.json")), out, FIXED);

        app.run(new String[] {"weather", "New", "York"});

        assertEquals("New York", provider.lastCity);
    }

    @Test
    void forecast_printsTable(@TempDir Path dir) {
        FakeProvider provider = new FakeProvider();
        provider.forecast = new Forecast("London", "GB", List.of(
                new DailyForecast(LocalDate.of(2026, 6, 17), 12.0, 18.0, "light rain")));
        CliApp app = new CliApp(provider, new HistoryStore(dir.resolve("h.json")), out, FIXED);

        int rc = app.run(new String[] {"forecast", "London"});

        assertEquals(0, rc);
        assertTrue(output().contains("5-day forecast - London, GB"), output());
        assertTrue(output().contains("2026-06-17"), output());
    }

    @Test
    void history_printsStoredEntries(@TempDir Path dir) {
        HistoryStore history = new HistoryStore(dir.resolve("history.json"));
        history.add(new HistoryEntry(NOW, "London", "GB", 12.5, "light rain"));
        CliApp app = new CliApp(new FakeProvider(), history, out, FIXED);

        int rc = app.run(new String[] {"history"});

        assertEquals(0, rc);
        assertTrue(output().contains("Search history (1 entry)"), output());
        assertTrue(output().contains("London, GB"), output());
    }

    @Test
    void history_whenEmpty_printsEmptyMessage(@TempDir Path dir) {
        CliApp app = new CliApp(new FakeProvider(), new HistoryStore(dir.resolve("h.json")), out, FIXED);

        int rc = app.run(new String[] {"history"});

        assertEquals(0, rc);
        assertTrue(output().contains("Search history is empty."), output());
    }

    @Test
    void unknownCommand_returnsOneAndShowsUsage(@TempDir Path dir) {
        CliApp app = new CliApp(new FakeProvider(), new HistoryStore(dir.resolve("h.json")), out, FIXED);

        int rc = app.run(new String[] {"bogus"});

        assertEquals(1, rc);
        assertTrue(output().contains("Unknown command: bogus"), output());
        assertTrue(output().contains("Usage:"), output());
    }

    @Test
    void weather_withoutCity_returnsOne(@TempDir Path dir) {
        CliApp app = new CliApp(new FakeProvider(), new HistoryStore(dir.resolve("h.json")), out, FIXED);

        int rc = app.run(new String[] {"weather"});

        assertEquals(1, rc);
        assertTrue(output().contains("Usage: weather <city>"), output());
    }

    @Test
    void weather_cityNotFound_returnsTwoWithFriendlyMessage(@TempDir Path dir) {
        FakeProvider provider = new FakeProvider();
        provider.toThrow = new CityNotFoundException("Nowhereville");
        CliApp app = new CliApp(provider, new HistoryStore(dir.resolve("h.json")), out, FIXED);

        int rc = app.run(new String[] {"weather", "Nowhereville"});

        assertEquals(2, rc);
        assertTrue(output().contains("no such city"), output());
        assertTrue(output().contains("Nowhereville"), output());
    }

    @Test
    void noArguments_printsUsageAndReturnsOne(@TempDir Path dir) {
        CliApp app = new CliApp(new FakeProvider(), new HistoryStore(dir.resolve("h.json")), out, FIXED);

        int rc = app.run(new String[] {});

        assertEquals(1, rc);
        assertTrue(output().contains("Usage:"), output());
    }

    // ---- test double -------------------------------------------------------

    /** A configurable in-memory {@link WeatherProvider}; never touches the network. */
    private static final class FakeProvider implements WeatherProvider {
        CurrentWeather current;
        Forecast forecast;
        RuntimeException toThrow;
        String lastCity;

        @Override
        public CurrentWeather currentWeather(String city) {
            lastCity = city;
            if (toThrow != null) {
                throw toThrow;
            }
            return current;
        }

        @Override
        public Forecast forecast(String city) {
            lastCity = city;
            if (toThrow != null) {
                throw toThrow;
            }
            return forecast;
        }
    }
}
