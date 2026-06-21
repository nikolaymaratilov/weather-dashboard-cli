package com.example.weather;

import com.example.weather.api.JdkHttpFetcher;
import com.example.weather.api.OpenWeatherMapClient;
import com.example.weather.api.WeatherProvider;
import com.example.weather.cli.CliApp;
import com.example.weather.cli.DemoRunner;
import com.example.weather.history.HistoryStore;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Application entry point: wires the real OpenWeatherMap client, history store,
 * and CLI together, and routes the offline {@code demo} command.
 */
public class Main {

    private static final String UNITS = "metric";
    private static final String API_KEY_ENV = "OPENWEATHER_API_KEY";
    private static final Path HISTORY_FILE = Path.of("history.json");

    public static void main(String[] args) {
        System.exit(dispatch(args, System.out));
    }

    /** Testable entry: chooses the offline demo or wires the live app. */
    static int dispatch(String[] args, PrintStream out) {
        if (args.length > 0 && args[0].equals("demo")) {
            return DemoRunner.run(out);
        }
        if (args.length == 0) {
            CliApp.printUsage(out);
            return 1;
        }

        boolean needsNetwork = args[0].equals("weather") || args[0].equals("forecast");
        String apiKey = System.getenv(API_KEY_ENV);
        if (needsNetwork && (apiKey == null || apiKey.isBlank())) {
            out.println("Error: " + API_KEY_ENV + " is not set. "
                    + "Set it to use live commands, or run 'demo' for an offline sample.");
            return 1;
        }

        WeatherProvider provider = needsNetwork
                ? new OpenWeatherMapClient(new JdkHttpFetcher(), apiKey, UNITS)
                : null;
        HistoryStore history = new HistoryStore(HISTORY_FILE);
        CliApp app = new CliApp(provider, history, out, Clock.systemUTC());
        return app.run(args);
    }
}
