package com.example.weather.api;

import com.example.weather.model.CurrentWeather;
import com.example.weather.model.Forecast;

/**
 * Source of weather data for a city. The CLI depends on this interface, not on
 * the concrete OpenWeatherMap client, which keeps it easy to test and to run
 * the offline demo.
 */
public interface WeatherProvider {

    /** Current weather for the given city. */
    CurrentWeather currentWeather(String city);

    /** 5-day forecast for the given city. */
    Forecast forecast(String city);
}
