package com.example.weather.cli;

import com.example.weather.history.HistoryEntry;
import com.example.weather.model.CurrentWeather;
import com.example.weather.model.DailyForecast;
import com.example.weather.model.Forecast;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Renders model objects as readable, aligned terminal text.
 *
 * <p>Pure functions (model in, {@code String} out) — easy to unit-test, and no
 * I/O. Output is ASCII-only and uses {@link Locale#ROOT} number formatting so
 * it looks identical on every machine and terminal.
 */
public final class WeatherFormatter {

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT).withZone(ZoneOffset.UTC);

    private WeatherFormatter() {
    }

    public static String formatCurrent(CurrentWeather w) {
        return String.format(Locale.ROOT,
                "Current weather - %s, %s%n"
                        + "  Condition:    %s%n"
                        + "  Temperature:  %.1f C  (feels like %.1f C)%n"
                        + "  Humidity:     %d%%%n"
                        + "  Wind:         %.1f m/s",
                w.city(), w.country(), w.description(),
                w.temperatureC(), w.feelsLikeC(), w.humidityPercent(), w.windSpeedMs());
    }

    public static String formatForecast(Forecast f) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "5-day forecast - %s, %s%n", f.city(), f.country()));
        sb.append(String.format(Locale.ROOT, "  %-11s %7s %7s   %s%n",
                "Date", "Min(C)", "Max(C)", "Condition"));
        for (DailyForecast d : f.days()) {
            sb.append(String.format(Locale.ROOT, "  %-11s %7.1f %7.1f   %s%n",
                    DAY.format(d.date()), d.minTempC(), d.maxTempC(), d.description()));
        }
        return sb.toString().stripTrailing();
    }

    public static String formatHistory(List<HistoryEntry> entries) {
        if (entries.isEmpty()) {
            return "Search history is empty.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Search history (%d %s)%n",
                entries.size(), entries.size() == 1 ? "entry" : "entries"));
        sb.append(String.format(Locale.ROOT, "  %-16s  %-18s %8s   %s%n",
                "When (UTC)", "City", "Temp(C)", "Condition"));
        for (HistoryEntry e : entries) {
            sb.append(String.format(Locale.ROOT, "  %-16s  %-18s %8.1f   %s%n",
                    STAMP.format(e.timestamp()), e.city() + ", " + e.country(),
                    e.temperatureC(), e.description()));
        }
        return sb.toString().stripTrailing();
    }
}
