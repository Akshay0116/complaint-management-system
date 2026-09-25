# Sample Test Cases

These map directly to each functionality and can become JUnit tests or
console demo steps.

## TC-01: File a new complaint

**Input:**
```
type = "Billing"
description = "Charged twice for the same invoice"
priority = HIGH
filedBy = User("Asha Menon", "asha@example.com")
```
**Expected output:**
```
Complaint ID: CMP-0001
Status: OPEN
Date Filed: 2026-09-25
History: [OPEN @ 2026-09-25 - "Filed"]
```

## TC-02: Check status of an existing complaint

**Input:** `getStatus("CMP-0001")`
**Expected output:** `OPEN`

## TC-03: Check status of a non-existent complaint

**Input:** `getStatus("CMP-9999")`
**Expected output:** Error/`null` with message `"Complaint not found."`

## TC-04: Filter by priority

**Setup:** 3 complaints filed — HIGH, MEDIUM, HIGH
**Input:** `filterByPriority(HIGH)`
**Expected output:** List of the 2 HIGH-priority complaints, others excluded

## TC-05: Sort by priority

**Setup:** Complaints filed in order LOW, HIGH, MEDIUM
**Input:** `sortByPriority()`
**Expected output:** `[HIGH complaint, MEDIUM complaint, LOW complaint]`

## TC-06: Escalation — overdue HIGH priority complaint

**Setup:** HIGH complaint filed 3 days ago (threshold = 2 days), still OPEN
**Input:** `checkAndEscalate()`
**Expected output:**
```
Returned list contains this complaint.
Its status is now ESCALATED.
History has new entry: "Escalated: overdue by 1 day(s)"
```

## TC-07: Escalation — complaint within threshold

**Setup:** MEDIUM complaint filed 2 days ago (threshold = 5 days)
**Input:** `checkAndEscalate()`
**Expected output:** Returned list does NOT contain this complaint; status
remains `OPEN`.

## TC-08: Admin updates status

**Input:** `admin.updateStatus(complaint, IN_PROGRESS, "Investigating billing system")`
**Expected output:**
```
Status: IN_PROGRESS
History gains entry: IN_PROGRESS @ <timestamp> - "Investigating billing system"
```

## TC-09: Admin closes a complaint

**Input:** `admin.closeComplaint(complaint, "Refund issued, resolved")`
**Expected output:**
```
Status: RESOLVED (or CLOSED)
History gains final entry with resolution note
```

## TC-10: Escalated complaint already resolved is skipped on rescan

**Setup:** Complaint status = `RESOLVED`, `dateFiled` 10 days ago (LOW priority, threshold 10)
**Input:** `checkAndEscalate()`
**Expected output:** Complaint NOT included in escalated list, status remains
`RESOLVED`.

## TC-11: Non-admin attempts to close a complaint

**Input:** `user.closeComplaint(...)` — note: `User` has no such method
**Expected output:** Compile-time error (this is enforced by design, not a
runtime check — `closeComplaint()` only exists on `Admin`, demonstrating
encapsulation/access control through class design rather than `if` checks).
