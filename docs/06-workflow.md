# End-to-End Workflow

## 1. Filing a complaint

1. `User` calls `fileComplaint(tracker, type, description, priority)`.
2. `ComplaintTracker.fileComplaint()`:
   - Generates a new `complaintId`.
   - Creates a `Complaint` with `status = OPEN`, `dateFiled = today`.
   - Logs the first `StatusChange` ("Filed").
   - Stores it in the `complaints` map.
3. Returns the new `Complaint` object to the caller.

## 2. Checking status

1. Anyone (User or Admin) calls `tracker.getStatus(complaintId)` or
   `tracker.getComplaint(complaintId)` for full detail.
2. Tracker looks up by ID and returns current status + history.

## 3. Filtering / sorting (admin dashboard view)

1. Admin calls `tracker.filterByPriority(HIGH)` to see urgent items first, or
   `tracker.sortByPriority()` / `sortByDateFiled()` for a full ordered view.

## 4. Escalation check

1. Triggered explicitly — either from a console menu option ("Run escalation
   scan") or automatically once per session/demo run.
2. `checkAndEscalate()` loops through all `OPEN`/`IN_PROGRESS` complaints.
3. For each, compares `getDaysSinceFiled()` against the priority's threshold.
4. If overdue:
   - Status → `ESCALATED`.
   - History gets a new entry noting how overdue it was.
   - (Optional) `assignedAdmin` reassigned to a `SeniorAdmin`.
5. Returns the list of newly escalated complaints (for a notification/report).

## 5. Admin resolution

1. `Admin` calls `updateStatus(complaint, IN_PROGRESS, note)` while working it.
2. Once resolved, `Admin` calls `closeComplaint(complaint, resolutionNote)`.
3. Status → `RESOLVED` or `CLOSED`, with the final note in history.

## Full lifecycle diagram

```
        fileComplaint()
User ─────────────────────▶ OPEN
                              │
              Admin picks up  │
                              ▼
                         IN_PROGRESS
                              │
        checkAndEscalate()    │   updateStatus()/closeComplaint()
     (if overdue)             │
                              ▼
                         ESCALATED ──▶ SeniorAdmin ──▶ RESOLVED/CLOSED
                              │
                (if resolved in time)
                              ▼
                         RESOLVED/CLOSED
```

## Simulating time for the demo

Since escalation depends on elapsed days, the demo/test class should let you
**backdate** a complaint's `dateFiled` (e.g., a package-private setter or a
constructor overload used only in tests) so you can show escalation firing
without literally waiting several days. This is standard practice for
demonstrating time-based logic in a class assignment.
