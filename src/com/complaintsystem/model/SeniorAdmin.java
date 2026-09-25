package com.complaintsystem.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a Senior Administrator authorized to handle escalated complaints.
 * Demonstrates Multi-level Inheritance (SeniorAdmin -> Admin -> User).
 */
public class SeniorAdmin extends Admin {

    /**
     * Primary constructor: Auto-generates userId and sets adminLevel to "Senior".
     *
     * @param name           Full name
     * @param contactDetails Email or phone number
     */
    public SeniorAdmin(String name, String contactDetails) {
        super(name, contactDetails);
        this.adminLevel = "Senior";
    }

    public SeniorAdmin(String name, String contactDetails, String department) {
        super(name, contactDetails, department);
        this.adminLevel = "Senior";
    }

    public SeniorAdmin(String id, String name, String contactDetails, String department) {
        super(id, name, contactDetails, department);
        this.adminLevel = "Senior";
    }

    public SeniorAdmin(String id, String name, String email, String phoneNumber, String department) {
        super(id, name, email, phoneNumber, department);
        this.adminLevel = "Senior";
    }

    // ==========================================
    // SPECIFIED METHODS
    // ==========================================

    /**
     * Prints and reviews all complaints flagged as ESCALATED.
     *
     * @param escalatedList list of escalated complaints to review
     */
    public void reviewEscalated(List<Complaint> escalatedList) {
        System.out.println("\n========================================================");
        System.out.println("  SENIOR ADMIN ESCALATION REVIEW: " + name + " (" + userId + ")");
        System.out.println("========================================================");
        if (escalatedList == null || escalatedList.isEmpty()) {
            System.out.println("No escalated complaints currently require review.");
            return;
        }

        int count = 0;
        for (Complaint c : escalatedList) {
            if (c.getStatus() == Status.ESCALATED) {
                count++;
                System.out.printf("[%d] ID: %s | Priority: %-6s | Category: %-12s\n", 
                        count, c.getComplaintId(), c.getPriority(), c.getType());
                System.out.println("    Filer       : " + c.getFiledBy().getName() + " (" + c.getFiledBy().getUserId() + ")");
                System.out.println("    Description : " + c.getDescription());
                System.out.println("    Days Elapsed: " + c.getDaysSinceFiled() + " days");
                System.out.println("--------------------------------------------------------");
            }
        }
        System.out.println("Total Escalated Complaints Reviewed: " + count);
    }

    // ==========================================
    // ROLE & CAPABILITY OVERRIDES
    // ==========================================

    @Override
    public String getRole() {
        return "SENIOR_ADMIN";
    }

    @Override
    public boolean canHandleEscalations() {
        return true;
    }

    @Override
    public boolean isSenior() {
        return true;
    }

    /**
     * Resolves an escalated complaint with senior executive sign-off.
     */
    public boolean resolveEscalatedComplaint(Complaint complaint, String resolutionNotes, LocalDate resolutionDate) {
        if (complaint == null) return false;
        return complaint.updateStatus(Status.RESOLVED, this, "[Senior Sign-off] " + resolutionNotes, resolutionDate);
    }

    @Override
    public String toString() {
        return String.format("[%s] Senior Admin %s (Dept: %s, Assigned: %d)", 
                userId, name, department, assignedCount);
    }
}
