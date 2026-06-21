package com.example.weather.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for the immutable model records. Deterministic and offline. */
class ModelTest {

    // ---- CurrentWeather ----------------------------------------------------

    @Test
    void currentWeather_holdsItsValues() {
        CurrentWeather cw = new CurrentWeather(
                "London", "GB", 12.5, 11.0, 80, 4.1, "light rain");

        assertEquals("London", cw.city());
        assertEquals("GB", cw.country());
        assertEquals(12.5, cw.temperatureC());
        assertEquals(80, cw.humidityPercent());
        assertEquals("light rain", cw.description());
    }

    @Test
    void currentWeather_rejectsHumidityOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
                new CurrentWeather("London", "GB", 12.5, 11.0, 150, 4.1, "rain"));
    }

    @Test
    void currentWeather_rejectsNullCity() {
        assertThrows(NullPointerException.class, () ->
                new CurrentWeather(null, "GB", 12.5, 11.0, 80, 4.1, "rain"));
    }

    // ---- DailyForecast -----------------------------------------------------

    @Test
    void dailyForecast_holdsItsValues() {
        DailyForecast d = new DailyForecast(LocalDate.of(2026, 6, 17), 14.0, 22.5, "clear sky");

        assertEquals(LocalDate.of(2026, 6, 17), d.date());
        assertEquals(14.0, d.minTempC());
        assertEquals(22.5, d.maxTempC());
        assertEquals("clear sky", d.description());
    }

    @Test
    void dailyForecast_rejectsMinAboveMax() {
        assertThrows(IllegalArgumentException.class, () ->
                new DailyForecast(LocalDate.of(2026, 6, 17), 30.0, 10.0, "clear sky"));
    }

    // ---- Forecast ----------------------------------------------------------

    @Test
    void forecast_copiesDaysDefensivelyAndIsUnmodifiable() {
        List<DailyForecast> source = new ArrayList<>();
        source.add(new DailyForecast(LocalDate.of(2026, 6, 17), 14.0, 22.5, "clear sky"));

        Forecast forecast = new Forecast("London", "GB", source);

        // Mutating the original list must not affect the record.
        source.clear();
        assertEquals(1, forecast.days().size());

        // The stored list itself is unmodifiable.
        assertThrows(UnsupportedOperationException.class, () ->
                forecast.days().add(
                        new DailyForecast(LocalDate.of(2026, 6, 18), 15.0, 20.0, "rain")));
    }

    @Test
    void forecast_rejectsNullDays() {
        assertThrows(NullPointerException.class, () ->
                new Forecast("London", "GB", null));
    }
}
