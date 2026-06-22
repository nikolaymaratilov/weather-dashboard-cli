# Weather Dashboard CLI

A small Java terminal application that fetches **current weather** and a **5-day forecast** for
any city from the [OpenWeatherMap](https://openweathermap.org/) free API, prints them as readable
terminal output, and appends every lookup to a local **JSON search history** file.

It also ships with a fully **offline `demo`** (bundled sample data, no API key, no network) so the
app can be run and graded without any setup.

## Functional requirements
1. Current weather lookup for a city (temperature, conditions, humidity, wind).
2. 5-day forecast (daily min/max temperature + dominant condition), aggregated from the free API's
   3-hourly data.
3. Search history appended to `history.json`, viewable with a `history` command.
4. Readable, aligned terminal output with clear headers.
5. Graceful errors: city not found, no network, missing/invalid API key.
6. API key read from an environment variable — never committed.

## Modules
| Module | Purpose |
|--------|---------|
| `model` | Immutable data types for weather (`CurrentWeather`, `Forecast`, `DailyForecast`). |
| `parse` | OpenWeatherMap JSON → models; aggregates 3-hourly forecast into up to 5 daily summaries. |
| `history` | Appends/reads search history to a JSON file (`HistoryStore`). |
| `api` | Fetches live data over HTTP; network isolated behind `HttpFetcher` / `WeatherProvider`. |
| `cli` | Parses commands, renders aligned output, runs the offline `demo`, wires it all together. |

## Tech stack
- **Java 17+** (developed on JDK 22; compiled to release 17 for portability)
- **Maven 3.9+** build
- **Gson 2.10.1** for JSON
- Built-in `java.net.http.HttpClient` for HTTP
- **JUnit 5** for tests (38 tests, all offline/deterministic)

## Run from a clean clone
Prerequisites: a **JDK 17 or newer** and **Maven 3.9+** on your `PATH` (`java -version`, `mvn -version`).

```bash
git clone https://github.com/nikolaymaratilov/weather-dashboard-cli
cd weather-dashboard-cli

# 1. Run the test suite (38 tests, fully offline)
mvn test

# 2. Build the runnable jar -> target/weather-dashboard-cli.jar
mvn package

# 3. Offline demo — no API key, no network needed
java -jar target/weather-dashboard-cli.jar demo
```

### Live usage (optional — needs a free API key)
Get a free key at <https://openweathermap.org/> (new keys can take a little while to activate),
then set it as an environment variable:

```powershell
# Windows PowerShell
$env:OPENWEATHER_API_KEY = "your_key_here"
java -jar target/weather-dashboard-cli.jar weather "New York"
java -jar target/weather-dashboard-cli.jar forecast London
java -jar target/weather-dashboard-cli.jar history
```

```bash
# macOS / Linux
export OPENWEATHER_API_KEY="your_key_here"
java -jar target/weather-dashboard-cli.jar weather "New York"
```

## Sample run
`java -jar target/weather-dashboard-cli.jar demo` prints (deterministic sample data):

```
=== Weather Dashboard CLI - offline demo (sample data) ===

Current weather - London, GB
  Condition:    light rain
  Temperature:  12.5 C  (feels like 11.0 C)
  Humidity:     80%
  Wind:         4.1 m/s

5-day forecast - London, GB
  Date         Min(C)  Max(C)   Condition
  2026-06-17     12.0    18.0   light rain
  2026-06-18     15.0    23.0   clear sky
  2026-06-19     16.0    25.0   few clouds
  2026-06-20     14.0    22.0   scattered clouds
  2026-06-21     13.0    19.0   light rain

Search history (3 entries)
  When (UTC)        City                Temp(C)   Condition
  2026-06-17 08:00  London, GB             12.5   light rain
  2026-06-17 08:05  Paris, FR              19.0   clear sky
  2026-06-17 08:10  Tokyo, JP              26.0   few clouds
```

## Repository
- GitHub: https://github.com/nikolaymaratilov/weather-dashboard-cli
