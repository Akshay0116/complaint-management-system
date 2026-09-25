package com.complaintsystem.model;

/**
 * Represents the urgency/priority level of a complaint.
 * Each priority defines its own SLA escalation threshold in days.
 */
public enum Priority {
    LOW(10),
    MEDIUM(5),
    HIGH(2);

    private final int escalationDays;

    Priority(int escalationDays) {
        this.escalationDays = escalationDays;
    }

    /**
     * @return Number of unresolved days allowed before auto-escalation triggers.
     */
    public int getEscalationDays() {
        return escalationDays;
    }
}
