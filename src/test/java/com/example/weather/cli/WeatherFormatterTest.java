package com.example.weather.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.weather.history.HistoryEntry;
import com.example.weather.model.CurrentWeather;
import com.example.weather.model.DailyForecast;
import com.example.weather.model.Forecast;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for the pure formatting functions. */
class WeatherFormatterTest {

    @Test
    void formatCurrent_includesAllFields() {
        CurrentWeather cw = new CurrentWeather("London", "GB", 12.5, 11.0, 80, 4.1, "light rain");
        String s = WeatherFormatter.formatCurrent(cw);

        assertTrue(s.contains("Current weather - London, GB"), s);
        assertTrue(s.contains("light rain"), s);
        assertTrue(s.contains("12.5"), s);
        assertTrue(s.contains("80%"), s);
        assertTrue(s.contains("4.1"), s);
    }

    @Test
    void formatForecast_hasHeaderAndOneRowPerDay() {
        Forecast f = new Forecast("London", "GB", List.of(
                new DailyForecast(LocalDate.of(2026, 6, 17), 12.0, 18.0, "light rain"),
                new DailyForecast(LocalDate.of(2026, 6, 18), 16.0, 24.0, "clear sky")));
        String s = WeatherFormatter.formatForecast(f);

        assertTrue(s.contains("5-day forecast - London, GB"), s);
        assertTrue(s.contains("Min(C)"), s);
        assertTrue(s.contains("Max(C)"), s);
        assertTrue(s.contains("2026-06-17"), s);
        assertTrue(s.contains("clear sky"), s);
    }

    @Test
    void formatHistory_listsEntries() {
        List<HistoryEntry> entries = List.of(
                new HistoryEntry(Instant.parse("2026-06-17T10:15:30Z"), "London", "GB", 12.5, "light rain"),
                new HistoryEntry(Instant.parse("2026-06-17T11:20:00Z"), "Paris", "FR", 19.0, "clear sky"));
        String s = WeatherFormatter.formatHistory(entries);

        assertTrue(s.contains("Search history (2 entries)"), s);
        assertTrue(s.contains("London, GB"), s);
        assertTrue(s.contains("2026-06-17 10:15"), s); // formatted in UTC
        assertTrue(s.contains("Paris, FR"), s);
    }

    @Test
    void formatHistory_emptyMessage() {
        assertEquals("Search history is empty.", WeatherFormatter.formatHistory(List.of()));
    }
}
