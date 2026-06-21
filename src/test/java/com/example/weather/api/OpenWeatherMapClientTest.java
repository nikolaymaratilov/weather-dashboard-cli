package com.example.weather.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.weather.model.CurrentWeather;
import com.example.weather.model.Forecast;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Offline tests for {@link OpenWeatherMapClient}: a fake {@link HttpFetcher}
 * returns committed fixture bodies, so no real network is involved.
 */
class OpenWeatherMapClientTest {

    @Test
    void currentWeather_parsesResponseAndBuildsExpectedUrl() {
        FakeHttpFetcher fake = new FakeHttpFetcher(200, fixture("current_london.json"));
        OpenWeatherMapClient client = new OpenWeatherMapClient(fake, "TESTKEY", "metric");

        CurrentWeather cw = client.currentWeather("London");

        assertEquals("London", cw.city());
        assertEquals(12.5, cw.temperatureC());

        assertTrue(fake.lastUrl.contains("/weather"), fake.lastUrl);
        assertTrue(fake.lastUrl.contains("q=London"), fake.lastUrl);
        assertTrue(fake.lastUrl.contains("units=metric"), fake.lastUrl);
        assertTrue(fake.lastUrl.contains("appid=TESTKEY"), fake.lastUrl);
    }

    @Test
    void forecast_parsesResponseAndUsesForecastEndpoint() {
        FakeHttpFetcher fake = new FakeHttpFetcher(200, fixture("forecast_london.json"));
        OpenWeatherMapClient client = new OpenWeatherMapClient(fake, "TESTKEY", "metric");

        Forecast forecast = client.forecast("London");

        assertEquals(2, forecast.days().size());
        assertTrue(fake.lastUrl.contains("/forecast"), fake.lastUrl);
    }

    @Test
    void notFound_throwsCityNotFound() {
        FakeHttpFetcher fake = new FakeHttpFetcher(404, fixture("city_not_found.json"));
        OpenWeatherMapClient client = new OpenWeatherMapClient(fake, "TESTKEY", "metric");

        CityNotFoundException ex =
                assertThrows(CityNotFoundException.class, () -> client.currentWeather("Nowhereville"));
        assertTrue(ex.getMessage().contains("Nowhereville"), ex.getMessage());
    }

    @Test
    void unauthorized_throwsApiErrorMentioningKey() {
        FakeHttpFetcher fake = new FakeHttpFetcher(401, "{\"cod\":401,\"message\":\"Invalid API key\"}");
        OpenWeatherMapClient client = new OpenWeatherMapClient(fake, "TESTKEY", "metric");

        WeatherApiException ex =
                assertThrows(WeatherApiException.class, () -> client.currentWeather("London"));
        assertTrue(ex.getMessage().toLowerCase().contains("key"), ex.getMessage());
    }

    @Test
    void cityNameWithSpace_isUrlEncoded() {
        FakeHttpFetcher fake = new FakeHttpFetcher(200, fixture("current_london.json"));
        OpenWeatherMapClient client = new OpenWeatherMapClient(fake, "TESTKEY", "metric");

        client.currentWeather("New York");

        assertTrue(fake.lastUrl.contains("q=New+York"), fake.lastUrl);
    }

    @Test
    void constructor_rejectsBlankApiKey() {
        FakeHttpFetcher fake = new FakeHttpFetcher(200, "");
        assertThrows(IllegalArgumentException.class,
                () -> new OpenWeatherMapClient(fake, "   ", "metric"));
    }

    // ---- test helpers ------------------------------------------------------

    /** Records the requested URL and returns a preset status + body. No network. */
    private static final class FakeHttpFetcher implements HttpFetcher {
        private final int status;
        private final String body;
        private String lastUrl;

        FakeHttpFetcher(int status, String body) {
            this.status = status;
            this.body = body;
        }

        @Override
        public Response get(String url) {
            this.lastUrl = url;
            return new Response(status, body);
        }
    }

    private static String fixture(String name) {
        try (InputStream in = OpenWeatherMapClientTest.class.getResourceAsStream("/fixtures/" + name)) {
            assertNotNull(in, "missing test fixture: " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
