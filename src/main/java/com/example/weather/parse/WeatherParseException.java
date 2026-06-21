package com.example.weather.parse;

/**
 * Thrown when an OpenWeatherMap response cannot be turned into a model —
 * because it is malformed JSON, is missing expected fields, or is an API
 * error body (e.g. "city not found").
 */
public class WeatherParseException extends RuntimeException {

    public WeatherParseException(String message) {
        super(message);
    }

    public WeatherParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
