package com.example.weather.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.weather.model.CurrentWeather;
import com.example.weather.model.DailyForecast;
import com.example.weather.model.Forecast;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Offline tests for {@link WeatherParser}, driven by committed JSON fixtures. */
class WeatherParserTest {

    @Test
    void parseCurrent_readsAllFields() throws IOException {
        CurrentWeather cw = WeatherParser.parseCurrent(fixture("current_london.json"));

        assertEquals("London", cw.city());
        assertEquals("GB", cw.country());
        assertEquals(12.5, cw.temperatureC());
        assertEquals(11.0, cw.feelsLikeC());
        assertEquals(80, cw.humidityPercent());
        assertEquals(4.1, cw.windSpeedMs());
        assertEquals("light rain", cw.description());
    }

    @Test
    void parseForecast_aggregatesThreeHourlySlotsIntoDays() throws IOException {
        Forecast forecast = WeatherParser.parseForecast(fixture("forecast_london.json"));

        assertEquals("London", forecast.city());
        assertEquals("GB", forecast.country());
        assertEquals(2, forecast.days().size());

        // Day 1: three slots -> min of temp_min, max of temp_max, dominant description.
        DailyForecast day1 = forecast.days().get(0);
        assertEquals(LocalDate.of(2026, 6, 17), day1.date());
        assertEquals(12.0, day1.minTempC());
        assertEquals(18.0, day1.maxTempC());
        assertEquals("light rain", day1.description()); // 2x rain beats 1x clouds

        // Day 2: two slots, both clear.
        DailyForecast day2 = forecast.days().get(1);
        assertEquals(LocalDate.of(2026, 6, 18), day2.date());
        assertEquals(16.0, day2.minTempC());
        assertEquals(24.0, day2.maxTempC());
        assertEquals("clear sky", day2.description());
    }

    @Test
    void parseCurrent_throwsOnApiErrorBody() throws IOException {
        WeatherParseException ex = assertThrows(WeatherParseException.class, () ->
                WeatherParser.parseCurrent(fixture("city_not_found.json")));
        assertTrue(ex.getMessage().contains("city not found"),
                "message should surface the API error, was: " + ex.getMessage());
    }

    @Test
    void parseCurrent_throwsOnMalformedJson() {
        assertThrows(WeatherParseException.class, () ->
                WeatherParser.parseCurrent("not valid json {"));
    }

    // ---- helper ------------------------------------------------------------

    private static String fixture(String name) throws IOException {
        try (InputStream in = WeatherParserTest.class.getResourceAsStream("/fixtures/" + name)) {
            assertNotNull(in, "missing test fixture: " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
