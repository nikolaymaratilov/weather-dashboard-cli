/**
 * Module: api — fetches live data from OpenWeatherMap over HTTP.
 *
 * <p>The network call sits behind small interfaces ({@code WeatherProvider},
 * {@code HttpFetcher}) so the rest of the app — and the tests — never touch
 * the real network.
 */
package com.example.weather.api;
