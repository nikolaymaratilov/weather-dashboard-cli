package com.example.weather.api;

/** Thrown when OpenWeatherMap reports the requested city does not exist (HTTP 404). */
public class CityNotFoundException extends WeatherApiException {

    private final String city;

    public CityNotFoundException(String city) {
        super("city not found: " + city);
        this.city = city;
    }

    public String city() {
        return city;
    }
}
