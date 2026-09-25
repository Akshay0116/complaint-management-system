package com.complaintsystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Backward-compatible status history class extending {@link StatusChange}.
 * Preserves compatibility for existing references and IDE project navigation.
 */
public class StatusHistoryEntry extends StatusChange {

    public StatusHistoryEntry(Status oldStatus, Status newStatus, String updatedBy, LocalDate timestamp, String remarks) {
        super(newStatus, (timestamp != null ? timestamp.atTime(12, 0) : LocalDateTime.now()), 
              (remarks != null ? remarks : "") + (updatedBy != null ? " [By: " + updatedBy + "]" : ""));
    }

    public StatusHistoryEntry(Status status, LocalDateTime timestamp, String note) {
        super(status, timestamp, note);
    }
}
