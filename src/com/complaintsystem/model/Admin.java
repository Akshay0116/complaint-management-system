package com.complaintsystem.model;

import java.time.LocalDate;

/**
 * Represents an Administrator in the system, authorized to manage,
 * update, assign, and close complaints.
 * Demonstrates Inheritance from {@link User}.
 */
public class Admin extends User {
    protected String adminLevel;
    protected String department;
    protected int assignedCount;

    /**
     * Primary constructor: Auto-generates userId and sets default adminLevel to "Regular".
     *
     * @param name           Full name of admin
     * @param contactDetails Email or phone number
     */
    public Admin(String name, String contactDetails) {
        super(name, contactDetails);
        this.adminLevel = "Regular";
        this.department = "General Administration";
        this.assignedCount = 0;
    }

    public Admin(String name, String contactDetails, String department) {
        super(name, contactDetails);
        this.adminLevel = "Regular";
        this.department = (department != null && !department.trim().isEmpty()) 
            ? department.trim() 
            : "General Administration";
        this.assignedCount = 0;
    }

    public Admin(String id, String name, String contactDetails, String department) {
        super(id, name, contactDetails);
        this.adminLevel = "Regular";
        this.department = (department != null && !department.trim().isEmpty()) 
            ? department.trim() 
            : "General Administration";
        this.assignedCount = 0;
    }

    public Admin(String id, String name, String email, String phoneNumber, String department) {
        super(id, name, email, phoneNumber);
        this.adminLevel = "Regular";
        this.department = (department != null && !department.trim().isEmpty()) 
            ? department.trim() 
            : "General Administration";
        this.assignedCount = 0;
    }

    // ==========================================
    // SPECIFIED METHODS
    // ==========================================

    /**
     * Validates transition and calls complaint.updateStatus().
     *
     * @param complaint  the target complaint
     * @param newStatus  the new status
     * @param note       explanation or progress note
     */
    public void updateStatus(Complaint complaint, Status newStatus, String note) {
        if (complaint == null) {
            System.out.println("[Error] Cannot update null complaint.");
            return;
        }
        if (complaint.getStatus() == Status.CLOSED) {
            System.out.println("[Warning] Complaint " + complaint.getComplaintId() + " is already CLOSED.");
            return;
        }
        complaint.updateStatus(newStatus, this, note, LocalDate.now());
    }

    /**
     * Sets status to CLOSED (or RESOLVED), logging resolution note.
     *
     * @param complaint      the target complaint
     * @param resolutionNote final resolution summary
     */
    public void closeComplaint(Complaint complaint, String resolutionNote) {
        if (complaint == null) {
            System.out.println("[Error] Cannot close null complaint.");
            return;
        }
        updateStatus(complaint, Status.CLOSED, resolutionNote);
    }

    /**
     * Assigns this admin as the current handler of the complaint.
     *
     * @param complaint the complaint to take ownership of
     */
    public void assignSelf(Complaint complaint) {
        if (complaint == null) {
            System.out.println("[Error] Cannot assign null complaint.");
            return;
        }
        complaint.setAssignedAdmin(this);
        System.out.println("[✓] Complaint " + complaint.getComplaintId() + " assigned to " + name + " (" + userId + ")");
    }

    // ==========================================
    // COMPATIBILITY & HELPER METHODS
    // ==========================================

    public boolean updateComplaintStatus(Complaint complaint, Status newStatus, String remarks, LocalDate updateDate) {
        if (complaint == null) return false;
        return complaint.updateStatus(newStatus, this, remarks, updateDate);
    }

    public boolean closeComplaint(Complaint complaint, String finalRemarks, LocalDate closeDate) {
        return updateComplaintStatus(complaint, Status.CLOSED, finalRemarks, closeDate);
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        if (adminLevel != null && !adminLevel.trim().isEmpty()) {
            this.adminLevel = adminLevel.trim();
        }
    }

    public boolean isSenior() {
        return "Senior".equalsIgnoreCase(adminLevel);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department != null && !department.trim().isEmpty()) {
            this.department = department.trim();
        }
    }

    public int getAssignedCount() {
        return assignedCount;
    }

    public void incrementAssigned() {
        this.assignedCount++;
    }

    public void decrementAssigned() {
        if (this.assignedCount > 0) {
            this.assignedCount--;
        }
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public boolean canHandleEscalations() {
        return isSenior();
    }

    @Override
    public String toString() {
        return String.format("[%s] Admin %s (Level: %s, Dept: %s, Assigned: %d)", 
                userId, name, adminLevel, department, assignedCount);
    }
}
