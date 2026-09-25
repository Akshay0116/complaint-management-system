package com.complaintsystem.model;

import com.complaintsystem.tracker.ComplaintTracker;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents anyone who can file complaints and check their status.
 * Base class for Admin.
 */
public class User {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    protected final String userId;
    protected String name;
    protected String contactDetails;

    /**
     * Primary constructor: Auto-generates userId (e.g., USR-001).
     *
     * @param name           Full name of user
     * @param contactDetails Email or phone number
     */
    public User(String name, String contactDetails) {
        this(generateNextUserId(), name, contactDetails);
    }

    /**
     * Overloaded constructor supporting predefined IDs (e.g. for seed data and tests).
     */
    public User(String userId, String name, String contactDetails) {
        if (userId == null || userId.trim().isEmpty()) {
            this.userId = generateNextUserId();
        } else {
            this.userId = userId.trim();
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty.");
        }
        this.name = name.trim();
        this.contactDetails = (contactDetails != null && !contactDetails.trim().isEmpty()) 
                ? contactDetails.trim() 
                : "No contact provided";
    }

    /**
     * Backward-compatibility constructor supporting separate email and phone.
     */
    public User(String userId, String name, String email, String phoneNumber) {
        this(userId, name, formatContact(email, phoneNumber));
    }

    private static String generateNextUserId() {
        return String.format("USR-%03d", ID_GENERATOR.incrementAndGet());
    }

    private static String formatContact(String email, String phone) {
        if (email != null && phone != null && !email.isEmpty() && !phone.isEmpty()) {
            return email + ", " + phone;
        } else if (email != null && !email.isEmpty()) {
            return email;
        } else if (phone != null && !phone.isEmpty()) {
            return phone;
        }
        return "No contact provided";
    }

    // ==========================================
    // SPECIFIED METHODS
    // ==========================================

    /**
     * Delegates complaint filing to ComplaintTracker, passing this User as filer.
     *
     * @param tracker     the system ComplaintTracker
     * @param type        Category of complaint
     * @param description Free-text details
     * @param priority    LOW, MEDIUM, HIGH
     * @return newly filed Complaint
     */
    public Complaint fileComplaint(ComplaintTracker tracker, String type, String description, Priority priority) {
        if (tracker == null) {
            throw new IllegalArgumentException("ComplaintTracker cannot be null.");
        }
        return tracker.fileComplaint(type, description, priority, this);
    }

    /**
     * Checks the current status of a complaint via ComplaintTracker.
     *
     * @param tracker     the system ComplaintTracker
     * @param complaintId unique identifier of the complaint
     * @return current Status of complaint or null if not found
     */
    public Status checkStatus(ComplaintTracker tracker, String complaintId) {
        if (tracker == null) {
            throw new IllegalArgumentException("ComplaintTracker cannot be null.");
        }
        return tracker.getStatus(complaintId);
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public String getUserId() {
        return userId;
    }

    /**
     * Compatibility alias for getUserId().
     */
    public String getId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        if (contactDetails != null && !contactDetails.trim().isEmpty()) {
            this.contactDetails = contactDetails.trim();
        }
    }

    public String getEmail() {
        return contactDetails;
    }

    public String getPhoneNumber() {
        return contactDetails;
    }

    /**
     * Polymorphic method to identify the role of the user.
     * Overridden in Admin and SeniorAdmin.
     */
    public String getRole() {
        return "USER";
    }

    public void displayProfile() {
        System.out.println("----------------------------------------");
        System.out.println("User ID   : " + userId);
        System.out.println("Role      : " + getRole());
        System.out.println("Name      : " + name);
        System.out.println("Contact   : " + contactDetails);
        System.out.println("----------------------------------------");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", userId, name, contactDetails);
    }
}
