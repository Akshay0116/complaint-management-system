package com.complaintsystem.tracker;

import com.complaintsystem.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The core engine of the system. Holds all complaints and provides every
 * operation for filing, querying, sorting, and escalating them.
 */
public class ComplaintTracker {
    private final Map<String, Complaint> complaints;
    private final Map<Priority, Integer> escalationThresholds;
    private final Map<String, User> users;
    private final Map<String, Admin> admins;
    private int idCounter;

    public ComplaintTracker() {
        this.complaints = new LinkedHashMap<>();
        this.users = new LinkedHashMap<>();
        this.admins = new LinkedHashMap<>();
        this.escalationThresholds = new EnumMap<>(Priority.class);
        this.escalationThresholds.put(Priority.HIGH, 2);
        this.escalationThresholds.put(Priority.MEDIUM, 5);
        this.escalationThresholds.put(Priority.LOW, 10);
        this.idCounter = 0;
    }

    // ==========================================
    // USER & ADMIN REGISTRATION
    // ==========================================

    public void registerUser(User user) {
        if (user != null) {
            users.put(user.getUserId(), user);
        }
    }

    public void registerAdmin(Admin admin) {
        if (admin != null) {
            admins.put(admin.getUserId(), admin);
            users.put(admin.getUserId(), admin);
        }
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public Admin getAdmin(String adminId) {
        return admins.get(adminId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<Admin> getAllAdmins() {
        return new ArrayList<>(admins.values());
    }

    public SeniorAdmin findAvailableSeniorAdmin() {
        for (Admin admin : admins.values()) {
            if (admin instanceof SeniorAdmin) {
                return (SeniorAdmin) admin;
            }
        }
        return null;
    }

    public Map<Priority, Integer> getEscalationThresholds() {
        return Collections.unmodifiableMap(escalationThresholds);
    }

    // ==========================================
    // SPECIFIED CORE METHODS
    // ==========================================

    /**
     * Creates a new Complaint, assigns ID, stores it, and returns it.
     */
    public Complaint fileComplaint(String type, String description, Priority priority, User filedBy) {
        if (filedBy == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        registerUser(filedBy);

        idCounter++;
        String complaintId = String.format("CMP-%04d", idCounter);

        Complaint complaint = new Complaint(complaintId, type, description, priority, filedBy, LocalDate.now());
        complaints.put(complaintId, complaint);
        return complaint;
    }

    /**
     * Looks up and returns the current status of a complaint.
     */
    public Status getStatus(String complaintId) {
        Complaint c = complaints.get(complaintId);
        return (c != null) ? c.getStatus() : null;
    }

    /**
     * Returns the full Complaint object (for detail view).
     */
    public Complaint getComplaint(String complaintId) {
        return complaints.get(complaintId);
    }

    /**
     * Returns all complaints matching that priority.
     */
    public List<Complaint> filterByPriority(Priority p) {
        if (p == null) return getAllComplaints();
        return complaints.values().stream()
                .filter(c -> c.getPriority() == p)
                .collect(Collectors.toList());
    }

    /**
     * Returns all complaints, sorted HIGH -> LOW.
     */
    public List<Complaint> sortByPriority() {
        return sortByPriority(true);
    }

    public List<Complaint> sortByPriority(boolean highFirst) {
        List<Complaint> sorted = new ArrayList<>(complaints.values());
        Comparator<Complaint> comparator = Comparator.comparingInt((Complaint c) -> {
            switch (c.getPriority()) {
                case HIGH: return 3;
                case MEDIUM: return 2;
                case LOW: return 1;
                default: return 0;
            }
        });

        if (highFirst) {
            comparator = comparator.reversed();
        }
        sorted.sort(comparator);
        return sorted;
    }

    /**
     * Returns all complaints, oldest first.
     */
    public List<Complaint> sortByDateFiled() {
        return sortByDateFiled(false);
    }

    public List<Complaint> sortByDateFiled(boolean newestFirst) {
        List<Complaint> sorted = new ArrayList<>(complaints.values());
        Comparator<Complaint> comparator = Comparator.comparing(Complaint::getDateFiled);
        if (newestFirst) {
            comparator = comparator.reversed();
        }
        sorted.sort(comparator);
        return sorted;
    }

    /**
     * Scans all OPEN/IN_PROGRESS complaints; escalates overdue ones;
     * returns the list of newly escalated complaints.
     */
    public List<Complaint> checkAndEscalate() {
        return checkAndEscalate(LocalDate.now());
    }

    /**
     * Overloaded escalation engine evaluating against a specific reference/simulated date.
     */
    public List<Complaint> checkAndEscalate(LocalDate referenceDate) {
        List<Complaint> newlyEscalated = new ArrayList<>();
        SeniorAdmin seniorAdmin = findAvailableSeniorAdmin();

        for (Complaint c : complaints.values()) {
            if (c.getStatus() == Status.OPEN || c.getStatus() == Status.IN_PROGRESS) {
                int defaultLow = escalationThresholds.getOrDefault(Priority.LOW, 10);
                int threshold = (c.getPriority() != null) 
                        ? escalationThresholds.getOrDefault(c.getPriority(), defaultLow) 
                        : defaultLow;
                int daysElapsed = c.getDaysSinceFiled(referenceDate);

                if (daysElapsed > threshold) {
                    String note = "Escalated: overdue by " + (daysElapsed - threshold) + " day(s)";
                    c.escalate(seniorAdmin, referenceDate, note);
                    newlyEscalated.add(c);
                }
            }
        }
        return newlyEscalated;
    }

    /**
     * Returns all registered complaints (for admin dashboard view).
     */
    public List<Complaint> getAllComplaints() {
        return new ArrayList<>(complaints.values());
    }

    // ==========================================
    // ADDITIONAL CONVENIENCE METHODS
    // ==========================================

    /**
     * Convenience method to file complaint using user ID and custom date.
     */
    public String fileComplaint(String userId, String type, String description, Priority priority, LocalDate dateFiled) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID '" + userId + "' does not exist. Please register first.");
        }

        idCounter++;
        String complaintId = String.format("CMP-%04d", idCounter);
        Complaint complaint = new Complaint(complaintId, type, description, priority, user, dateFiled);
        complaints.put(complaintId, complaint);
        return complaintId;
    }

    public List<Complaint> getComplaintsByUser(String userId) {
        return complaints.values().stream()
                .filter(c -> c.getUserId().equalsIgnoreCase(userId))
                .collect(Collectors.toList());
    }

    public List<Complaint> filterByStatus(Status status) {
        if (status == null) return getAllComplaints();
        return complaints.values().stream()
                .filter(c -> c.getStatus() == status)
                .collect(Collectors.toList());
    }

    public boolean updateComplaintStatus(String complaintId, Status newStatus, String adminId, String remarks, LocalDate updateDate) {
        Complaint complaint = complaints.get(complaintId);
        if (complaint == null) {
            System.out.println("[Error] Complaint ID '" + complaintId + "' does not exist.");
            return false;
        }

        Admin admin = admins.get(adminId);
        if (admin == null) {
            System.out.println("[Error] Admin ID '" + adminId + "' is not registered or authorized.");
            return false;
        }

        return admin.updateComplaintStatus(complaint, newStatus, remarks, updateDate);
    }

    public boolean closeComplaint(String complaintId, String adminId, String remarks, LocalDate closeDate) {
        return updateComplaintStatus(complaintId, Status.CLOSED, adminId, remarks, closeDate);
    }

    /**
     * Pre-loads realistic sample dataset for immediate testing and verification.
     */
    public void seedSampleData(LocalDate baseDate) {
        // Users
        User u1 = new User("USR-101", "Alice Smith", "alice@example.com", "+1-555-0101");
        User u2 = new User("USR-102", "David Miller", "david@example.com", "+1-555-0102");
        User u3 = new User("USR-103", "Emma Watson", "emma@example.com", "+1-555-0103");
        registerUser(u1);
        registerUser(u2);
        registerUser(u3);

        // Admins
        Admin a1 = new Admin("ADM-201", "Bob Johnson", "bob@support.com", "+1-555-0201", "Customer Support");
        SeniorAdmin sa1 = new SeniorAdmin("ADM-S01", "Diana Prince", "diana@support.com", "+1-555-0301", "Executive Escalations");
        registerAdmin(a1);
        registerAdmin(sa1);

        // Pre-filed Complaints
        // CMP-0001: HIGH Priority, filed 4 days ago -> OVERDUE (SLA: 2 days)
        fileComplaint(u1.getUserId(), "Billing", "Double charge on subscription fee", Priority.HIGH, baseDate.minusDays(4));

        // CMP-0002: LOW Priority, filed 4 days ago -> NOT Overdue (SLA: 10 days)
        fileComplaint(u2.getUserId(), "Feature Request", "Request dark mode option in portal", Priority.LOW, baseDate.minusDays(4));

        // CMP-0003: MEDIUM Priority, filed 1 day ago -> NOT Overdue (SLA: 5 days)
        fileComplaint(u3.getUserId(), "Technical", "Password reset email link not arriving", Priority.MEDIUM, baseDate.minusDays(1));

        // CMP-0004: HIGH Priority, filed 3 days ago, put IN_PROGRESS by Bob -> OVERDUE (SLA: 2 days)
        String c4Id = fileComplaint(u1.getUserId(), "Account", "Account locked after 2FA outage", Priority.HIGH, baseDate.minusDays(3));
        Complaint c4 = getComplaint(c4Id);
        c4.updateStatus(Status.IN_PROGRESS, a1, "Investigating auth logs", baseDate.minusDays(2));
    }
}
