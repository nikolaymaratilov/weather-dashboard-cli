package com.example.weather.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.weather.model.CurrentWeather;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Offline tests for {@link HistoryStore}, using a JUnit temp directory for real disk I/O. */
class HistoryStoreTest {

    private static final Instant T1 = Instant.parse("2026-06-17T10:15:30Z");
    private static final Instant T2 = Instant.parse("2026-06-17T11:20:00Z");

    @Test
    void addThenAll_roundTripsInOrder(@TempDir Path dir) {
        HistoryStore store = new HistoryStore(dir.resolve("history.json"));

        store.add(new HistoryEntry(T1, "London", "GB", 12.5, "light rain"));
        store.add(new HistoryEntry(T2, "Paris", "FR", 19.0, "clear sky"));

        List<HistoryEntry> all = store.all();
        assertEquals(2, all.size());
        assertEquals("London", all.get(0).city());
        assertEquals(T1, all.get(0).timestamp());
        assertEquals("Paris", all.get(1).city());
        assertEquals(19.0, all.get(1).temperatureC());
    }

    @Test
    void all_onMissingFile_returnsEmpty(@TempDir Path dir) {
        HistoryStore store = new HistoryStore(dir.resolve("does-not-exist.json"));
        assertTrue(store.all().isEmpty());
    }

    @Test
    void all_onEmptyFile_returnsEmpty(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("history.json");
        Files.writeString(file, "   ", StandardCharsets.UTF_8);

        HistoryStore store = new HistoryStore(file);
        assertTrue(store.all().isEmpty());
    }

    @Test
    void all_onCorruptFile_throws(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("history.json");
        Files.writeString(file, "{ this is not valid json", StandardCharsets.UTF_8);

        HistoryStore store = new HistoryStore(file);
        assertThrows(HistoryStoreException.class, store::all);
    }

    @Test
    void add_createsParentDirectories(@TempDir Path dir) {
        Path nested = dir.resolve("nested").resolve("sub").resolve("history.json");
        HistoryStore store = new HistoryStore(nested);

        store.add(new HistoryEntry(T1, "London", "GB", 12.5, "light rain"));

        assertTrue(Files.exists(nested));
        assertEquals(1, store.all().size());
    }

    @Test
    void data_persistsAcrossStoreInstances(@TempDir Path dir) {
        Path file = dir.resolve("history.json");
        new HistoryStore(file).add(new HistoryEntry(T1, "London", "GB", 12.5, "light rain"));

        // A fresh instance reads what the previous one wrote — proves real persistence.
        List<HistoryEntry> all = new HistoryStore(file).all();
        assertEquals(1, all.size());
        assertEquals("London", all.get(0).city());
    }

    @Test
    void historyEntry_ofCurrentWeather_copiesFields() {
        CurrentWeather cw = new CurrentWeather("Berlin", "DE", 21.0, 20.0, 55, 3.2, "few clouds");
        HistoryEntry entry = HistoryEntry.of(T1, cw);

        assertEquals(T1, entry.timestamp());
        assertEquals("Berlin", entry.city());
        assertEquals("DE", entry.country());
        assertEquals(21.0, entry.temperatureC());
        assertEquals("few clouds", entry.description());
    }
}
