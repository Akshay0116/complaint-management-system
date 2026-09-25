package com.complaintsystem.app;

import com.complaintsystem.model.*;
import com.complaintsystem.tracker.ComplaintTracker;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated Test Runner implementing test cases TC-01 through TC-11
 * from docs/08-test-cases.md.
 */
public class SystemTest {

    public static void runAll(ComplaintTracker existingTracker, LocalDate evalDate) {
        System.out.println("\n========================================================");
        System.out.println("   COMPLAINT MANAGEMENT SYSTEM - TEST SUITE (TC-01..11) ");
        System.out.println("========================================================");
        int passed = 0;
        int total = 11;

        LocalDate today = (evalDate != null) ? evalDate : LocalDate.now();

        // ----------------------------------------------------
        // TC-01: File a new complaint
        // ----------------------------------------------------
        Complaint tc01Complaint = null;
        ComplaintTracker tracker = new ComplaintTracker();
        try {
            System.out.print("[TC-01] File a new complaint ... ");
            User asha = new User("Asha Menon", "asha@example.com");
            tc01Complaint = tracker.fileComplaint("Billing", "Charged twice for the same invoice", Priority.HIGH, asha);

            boolean idOk = tc01Complaint.getComplaintId() != null && tc01Complaint.getComplaintId().startsWith("CMP-");
            boolean statusOk = tc01Complaint.getStatus() == Status.OPEN;
            boolean dateOk = tc01Complaint.getDateFiled() != null;
            boolean historyOk = !tc01Complaint.getStatusHistory().isEmpty() &&
                    tc01Complaint.getStatusHistory().get(0).getStatus() == Status.OPEN;

            if (idOk && statusOk && dateOk && historyOk) {
                System.out.println("PASSED (" + tc01Complaint.getComplaintId() + ", Status=OPEN, Date=" + tc01Complaint.getDateFiled() + ")");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-02: Check status of an existing complaint
        // ----------------------------------------------------
        try {
            System.out.print("[TC-02] Check status of existing complaint ... ");
            Status status = tracker.getStatus(tc01Complaint.getComplaintId());
            if (status == Status.OPEN) {
                System.out.println("PASSED (Status=" + status + ")");
                passed++;
            } else {
                System.out.println("FAILED (Expected OPEN, got " + status + ")");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-03: Check status of a non-existent complaint
        // ----------------------------------------------------
        try {
            System.out.print("[TC-03] Check status of non-existent complaint ... ");
            Status missingStatus = tracker.getStatus("CMP-9999");
            if (missingStatus == null) {
                System.out.println("PASSED (Returned null: 'Complaint not found.')");
                passed++;
            } else {
                System.out.println("FAILED (Expected null, got " + missingStatus + ")");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-04: Filter by priority
        // ----------------------------------------------------
        try {
            System.out.print("[TC-04] Filter by priority (HIGH) ... ");
            ComplaintTracker filterTracker = new ComplaintTracker();
            User user = new User("Test User", "test@test.com");
            filterTracker.fileComplaint("Billing", "High 1", Priority.HIGH, user);
            filterTracker.fileComplaint("Service", "Medium 1", Priority.MEDIUM, user);
            filterTracker.fileComplaint("Hardware", "High 2", Priority.HIGH, user);

            List<Complaint> highComplaints = filterTracker.filterByPriority(Priority.HIGH);
            boolean allHigh = highComplaints.stream().allMatch(c -> c.getPriority() == Priority.HIGH);
            if (highComplaints.size() == 2 && allHigh) {
                System.out.println("PASSED (Found 2 HIGH priority complaints, others excluded)");
                passed++;
            } else {
                System.out.println("FAILED (Expected 2, got " + highComplaints.size() + ")");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-05: Sort by priority
        // ----------------------------------------------------
        try {
            System.out.print("[TC-05] Sort by priority (HIGH -> MEDIUM -> LOW) ... ");
            ComplaintTracker sortTracker = new ComplaintTracker();
            User user = new User("Sort User", "sort@test.com");
            sortTracker.fileComplaint("Cat1", "Low Priority Ticket", Priority.LOW, user);
            sortTracker.fileComplaint("Cat2", "High Priority Ticket", Priority.HIGH, user);
            sortTracker.fileComplaint("Cat3", "Medium Priority Ticket", Priority.MEDIUM, user);

            List<Complaint> sorted = sortTracker.sortByPriority();
            if (sorted.size() == 3 &&
                    sorted.get(0).getPriority() == Priority.HIGH &&
                    sorted.get(1).getPriority() == Priority.MEDIUM &&
                    sorted.get(2).getPriority() == Priority.LOW) {
                System.out.println("PASSED ([HIGH, MEDIUM, LOW])");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-06: Escalation - overdue HIGH priority complaint
        // ----------------------------------------------------
        try {
            System.out.print("[TC-06] Escalation - overdue HIGH priority (3 days old > 2 days) ... ");
            ComplaintTracker escTracker = new ComplaintTracker();
            SeniorAdmin senior = new SeniorAdmin("Senior Diana", "diana@support.com");
            escTracker.registerAdmin(senior);

            User user = new User("Overdue User", "user@test.com");
            // HIGH complaint filed 3 days ago (threshold = 2 days)
            Complaint highOverdue = escTracker.fileComplaint("Network", "Outage in branch", Priority.HIGH, user);
            highOverdue.setDateFiled(today.minusDays(3));

            List<Complaint> newlyEscalated = escTracker.checkAndEscalate(today);
            boolean inReturnedList = newlyEscalated.contains(highOverdue);
            boolean statusEscalated = highOverdue.getStatus() == Status.ESCALATED;
            boolean noteOk = highOverdue.getStatusHistory().stream()
                    .anyMatch(sc -> sc.getNote().contains("overdue by 1 day(s)"));

            if (inReturnedList && statusEscalated && noteOk) {
                System.out.println("PASSED (Status=ESCALATED, History contains 'overdue by 1 day(s)')");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-07: Escalation - complaint within threshold
        // ----------------------------------------------------
        try {
            System.out.print("[TC-07] Escalation - complaint within threshold (2 days old <= 5 days) ... ");
            ComplaintTracker withinTracker = new ComplaintTracker();
            User user = new User("Within User", "within@test.com");
            // MEDIUM complaint filed 2 days ago (threshold = 5 days)
            Complaint mediumOk = withinTracker.fileComplaint("Billing", "Tax question", Priority.MEDIUM, user);
            mediumOk.setDateFiled(today.minusDays(2));

            List<Complaint> newlyEscalated = withinTracker.checkAndEscalate(today);
            boolean notInList = !newlyEscalated.contains(mediumOk);
            boolean statusRemainsOpen = mediumOk.getStatus() == Status.OPEN;

            if (notInList && statusRemainsOpen) {
                System.out.println("PASSED (Not in escalated list, status remains OPEN)");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-08: Admin updates status
        // ----------------------------------------------------
        try {
            System.out.print("[TC-08] Admin updates status ... ");
            User user = new User("User TC8", "u8@test.com");
            Complaint comp = tracker.fileComplaint("Billing", "Invoice discrepancy", Priority.LOW, user);
            Admin admin = new Admin("Admin Bob", "bob@support.com");

            admin.updateStatus(comp, Status.IN_PROGRESS, "Investigating billing system");
            boolean statusOk = comp.getStatus() == Status.IN_PROGRESS;
            boolean historyNoteOk = comp.getStatusHistory().stream()
                    .anyMatch(sc -> sc.getStatus() == Status.IN_PROGRESS && sc.getNote().contains("Investigating billing system"));

            if (statusOk && historyNoteOk) {
                System.out.println("PASSED (Status=IN_PROGRESS, History logged with timestamp & note)");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-09: Admin closes a complaint
        // ----------------------------------------------------
        try {
            System.out.print("[TC-09] Admin closes a complaint ... ");
            User user = new User("User TC9", "u9@test.com");
            Complaint comp = tracker.fileComplaint("Service", "Refund request", Priority.LOW, user);
            Admin admin = new Admin("Admin Bob", "bob@support.com");

            admin.closeComplaint(comp, "Refund issued, resolved");
            boolean statusOk = comp.getStatus() == Status.CLOSED;
            boolean historyNoteOk = comp.getStatusHistory().stream()
                    .anyMatch(sc -> sc.getStatus() == Status.CLOSED && sc.getNote().contains("Refund issued, resolved"));

            if (statusOk && historyNoteOk) {
                System.out.println("PASSED (Status=CLOSED, History gains final resolution note)");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-10: Escalated complaint already resolved is skipped on rescan
        // ----------------------------------------------------
        try {
            System.out.print("[TC-10] Already resolved complaint skipped on escalation rescan ... ");
            ComplaintTracker rescanTracker = new ComplaintTracker();
            User user = new User("User TC10", "u10@test.com");
            Admin admin = new Admin("Admin Bob", "bob@support.com");
            // Complaint filed 10 days ago (LOW priority)
            Complaint comp = rescanTracker.fileComplaint("Billing", "Old ticket", Priority.LOW, user);
            comp.setDateFiled(today.minusDays(10));

            // Mark RESOLVED
            comp.updateStatus(Status.RESOLVED, admin, "Resolved 5 days ago", today.minusDays(5));

            // Trigger checkAndEscalate
            List<Complaint> newlyEscalated = rescanTracker.checkAndEscalate(today);
            boolean notEscalated = !newlyEscalated.contains(comp);
            boolean statusRemainsResolved = comp.getStatus() == Status.RESOLVED;

            if (notEscalated && statusRemainsResolved) {
                System.out.println("PASSED (Complaint skipped, status remains RESOLVED)");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // ----------------------------------------------------
        // TC-11: Non-admin attempts to close a complaint
        // ----------------------------------------------------
        try {
            System.out.print("[TC-11] Non-admin cannot close complaint (enforced at compile-time by OOP design) ... ");
            // Verify via reflection that closeComplaint does NOT exist on User class
            boolean methodExistsOnUser = false;
            for (Method m : User.class.getDeclaredMethods()) {
                if ("closeComplaint".equals(m.getName())) {
                    methodExistsOnUser = true;
                    break;
                }
            }

            // Verify that closeComplaint DOES exist on Admin class
            boolean methodExistsOnAdmin = false;
            for (Method m : Admin.class.getDeclaredMethods()) {
                if ("closeComplaint".equals(m.getName())) {
                    methodExistsOnAdmin = true;
                    break;
                }
            }

            if (!methodExistsOnUser && methodExistsOnAdmin) {
                System.out.println("PASSED (compile-time error enforced by class design: User has no closeComplaint)");
                passed++;
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        System.out.println("\n--------------------------------------------------------");
        System.out.printf("Test Results: %d / %d Tests Passed (%.1f%%)\n", passed, total, (passed * 100.0 / total));
        System.out.println("--------------------------------------------------------\n");
    }

    public static void main(String[] args) {
        runAll(null, LocalDate.now());
    }
}
