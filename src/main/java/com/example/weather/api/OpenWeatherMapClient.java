package com.example.weather.api;

import com.example.weather.model.CurrentWeather;
import com.example.weather.model.Forecast;
import com.example.weather.parse.WeatherParser;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * {@link WeatherProvider} backed by the OpenWeatherMap free API.
 *
 * <p>Builds the request URL, delegates the actual GET to an injected
 * {@link HttpFetcher}, maps the HTTP status to a clear outcome, and hands the
 * body to {@link WeatherParser}.
 */
public class OpenWeatherMapClient implements WeatherProvider {

    private static final String DEFAULT_BASE_URL = "https://api.openweathermap.org/data/2.5";

    private final HttpFetcher fetcher;
    private final String apiKey;
    private final String units;
    private final String baseUrl;

    public OpenWeatherMapClient(HttpFetcher fetcher, String apiKey, String units) {
        this(fetcher, apiKey, units, DEFAULT_BASE_URL);
    }

    /** Full constructor; {@code baseUrl} is overridable so tests can point elsewhere. */
    public OpenWeatherMapClient(HttpFetcher fetcher, String apiKey, String units, String baseUrl) {
        this.fetcher = Objects.requireNonNull(fetcher, "fetcher");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        this.apiKey = apiKey;
        this.units = Objects.requireNonNull(units, "units");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
    }

    @Override
    public CurrentWeather currentWeather(String city) {
        return WeatherParser.parseCurrent(getBody("/weather", city));
    }

    @Override
    public Forecast forecast(String city) {
        return WeatherParser.parseForecast(getBody("/forecast", city));
    }

    private String getBody(String path, String city) {
        Objects.requireNonNull(city, "city");
        String url = baseUrl + path
                + "?q=" + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&units=" + units
                + "&appid=" + apiKey;

        HttpFetcher.Response response;
        try {
            response = fetcher.get(url);
        } catch (IOException e) {
            throw new WeatherApiException("network error contacting OpenWeatherMap: " + e.getMessage(), e);
        }

        return switch (response.statusCode()) {
            case 200 -> response.body();
            case 404 -> throw new CityNotFoundException(city);
            case 401 -> throw new WeatherApiException(
                    "OpenWeatherMap rejected the API key (HTTP 401). Check your OPENWEATHER_API_KEY.");
            default -> throw new WeatherApiException(
                    "OpenWeatherMap returned HTTP " + response.statusCode() + " for city '" + city + "'");
        };
    }
}
