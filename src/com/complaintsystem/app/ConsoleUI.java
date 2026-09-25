package com.complaintsystem.app;

import com.complaintsystem.model.*;
import com.complaintsystem.tracker.ComplaintTracker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive Console Interface for the Complaint Management System.
 * Provides user, administrator, and escalation testing portals with clean CLI navigation.
 */
public class ConsoleUI {
    private final ComplaintTracker tracker;
    private final Scanner scanner;
    private LocalDate simulatedToday;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ConsoleUI(ComplaintTracker tracker, LocalDate initialDate) {
        this.tracker = tracker;
        this.scanner = new Scanner(System.in);
        this.simulatedToday = (initialDate != null) ? initialDate : LocalDate.now();
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    userPortal();
                    break;
                case "2":
                    adminPortal();
                    break;
                case "3":
                    escalationCenter();
                    break;
                case "4":
                    runAutomatedTestSuite();
                    break;
                case "5":
                    displaySystemOverview();
                    break;
                case "6":
                    System.out.println("\nThank you for using the Complaint Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid selection. Please enter a number between 1 and 6.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n========================================================");
        System.out.println("          COMPLAINT MANAGEMENT SYSTEM (JAVA OOP)       ");
        System.out.println("========================================================");
        System.out.println("  Simulated Date: " + simulatedToday.format(DATE_FMT));
        System.out.println("--------------------------------------------------------");
        System.out.println("  1. User Portal (File complaint, check status, history)");
        System.out.println("  2. Admin Portal (View, filter, sort, update & resolve)");
        System.out.println("  3. Escalation Center (Run auto-escalation, time-travel)");
        System.out.println("  4. Run Automated Demo & Test Cases");
        System.out.println("  5. View System Overview & Accounts");
        System.out.println("  6. Exit");
        System.out.println("--------------------------------------------------------");
        System.out.print("Select an option (1-6): ");
    }

    // =========================================================
    // USER PORTAL
    // =========================================================

