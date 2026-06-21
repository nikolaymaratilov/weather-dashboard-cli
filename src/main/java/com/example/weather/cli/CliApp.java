package com.example.weather.cli;

import com.example.weather.api.CityNotFoundException;
import com.example.weather.api.WeatherApiException;
import com.example.weather.api.WeatherProvider;
import com.example.weather.history.HistoryEntry;
import com.example.weather.history.HistoryStore;
import com.example.weather.history.HistoryStoreException;
import com.example.weather.model.CurrentWeather;
import com.example.weather.model.Forecast;
import java.io.PrintStream;
import java.time.Clock;
import java.util.Arrays;
import java.util.Objects;

/**
 * Parses the command line and runs the weather / forecast / history commands.
 *
 * <p>Dependencies (the weather source, history store, output stream, clock) are
 * injected, so the whole command surface can be tested with a fake provider, a
 * temp-file store, and a captured {@link PrintStream}.
 */
public class CliApp {

    private final WeatherProvider provider;
    private final HistoryStore history;
    private final PrintStream out;
    private final Clock clock;

    public CliApp(WeatherProvider provider, HistoryStore history, PrintStream out, Clock clock) {
        this.provider = provider; // may be null for commands that don't need the network
        this.history = Objects.requireNonNull(history, "history");
        this.out = Objects.requireNonNull(out, "out");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Runs one command and returns a process exit code (0 ok, 1 usage error, 2 runtime error). */
    public int run(String[] args) {
        if (args.length == 0) {
            printUsage(out);
            return 1;
        }
        try {
            return switch (args[0]) {
                case "weather" -> showWeather(args);
                case "forecast" -> showForecast(args);
                case "history" -> showHistory();
                default -> {
                    out.println("Unknown command: " + args[0]);
                    printUsage(out);
                    yield 1;
                }
            };
        } catch (CityNotFoundException e) {
            out.println("Error: no such city - \"" + e.city() + "\".");
            return 2;
        } catch (WeatherApiException | HistoryStoreException e) {
            out.println("Error: " + e.getMessage());
            return 2;
        }
    }

    private int showWeather(String[] args) {
        String city = cityArg(args);
        if (city == null) {
            return 1;
        }
        CurrentWeather cw = provider.currentWeather(city);
        out.println(WeatherFormatter.formatCurrent(cw));
        history.add(HistoryEntry.of(clock.instant(), cw));
        return 0;
    }

    private int showForecast(String[] args) {
        String city = cityArg(args);
        if (city == null) {
            return 1;
        }
        Forecast forecast = provider.forecast(city);
        out.println(WeatherFormatter.formatForecast(forecast));
        return 0;
    }

    private int showHistory() {
        out.println(WeatherFormatter.formatHistory(history.all()));
        return 0;
    }

    /** Joins the args after the command so "weather New York" works without quotes. */
    private String cityArg(String[] args) {
        if (args.length < 2) {
            out.println("Usage: " + args[0] + " <city>");
            return null;
        }
        String city = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (city.isEmpty()) {
            out.println("Usage: " + args[0] + " <city>");
            return null;
        }
        return city;
    }

    public static void printUsage(PrintStream out) {
        out.println("""
                Weather Dashboard CLI
                Usage:
                  weather <city>     Show current weather and record the search
                  forecast <city>    Show the 5-day forecast
                  history            Show past searches
                  demo               Run an offline demo from bundled sample data""");
    }
}
