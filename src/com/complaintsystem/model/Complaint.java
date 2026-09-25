package com.complaintsystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a single complaint filed by a User. This is the central data
 * entity of the system.
 */
public class Complaint {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final String complaintId;
    private String type;
    private String description;
    private Priority priority;
    private LocalDate dateFiled;
    private Status status;
    private final User filedBy;
    private Admin assignedAdmin;
    private final List<StatusChange> statusHistory;
    private String resolutionNotes;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Primary constructor: Initializes complaint, sets status = OPEN, logs first history entry.
     * Auto-generates unique complaintId (e.g. CMP-0001).
     *
     * @param type        Category of complaint (e.g., "Billing", "Service", "Product Defect")
     * @param description Free-text details of the complaint
     * @param priority    LOW, MEDIUM, HIGH
     * @param filedBy     Reference to the user who filed it
     */
    public Complaint(String type, String description, Priority priority, User filedBy) {
        this(generateNextId(), type, description, priority, filedBy, LocalDate.now());
    }

    /**
     * Overloaded constructor supporting custom/historical filing dates and identifiers.
     */
    public Complaint(String complaintId, String type, String description, Priority priority, User filedBy, LocalDate dateFiled) {
        if (complaintId == null || complaintId.trim().isEmpty()) {
            this.complaintId = generateNextId();
        } else {
            this.complaintId = complaintId.trim();
        }

        if (filedBy == null) {
            throw new IllegalArgumentException("Complaint must be filed by a valid User.");
        }

        this.type = (type != null && !type.trim().isEmpty()) ? type.trim() : "General";
        this.description = (description != null && !description.trim().isEmpty()) ? description.trim() : "No description";
        this.priority = (priority != null) ? priority : Priority.MEDIUM;
        this.filedBy = filedBy;
        this.dateFiled = (dateFiled != null) ? dateFiled : LocalDate.now();
        this.status = Status.OPEN;
        this.assignedAdmin = null;
        this.statusHistory = new ArrayList<>();

        // Append-only history: first entry logged on creation
        LocalDateTime logTime = this.dateFiled.atTime(9, 0); // default filing morning timestamp
        this.statusHistory.add(new StatusChange(Status.OPEN, logTime, "Complaint filed by " + filedBy.getName()));
    }

    private static String generateNextId() {
        return String.format("CMP-%04d", ID_GENERATOR.incrementAndGet());
    }

    // ==========================================
    // SPECIFIED METHODS
    // ==========================================

    /**
     * Returns the current status of the complaint.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Compatibility alias for getStatus().
     */
    public Status getCurrentStatus() {
        return status;
    }

    /**
     * Changes status and appends entry to statusHistory (append-only).
     *
     * @param newStatus new lifecycle status
     * @param note      audit note explaining the change
     */
    public void updateStatus(Status newStatus, String note) {
        if (newStatus == null) return;
        this.status = newStatus;
        this.statusHistory.add(new StatusChange(newStatus, LocalDateTime.now(), note));
    }

    /**
     * Overloaded status update supporting admin assignment, custom timestamp, and return status.
     */
    public boolean updateStatus(Status newStatus, Admin admin, String note, LocalDate effectiveDate) {
        if (newStatus == null) return false;
        if (this.status == Status.CLOSED) {
            System.out.println("[Warning] Complaint " + complaintId + " is CLOSED and cannot be modified.");
            return false;
        }

        this.status = newStatus;
        if (admin != null) {
            setAssignedAdmin(admin);
        }

        if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
            this.resolutionNotes = (note != null) ? note : "";
        }

        LocalDateTime timestamp = (effectiveDate != null) 
                ? effectiveDate.atTime(12, 0) 
                : LocalDateTime.now();
        