    private void userPortal() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [ USER PORTAL ] ---");
            System.out.println("1. File a New Complaint");
            System.out.println("2. Check Complaint Status & Full History");
            System.out.println("3. View All Complaints Filed by Me");
            System.out.println("4. Register New User Account");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice (1-5): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    fileComplaintFlow();
                    break;
                case "2":
                    checkStatusFlow();
                    break;
                case "3":
                    viewMyComplaintsFlow();
                    break;
                case "4":
                    registerUserFlow();
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Enter 1-5.");
            }
        }
    }

    private void fileComplaintFlow() {
        System.out.println("\n>> File a New Complaint");
        System.out.print("Enter your User ID (e.g., USR-101): ");
        String userId = scanner.nextLine().trim();
        User user = tracker.getUser(userId);
        if (user == null) {
            System.out.println("[!] User ID not found. Available demo users: USR-101, USR-102, USR-103");
            return;
        }

        System.out.print("Enter Complaint Category/Type (e.g. Billing, Technical, Service): ");
        String type = scanner.nextLine().trim();

        System.out.print("Enter Detailed Description: ");
        String desc = scanner.nextLine().trim();

        System.out.println("Select Priority Level:");
        System.out.println("  1. LOW    (SLA: 10 days before escalation)");
        System.out.println("  2. MEDIUM (SLA: 5 days before escalation)");
        System.out.println("  3. HIGH   (SLA: 2 days before escalation)");
        System.out.print("Choose priority (1-3) [default: 2]: ");
        String pChoice = scanner.nextLine().trim();
        Priority priority = Priority.MEDIUM;
        if ("1".equals(pChoice)) priority = Priority.LOW;
        else if ("3".equals(pChoice)) priority = Priority.HIGH;

        try {
            Complaint complaint = user.fileComplaint(tracker, type, desc, priority);
            System.out.println("\n[✓] Complaint filed successfully!");
            System.out.println("    Assigned ID  : " + complaint.getComplaintId());
            System.out.println("    Priority     : " + priority + " (SLA: " + priority.getEscalationDays() + " days)");
            System.out.println("    Initial State: OPEN");
            System.out.println("    Date Filed   : " + simulatedToday.format(DATE_FMT));
        } catch (Exception e) {
            System.out.println("[!] Error filing complaint: " + e.getMessage());
        }
    }

    private void checkStatusFlow() {
        System.out.print("\nEnter Complaint ID to inspect (e.g. CMP-0001): ");
        String id = scanner.nextLine().trim();
        Complaint c = tracker.getComplaint(id);
        if (c == null) {
            System.out.println("[!] Complaint with ID '" + id + "' was not found.");
            return;
        }
        System.out.println("\n" + c.getDetailsFormatted());
    }

    private void viewMyComplaintsFlow() {
        System.out.print("\nEnter your User ID: ");
        String userId = scanner.nextLine().trim();
        List<Complaint> myComplaints = tracker.getComplaintsByUser(userId);
        if (myComplaints.isEmpty()) {
            System.out.println("No complaints on record for user: " + userId);
            return;
        }
        System.out.println("\nComplaints filed by " + userId + " (" + myComplaints.size() + " total):");
        printComplaintTable(myComplaints);
    }

    private void registerUserFlow() {
        System.out.print("\nEnter new User ID (e.g. USR-104): ");
        String id = scanner.nextLine().trim();
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();

        try {
            User newUser = new User(id, name, email, phone);
            tracker.registerUser(newUser);
            System.out.println("[✓] User registered successfully: " + newUser);
        } catch (Exception e) {
            System.out.println("[!] Registration error: " + e.getMessage());
        }
    }

    // =========================================================
    // ADMIN PORTAL
    // =========================================================

    private void adminPortal() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [ ADMIN PORTAL ] ---");
            System.out.println("1. View All Complaints");
            System.out.println("2. Filter Complaints by Status");
            System.out.println("3. Filter Complaints by Priority");
            System.out.println("4. Sort Complaints by Priority (Urgency)");
            System.out.println("5. Sort Complaints by Date Filed");
            System.out.println("6. Update Complaint Status / Resolve / Close");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter choice (1-7): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    printComplaintTable(tracker.getAllComplaints());
                    break;
                case "2":
                    filterStatusFlow();
                    break;
                case "3":
                    filterPriorityFlow();
                    break;
                case "4":
                    System.out.println("\nSorted by Priority (Urgent First):");
                    printComplaintTable(tracker.sortByPriority(true));
                    break;
                case "5":
                    System.out.println("\nSorted by Filing Date (Newest First):");
                    printComplaintTable(tracker.sortByDateFiled(true));
                    break;
                case "6":
                    updateStatusFlow();
                    break;
                case "7":
                    back = true;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Enter 1-7.");
            }
        }
    }

    private void filterStatusFlow() {
        System.out.println("\nSelect Status to Filter by:");
        System.out.println("1. OPEN");
        System.out.println("2. IN_PROGRESS");
        System.out.println("3. ESCALATED");
        System.out.println("4. RESOLVED");
        System.out.println("5. CLOSED");
        System.out.print("Choice: ");
        String c = scanner.nextLine().trim();
        Status s = null;
        switch (c) {
            case "1": s = Status.OPEN; break;
            case "2": s = Status.IN_PROGRESS; break;
            case "3": s = Status.ESCALATED; break;
            case "4": s = Status.RESOLVED; break;
            case "5": s = Status.CLOSED; break;
            default: System.out.println("[!] Invalid status selection."); return;
        }
        List<Complaint> list = tracker.filterByStatus(s);
        System.out.println("\nFiltered results for status [" + s + "] (" + list.size() + " matches):");
        printComplaintTable(list);
    }

    private void filterPriorityFlow() {
        System.out.println("\nSelect Priority to Filter by:");
        System.out.println("1. HIGH");
        System.out.println("2. MEDIUM");
        System.out.println("3. LOW");
        System.out.print("Choice: ");
        String c = scanner.nextLine().trim();
        Priority p = null;
        switch (c) {
            case "1": p = Priority.HIGH; break;
            case "2": p = Priority.MEDIUM; break;
            case "3": p = Priority.LOW; break;
            default: System.out.println("[!] Invalid priority selection."); return;
        }
        List<Complaint> list = tracker.filterByPriority(p);
        System.out.println("\nFiltered results for priority [" + p + "] (" + list.size() + " matches):");
        printComplaintTable(list);
    }

    private void updateStatusFlow() {
        System.out.print("\nEnter Complaint ID to update: ");
        String cid = scanner.nextLine().trim();
        Complaint comp = tracker.getComplaint(cid);
        if (comp == null) {
            System.out.println("[!] Complaint not found.");
            return;
        }

        System.out.print("Enter Admin ID performing update (e.g. ADM-201 or ADM-S01): ");
        String aid = scanner.nextLine().trim();
        Admin admin = tracker.getAdmin(aid);
        if (admin == null) {
            System.out.println("[!] Admin not recognized. Seeded admins: ADM-201 (Bob), ADM-S01 (Senior Diana)");
            return;
        }

        System.out.println("Select New Status:");
        System.out.println("1. IN_PROGRESS");
        System.out.println("2. RESOLVED");
        System.out.println("3. CLOSED");
        System.out.print("Choice: ");
        String sc = scanner.nextLine().trim();
        Status newStatus = null;
        if ("1".equals(sc)) newStatus = Status.IN_PROGRESS;
        else if ("2".equals(sc)) newStatus = Status.RESOLVED;
        else if ("3".equals(sc)) newStatus = Status.CLOSED;
        else {
            System.out.println("[!] Invalid status option.");
            return;
        }

        System.out.print("Enter Resolution/Progress Remarks: ");
        String remarks = scanner.nextLine().trim();

        boolean ok = tracker.updateComplaintStatus(cid, newStatus, aid, remarks, simulatedToday);
        if (ok) {
            System.out.println("[✓] Complaint status successfully updated to " + newStatus + "!");
        } else {
            System.out.println("[!] Failed to update status.");
        }
    }

    // =========================================================
    // ESCALATION CENTER & SIMULATION
    // =========================================================

    private void escalationCenter() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [ ESCALATION CENTER & SIMULATION ] ---");
            System.out.println("  Current Simulated Date : " + simulatedToday.format(DATE_FMT));
            System.out.println("  SLA Escalation Rules   : HIGH > 2 days | MEDIUM > 5 days | LOW > 10 days");
            System.out.println("-------------------------------------------------------------------");
            System.out.println("1. Run SLA Auto-Escalation Check Now");
            System.out.println("2. Advance Simulated Date (+1 Day)");
            System.out.println("3. Advance Simulated Date (+3 Days)");
            System.out.println("4. Advance Simulated Date (+7 Days)");
            System.out.println("5. Jump to Specific Date");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter choice (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    runEscalationCheck();
                    break;
                case "2":
                    advanceDate(1);
                    break;
                case "3":
                    advanceDate(3);
                    break;
                case "4":
                    advanceDate(7);
                    break;
                case "5":
                    System.out.print("Enter new date (yyyy-MM-dd): ");
                    try {
                        simulatedToday = LocalDate.parse(scanner.nextLine().trim(), DATE_FMT);
                        System.out.println("[✓] Simulated date updated to: " + simulatedToday.format(DATE_FMT));
                    } catch (Exception e) {
                        System.out.println("[!] Invalid date format. Please use yyyy-MM-dd.");
                    }
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println("[!] Invalid option.");
            }
        }
    }

    private void advanceDate(int days) {
        simulatedToday = simulatedToday.plusDays(days);
        System.out.println("\n[✓] Clock fast-forwarded by " + days + " day(s).");
        System.out.println("    New Simulated Date: " + simulatedToday.format(DATE_FMT));
        System.out.println("    Run 'Option 1' to check for newly overdue complaints.");
    }

    private void runEscalationCheck() {
        System.out.println("\n>> Evaluating all active complaints against date: " + simulatedToday.format(DATE_FMT));
        List<Complaint> escalated = tracker.checkAndEscalate(simulatedToday);
        if (escalated.isEmpty()) {
            System.out.println("[✓] All complaints are within their permitted SLA. No complaints escalated.");
        } else {
            System.out.println("[!] " + escalated.size() + " complaint(s) breached SLA and were ESCALATED to Senior Admin:");
            for (Complaint c : escalated) {
                System.out.printf("    • %s | Priority: %-6s | Filed: %s (%d days ago) | Assigned: %s\n",
                    c.getId(),
                    c.getPriority(),
                    c.getDateFiled().format(DATE_FMT),
                    c.getDaysElapsed(simulatedToday),
                    (c.getAssignedAdmin() != null ? c.getAssignedAdmin().getName() : "None"));
            }
        }
    }

    // =========================================================
    // AUTOMATED TEST SUITE & DEMO
    // =========================================================

    public void runAutomatedTestSuite() {
        System.out.println("\n========================================================");
        System.out.println("       RUNNING AUTOMATED TEST CASES & DEMO SUITE        ");
        System.out.println("========================================================");
        SystemTest.runAll(tracker, simulatedToday);
    }

    private void displaySystemOverview() {
        System.out.println("\n================ [ SYSTEM OVERVIEW ] ================");
        System.out.println("Registered Users (" + tracker.getAllUsers().size() + "):");
        for (User u : tracker.getAllUsers()) {
            System.out.println("  • " + u);
        }
        System.out.println("\nRegistered Administrators (" + tracker.getAllAdmins().size() + "):");
        for (Admin a : tracker.getAllAdmins()) {
            System.out.println("  • " + a);
        }
        System.out.println("\nTotal Registered Complaints: " + tracker.getAllComplaints().size());
        printComplaintTable(tracker.getAllComplaints());
    }

    // =========================================================
    // HELPER: TABLE FORMATTER
    // =========================================================

    public static void printComplaintTable(List<Complaint> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("(No complaints to display)");
            return;
        }
        System.out.println("+----------+----------+-------------+--------------+------------+--------------------------------+");
        System.out.println("| ID       | Priority | Category    | Status       | Date Filed | Assigned Admin                 |");
        System.out.println("+----------+----------+-------------+--------------+------------+--------------------------------+");
        for (Complaint c : list) {
            String admin = (c.getAssignedAdmin() != null) ? c.getAssignedAdmin().getName() : "Unassigned";
            System.out.printf("| %-8s | %-8s | %-11s | %-12s | %-10s | %-30s |\n",
                c.getId(),
                c.getPriority(),
                truncate(c.getType(), 11),
                c.getCurrentStatus(),
                c.getDateFiled().format(DATE_FMT),
                truncate(admin, 30));
        }
        System.out.println("+----------+----------+-------------+--------------+------------+--------------------------------+");
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return (s.length() <= max) ? s : s.substring(0, max - 3) + "...";
    }
}
