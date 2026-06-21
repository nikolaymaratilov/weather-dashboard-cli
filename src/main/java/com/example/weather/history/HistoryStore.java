package com.example.weather.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores search history as a JSON array in a single file.
 *
 * <p>Each {@link #add} reads the current list, appends the new entry, and
 * rewrites the file. That is more than enough for a single-user CLI and keeps
 * the on-disk format a plain, human-readable JSON array.
 */
public class HistoryStore {

    private static final Type LIST_TYPE = new TypeToken<List<HistoryEntry>>() {
    }.getType();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .setPrettyPrinting()
            .create();

    private final Path file;

    public HistoryStore(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    /** Returns all stored entries in insertion order; empty if there is no history yet. */
    public List<HistoryEntry> all() {
        if (!Files.exists(file)) {
            return List.of();
        }
        String json;
        try {
            json = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new HistoryStoreException("could not read history file: " + file, e);
        }
        if (json.isBlank()) {
            return List.of();
        }
        try {
            List<HistoryEntry> entries = GSON.fromJson(json, LIST_TYPE);
            return entries == null ? List.of() : List.copyOf(entries);
        } catch (RuntimeException e) { // JsonSyntaxException and friends
            throw new HistoryStoreException("history file is corrupt: " + file, e);
        }
    }

    /** Appends one entry to the history file, creating the file (and parents) if needed. */
    public void add(HistoryEntry entry) {
        Objects.requireNonNull(entry, "entry");
        List<HistoryEntry> entries = new ArrayList<>(all());
        entries.add(entry);
        write(entries);
    }

    private void write(List<HistoryEntry> entries) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    file,
                    GSON.toJson(entries, LIST_TYPE),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new HistoryStoreException("could not write history file: " + file, e);
        }
    }

    /** Reads/writes {@link Instant} as an ISO-8601 string so the JSON stays readable. */
    private static final class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return Instant.parse(in.nextString());
        }
    }
}