        String author = (admin != null) ? " [By: " + admin.getName() + "]" : "";
        this.statusHistory.add(new StatusChange(newStatus, timestamp, (note != null ? note : "") + author));
        return true;
    }

    /**
     * Calculates days elapsed since the complaint was filed relative to today.
     * Used by escalation logic.
     */
    public int getDaysSinceFiled() {
        return getDaysSinceFiled(LocalDate.now());
    }

    /**
     * Calculates days elapsed since the complaint was filed relative to a reference date.
     */
    public int getDaysSinceFiled(LocalDate referenceDate) {
        LocalDate evalDate = (referenceDate != null) ? referenceDate : LocalDate.now();
        return (int) ChronoUnit.DAYS.between(dateFiled, evalDate);
    }

    /**
     * Alias for getDaysSinceFiled(referenceDate).
     */
    public int getDaysElapsed(LocalDate referenceDate) {
        return getDaysSinceFiled(referenceDate);
    }

    public String getResolutionNotes() {
        return (resolutionNotes != null) ? resolutionNotes : "";
    }

    /**
     * Returns the full, unmodifiable status audit trail.
     */
    public List<StatusChange> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    /**
     * Escalates complaint to SeniorAdmin and logs history record.
     */
    public void escalate(SeniorAdmin seniorAdmin, LocalDate escalationDate, String note) {
        this.status = Status.ESCALATED;
        setAssignedAdmin(seniorAdmin);

        LocalDateTime timestamp = (escalationDate != null) 
                ? escalationDate.atTime(12, 0) 
                : LocalDateTime.now();
        this.statusHistory.add(new StatusChange(Status.ESCALATED, timestamp, note));
    }

    /**
     * Checks if complaint is unresolved and has exceeded its priority threshold.
     */
    public boolean isOverdue(LocalDate referenceDate) {
        if (!status.isUnresolved()) {
            return false;
        }
        return getDaysSinceFiled(referenceDate) > priority.getEscalationDays();
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public String getComplaintId() {
        return complaintId;
    }

    public String getId() {
        return complaintId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null && !type.trim().isEmpty()) {
            this.type = type.trim();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description.trim();
        }
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        if (priority != null) {
            this.priority = priority;
        }
    }

    public LocalDate getDateFiled() {
        return dateFiled;
    }

    /**
     * Public setter for tests and simulations to backdate complaints.
     */
    public void setDateFiled(LocalDate dateFiled) {
        if (dateFiled != null) {
            this.dateFiled = dateFiled;
        }
    }

    public User getFiledBy() {
        return filedBy;
    }

    public String getUserId() {
        return filedBy.getUserId();
    }

    public Admin getAssignedAdmin() {
        return assignedAdmin;
    }

    public void setAssignedAdmin(Admin assignedAdmin) {
        if (this.assignedAdmin != null) {
            this.assignedAdmin.decrementAssigned();
        }
        this.assignedAdmin = assignedAdmin;
        if (this.assignedAdmin != null) {
            this.assignedAdmin.incrementAssigned();
        }
    }

    public String getDetailsFormatted() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append(String.format(" Complaint ID : %-15s | Priority : %-10s\n", complaintId, priority));
        sb.append(String.format(" Filed By     : %-25s | Date     : %-10s\n", 
                filedBy.getName() + " (" + filedBy.getUserId() + ")", dateFiled.format(DATE_FMT)));
        sb.append(String.format(" Category     : %-15s | Status   : %-15s\n", type, status));
        String adminInfo = (assignedAdmin != null) 
                ? assignedAdmin.getName() + " (" + assignedAdmin.getRole() + ")" 
                : "Unassigned";
        sb.append(String.format(" Assigned To  : %s\n", adminInfo));
        sb.append("------------------------------------------------------------------------\n");
        sb.append(" Description  : ").append(description).append("\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append(" Status History (Audit Trail):\n");
        for (StatusChange sc : statusHistory) {
            sb.append("   • ").append(sc.toString()).append("\n");
        }
        sb.append("========================================================================");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Complaint)) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(complaintId, complaint.complaintId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(complaintId);
    }

    @Override
    public String toString() {
        String admin = (assignedAdmin != null) ? assignedAdmin.getName() : "Unassigned";
        return String.format("[%s] %-8s | %-12s | %-11s | Filed by: %s | Admin: %s | %s",
                complaintId, priority, type, status, filedBy.getName(), admin, description);
    }
}
