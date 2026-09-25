package com.complaintsystem.model;

/**
 * Represents the lifecycle status of a complaint in the system.
 */
public enum Status {
    OPEN("Open / Pending Triage"),
    IN_PROGRESS("In Progress / Under Investigation"),
    ESCALATED("Escalated to Senior Admin"),
    RESOLVED("Resolved / Solution Provided"),
    CLOSED("Closed / Finalized");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines whether the complaint is still active and unresolved.
     * Only unresolved complaints are subject to SLA tracking and escalation.
     *
     * @return true if OPEN or IN_PROGRESS; false if ESCALATED, RESOLVED, or CLOSED.
     */
    public boolean isUnresolved() {
        return this == OPEN || this == IN_PROGRESS;
    }
}
