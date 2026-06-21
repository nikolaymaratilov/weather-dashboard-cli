package com.example.weather.parse;

import com.example.weather.model.CurrentWeather;
import com.example.weather.model.DailyForecast;
import com.example.weather.model.Forecast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns OpenWeatherMap JSON into our domain models.
 *
 * <p>Handles the two free-plan endpoints:
 * <ul>
 *   <li>current weather ({@code /data/2.5/weather}) &rarr; {@link CurrentWeather}</li>
 *   <li>5-day / 3-hour forecast ({@code /data/2.5/forecast}) &rarr; {@link Forecast},
 *       with the 3-hourly slots aggregated into up to five daily summaries.</li>
 * </ul>
 */
public final class WeatherParser {

    /** Maximum number of days to keep from a forecast response. */
    private static final int MAX_DAYS = 5;

    private WeatherParser() {
    }

    /** Parses a current-weather response body into a {@link CurrentWeather}. */
    public static CurrentWeather parseCurrent(String json) {
        JsonObject root = toObject(json);
        requireField(root, "main");
        requireField(root, "weather");

        JsonObject main = root.getAsJsonObject("main");
        JsonObject wind = root.has("wind") ? root.getAsJsonObject("wind") : new JsonObject();
        JsonObject sys = root.has("sys") ? root.getAsJsonObject("sys") : new JsonObject();

        String city = getString(root, "name", "");
        String country = getString(sys, "country", "");
        double temp = getDouble(main, "temp");
        double feelsLike = main.has("feels_like") ? getDouble(main, "feels_like") : temp;
        int humidity = getInt(main, "humidity");
        double windSpeed = wind.has("speed") ? getDouble(wind, "speed") : 0.0;
        String description = firstWeatherDescription(root.getAsJsonArray("weather"));

        try {
            return new CurrentWeather(city, country, temp, feelsLike, humidity, windSpeed, description);
        } catch (RuntimeException e) {
            throw new WeatherParseException("invalid current-weather values: " + e.getMessage(), e);
        }
    }

    /** Parses a 5-day/3-hour forecast body and aggregates it into daily summaries. */
    public static Forecast parseForecast(String json) {
        JsonObject root = toObject(json);
        requireField(root, "list");

        JsonArray slots = root.getAsJsonArray("list");
        JsonObject cityObj = root.has("city") ? root.getAsJsonObject("city") : new JsonObject();
        String city = getString(cityObj, "name", "");
        String country = getString(cityObj, "country", "");

        // Group the 3-hourly slots by calendar day, preserving first-seen order.
        Map<LocalDate, DayAccumulator> byDay = new LinkedHashMap<>();
        for (var element : slots) {
            JsonObject slot = element.getAsJsonObject();
            JsonObject main = slot.getAsJsonObject("main");
            double tMin = main.has("temp_min") ? getDouble(main, "temp_min") : getDouble(main, "temp");
            double tMax = main.has("temp_max") ? getDouble(main, "temp_max") : getDouble(main, "temp");
            String desc = firstWeatherDescription(slot.getAsJsonArray("weather"));
            byDay.computeIfAbsent(dateOf(slot), d -> new DayAccumulator()).add(tMin, tMax, desc);
        }

        List<DailyForecast> days = new ArrayList<>();
        for (var entry : byDay.entrySet()) {
            if (days.size() == MAX_DAYS) {
                break;
            }
            DayAccumulator acc = entry.getValue();
            days.add(new DailyForecast(entry.getKey(), acc.min, acc.max, acc.dominantDescription()));
        }
        return new Forecast(city, country, days);
    }

    // ---- aggregation -------------------------------------------------------

    /** Collects the temperatures and conditions seen across one calendar day. */
    private static final class DayAccumulator {
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;
        private final Map<String, Integer> descriptionCounts = new LinkedHashMap<>();

        void add(double tMin, double tMax, String description) {
            min = Math.min(min, tMin);
            max = Math.max(max, tMax);
            descriptionCounts.merge(description, 1, Integer::sum);
        }

        /** Most frequent description that day; ties resolve to the first one seen. */
        String dominantDescription() {
            String best = "";
            int bestCount = -1;
            for (var e : descriptionCounts.entrySet()) {
                if (e.getValue() > bestCount) {
                    bestCount = e.getValue();
                    best = e.getKey();
                }
            }
            return best;
        }
    }

    // ---- JSON helpers ------------------------------------------------------

    private static JsonObject toObject(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException e) { // JsonSyntaxException / IllegalStateException
            throw new WeatherParseException("could not parse JSON: " + e.getMessage(), e);
        }
    }

    private static void requireField(JsonObject root, String field) {
        if (!root.has(field)) {
            // Surface the API's own message (e.g. "city not found") when present.
            String detail = root.has("message")
                    ? root.get("message").getAsString()
                    : "missing field '" + field + "'";
            throw new WeatherParseException("unexpected weather response: " + detail);
        }
    }

    private static LocalDate dateOf(JsonObject slot) {
        if (slot.has("dt_txt") && !slot.get("dt_txt").isJsonNull()) {
            // Format is "yyyy-MM-dd HH:mm:ss"; the date is the first 10 chars.
            return LocalDate.parse(slot.get("dt_txt").getAsString().substring(0, 10));
        }
        long dt = getLong(slot, "dt");
        return Instant.ofEpochSecond(dt).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static String firstWeatherDescription(JsonArray weather) {
        if (weather == null || weather.isEmpty()) {
            return "";
        }
        return getString(weather.get(0).getAsJsonObject(), "description", "");
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : fallback;
    }

    private static double getDouble(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            throw new WeatherParseException("missing numeric field '" + key + "'");
        }
        return obj.get(key).getAsDouble();
    }

    private static int getInt(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            throw new WeatherParseException("missing numeric field '" + key + "'");
        }
        return obj.get(key).getAsInt();
    }

    private static long getLong(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            throw new WeatherParseException("missing numeric field '" + key + "'");
        }
        return obj.get(key).getAsLong();
    }
}
