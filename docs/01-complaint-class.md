# Class: `Complaint`

Represents a single complaint filed by a `User`. This is the central data
entity of the system.

## Attributes

| Attribute | Type | Description |
|---|---|---|
| `complaintId` | `String` | Unique identifier, auto-generated (e.g., `CMP-0001`) |
| `type` | `String` | Category of complaint (e.g., "Billing", "Service", "Product Defect") |
| `description` | `String` | Free-text details of the complaint |
| `priority` | `Priority` (enum) | `LOW`, `MEDIUM`, `HIGH` |
| `dateFiled` | `LocalDate` | Date the complaint was created |
| `status` | `Status` (enum) | `OPEN`, `IN_PROGRESS`, `ESCALATED`, `RESOLVED`, `CLOSED` |
| `filedBy` | `User` | Reference to the user who filed it |
| `assignedAdmin` | `Admin` (nullable) | Admin currently handling it |
| `statusHistory` | `List<StatusChange>` | Full audit trail of status transitions |

## Supporting enums

```java
public enum Priority {
    LOW, MEDIUM, HIGH
}

public enum Status {
    OPEN, IN_PROGRESS, ESCALATED, RESOLVED, CLOSED
}
```

## Supporting helper class: `StatusChange`

| Attribute | Type | Description |
|---|---|---|
| `status` | `Status` | The status at this point in history |
| `timestamp` | `LocalDateTime` | When the change happened |
| `note` | `String` | Optional note (e.g., "Escalated: overdue by 3 days") |

## Methods

| Method | Returns | Purpose |
|---|---|---|
| `Complaint(type, description, priority, filedBy)` | constructor | Initializes complaint, sets `status = OPEN`, logs first history entry |
| `getStatus()` | `Status` | Returns current status |
| `updateStatus(Status newStatus, String note)` | `void` | Changes status, appends to `statusHistory` |
| `getDaysSinceFiled()` | `int` | `dateFiled` vs today, used by escalation logic |
| `getStatusHistory()` | `List<StatusChange>` | Returns full audit trail |
| `toString()` | `String` | Human-readable summary for console display |

## Notes

- `statusHistory` is **append-only** — nothing is ever overwritten, so the
  system can show a full timeline for any complaint.
- `assignedAdmin` starts `null` and is set when an `Admin` picks it up, or
  automatically reassigned to a `SeniorAdmin` on escalation.
