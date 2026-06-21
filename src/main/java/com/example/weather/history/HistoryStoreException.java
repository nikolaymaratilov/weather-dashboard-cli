package com.example.weather.history;

/** Thrown when the history file cannot be read or written, or is corrupt. */
public class HistoryStoreException extends RuntimeException {

    public HistoryStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
