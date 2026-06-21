package com.example.weather.api;

/** Thrown when an OpenWeatherMap request fails (network error, bad key, rate limit, ...). */
public class WeatherApiException extends RuntimeException {

    public WeatherApiException(String message) {
        super(message);
    }

    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
