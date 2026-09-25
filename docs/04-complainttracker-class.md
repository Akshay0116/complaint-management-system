# Class: `ComplaintTracker`

The core engine of the system. Holds all complaints and provides every
operation for filing, querying, sorting, and escalating them. This is where
most of the "business logic" lives, keeping `Complaint`, `User`, and `Admin`
focused purely on data + identity.

## Attributes

| Attribute | Type | Description |
|---|---|---|
| `complaints` | `Map<String, Complaint>` | All complaints, keyed by `complaintId`, for O(1) lookup |
| `escalationThresholds` | `Map<Priority, Integer>` | Days-until-overdue per priority (e.g., HIGH→2, MEDIUM→5, LOW→10) |
| `idCounter` | `int` | Used to auto-generate sequential complaint IDs |

## Methods

| Method | Returns | Purpose |
|---|---|---|
| `fileComplaint(String type, String description, Priority priority, User filedBy)` | `Complaint` | Creates a new `Complaint`, assigns ID, stores it, returns it |
| `getStatus(String complaintId)` | `Status` | Looks up and returns current status |
| `getComplaint(String complaintId)` | `Complaint` | Returns the full object (for detail view) |
| `filterByPriority(Priority p)` | `List<Complaint>` | Returns all complaints matching that priority |
| `sortByPriority()` | `List<Complaint>` | Returns all complaints, HIGH → LOW |
| `sortByDateFiled()` | `List<Complaint>` | Returns all complaints, oldest first |
| `checkAndEscalate()` | `List<Complaint>` | Scans all `OPEN`/`IN_PROGRESS` complaints; escalates overdue ones; returns the list of newly escalated complaints |
| `getAllComplaints()` | `List<Complaint>` | Returns everything (for admin dashboard view) |

## Why `ComplaintTracker` owns escalation logic (not `Complaint` itself)

`Complaint` could, in principle, know its own overdue status
(`getDaysSinceFiled()` lives there). But the **decision** to escalate, and
the **side effect** of reassigning to a `SeniorAdmin`, involves comparing
across the whole threshold policy and touching the admin pool — that's
system-level behavior, not something a single data object should be
responsible for. This keeps `Complaint` a clean data class and
`ComplaintTracker` the single place business rules live.

## Example method sketch

```java
public List<Complaint> checkAndEscalate() {
    List<Complaint> newlyEscalated = new ArrayList<>();
    for (Complaint c : complaints.values()) {
        if (c.getStatus() == Status.OPEN || c.getStatus() == Status.IN_PROGRESS) {
            int threshold = escalationThresholds.get(c.getPriority());
            if (c.getDaysSinceFiled() > threshold) {
                c.updateStatus(Status.ESCALATED,
                    "Escalated: overdue by " + (c.getDaysSinceFiled() - threshold) + " day(s)");
                newlyEscalated.add(c);
            }
        }
    }
    return newlyEscalated;
}
```
