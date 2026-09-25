# Complaint Management System — User Manual & Setup Guide

Welcome to the **Complaint Management System** user manual. This guide provides comprehensive instructions on how to set up, compile, run, and demonstrate all system features.

---

## 1. System Requirements & Prerequisites
- **Java Development Kit (JDK)**: JDK 17, 21, or 26.
- **Operating System**: Windows, macOS, or Linux.
- **Terminal / Command Prompt / PowerShell**.

Verify your Java installation:
```bash
java -version
javac -version
```

---

## 2. Directory Structure
```
javaaproject3/
├── bin/                             # Compiled Java byte-code (.class files)
├── docs/                            # Modular design documentation
│   ├── 01-complaint-class.md
│   ├── 02-user-class.md
│   ├── 03-admin-class.md
│   ├── 04-complainttracker-class.md
│   ├── 05-relationships.md
│   ├── 06-workflow.md
│   ├── 07-escalation-logic.md
│   └── 08-test-cases.md
├── src/
│   └── com/complaintsystem/
│       ├── model/                   # Enums & Domain entities
│       │   ├── Priority.java
│       │   ├── Status.java
│       │   ├── StatusHistoryEntry.java
│       │   ├── User.java
│       │   ├── Admin.java
│       │   ├── SeniorAdmin.java
│       │   └── Complaint.java
│       ├── tracker/                 # Engine & business logic
│       │   └── ComplaintTracker.java
│       └── app/                     # CLI & Test runner
│           ├── ConsoleUI.java
│           ├── Main.java
│           └── SystemTest.java
├── DESIGN_DOCUMENT.md               # Unified Design Document
├── USER_MANUAL.md                   # This User Manual
└── README.md                        # Quickstart overview
```

---

## 3. Compilation & Execution

### Option A: Running from PowerShell or Command Prompt (Windows)

#### Step 1: Compile all Java source files
```powershell
javac -d bin src/com/complaintsystem/model/*.java src/com/complaintsystem/tracker/*.java src/com/complaintsystem/app/*.java
```

#### Step 2: Run the Interactive Console Application
```powershell
java -cp bin com.complaintsystem.app.Main
```

#### Step 3: Run the Automated Test Suite (Zero-interaction verification)
```powershell
java -cp bin com.complaintsystem.app.SystemTest
```

---

## 4. Pre-Seeded Demo Data

When the application starts, it automatically seeds realistic records so you can test features without manual data entry:

### Registered Users:
- `USR-101`: Alice Smith (`alice@example.com`)
- `USR-102`: David Miller (`david@example.com`)
- `USR-103`: Emma Watson (`emma@example.com`)

### Registered Administrators:
- `ADM-201`: Bob Johnson (Customer Support Admin)
- `ADM-S01`: Diana Prince (Executive Escalations — **Senior Admin**)

### Pre-Seeded Complaints:
- `CMP-0001`: **HIGH** Priority (filed 4 days ago) $\rightarrow$ *SLA Breached (SLA: 2 days)*
- `CMP-0002`: **LOW** Priority (filed 4 days ago) $\rightarrow$ *Within SLA (SLA: 10 days)*
- `CMP-0003`: **MEDIUM** Priority (filed 1 day ago) $\rightarrow$ *Within SLA (SLA: 5 days)*
- `CMP-0004`: **HIGH** Priority (filed 3 days ago, `IN_PROGRESS`) $\rightarrow$ *SLA Breached (SLA: 2 days)*

---

## 5. Feature Walkthrough & Demonstration

### Feature 1: Filing a New Complaint
1. Select `1. User Portal` $\rightarrow$ `1. File a New Complaint`.
2. Enter User ID: `USR-101`.
3. Enter Category: `Billing`.
4. Enter Description: `Double charged for annual service subscription`.
5. Select Priority: `3` (HIGH).
6. **Result**: The system assigns `CMP-0005` with status `OPEN` and an initial audit log entry.

### Feature 2: Checking Status & Full Audit Trail
1. Select `1. User Portal` $\rightarrow$ `2. Check Complaint Status & Full History`.
2. Enter Complaint ID: `CMP-0001`.
3. **Result**: The system prints a formatted card displaying category, priority, current status, assigned admin, and every chronological status update with author and timestamps.

### Feature 3: Priority Sorting & Status Filtering
1. Select `2. Admin Portal` $\rightarrow$ `4. Sort Complaints by Priority (Urgency)`.
2. **Result**: Displays all complaints arranged with `HIGH` priority tickets on top, followed by `MEDIUM` and `LOW`.
3. Select `2. Filter Complaints by Status` $\rightarrow$ choose `OPEN` or `IN_PROGRESS` to filter active queues.

### Feature 4: Automatic SLA Escalation & Time Travel
1. Select `3. Escalation Center`.
2. Select `1. Run SLA Auto-Escalation Check Now`.
3. **Result**:
   - `CMP-0001` (HIGH, 4 days old > 2 day SLA) and `CMP-0004` (HIGH, 3 days old > 2 day SLA) are immediately escalated!
   - Their status changes from `OPEN`/`IN_PROGRESS` $\rightarrow$ `ESCALATED`.
   - Both tickets are automatically reassigned to Senior Admin **Diana Prince** (`ADM-S01`).
   - `CMP-0002` (LOW, 4 days old $\le$ 10 days SLA) remains `OPEN`.
4. Advance the clock: Choose `3. Advance Simulated Date (+3 Days)` and re-run check to observe further escalations.

### Feature 5: Admin Resolution & Closure
1. Select `2. Admin Portal` $\rightarrow$ `6. Update Complaint Status / Resolve / Close`.
2. Enter Complaint ID: `CMP-0002`.
3. Enter Admin ID: `ADM-201`.
4. Select New Status: `2. RESOLVED`.
5. Enter Remarks: `Refund processed and credited back to customer account.`
6. **Result**: The status transitions to `RESOLVED` and the action is recorded in the permanent audit trail.
