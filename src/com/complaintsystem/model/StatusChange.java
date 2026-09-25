package com.complaintsystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Supporting helper class representing an immutable status transition record
 * in the append-only audit trail of a Complaint.
 */
public class StatusChange {
    private final Status status;
    private final LocalDateTime timestamp;
    private final String note;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatusChange(Status status, LocalDateTime timestamp, String note) {
        this.status = status;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
        this.note = (note != null && !note.trim().isEmpty()) ? note.trim() : "No note provided";
    }

    public StatusChange(Status status, String note) {
        this(status, LocalDateTime.now(), note);
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return String.format("[%s] Status: %-12s | Note: %s",
                timestamp.format(FORMATTER), status, note);
    }
}
