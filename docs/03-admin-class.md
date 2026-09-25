# Classes: `Admin` and `SeniorAdmin`

## `Admin extends User`

Authorized to update statuses and close complaints. Inherits `userId`,
`name`, `contactDetails` from `User`.

### Additional attributes

| Attribute | Type | Description |
|---|---|---|
| `adminLevel` | `String` or `boolean isSenior` | Distinguishes regular vs senior admins (or use subclassing instead — see below) |

### Methods

| Method | Returns | Purpose |
|---|---|---|
| `Admin(name, contactDetails)` | constructor | Calls `super(name, contactDetails)` |
| `updateStatus(Complaint complaint, Status newStatus, String note)` | `void` | Validates transition, calls `complaint.updateStatus()` |
| `closeComplaint(Complaint complaint, String resolutionNote)` | `void` | Sets status to `RESOLVED` or `CLOSED`, logs resolution note |
| `assignSelf(Complaint complaint)` | `void` | Sets `complaint.assignedAdmin = this` |

## `SeniorAdmin extends Admin`

Handles escalated complaints. No new attributes needed — the subclass itself
is the signal that this admin handles escalations.

### Additional methods

| Method | Returns | Purpose |
|---|---|---|
| `SeniorAdmin(name, contactDetails)` | constructor | Calls `super(name, contactDetails)` |
| `reviewEscalated(List<Complaint> escalatedList)` | `void` | Prints/reviews all complaints flagged `ESCALATED` |

## Why subclassing instead of a `isSenior` boolean flag?

Both are valid OOP choices:

- **Subclass (`SeniorAdmin extends Admin`)** — cleaner if senior admins have
  genuinely different *behavior* (e.g., only they can close `ESCALATED`
  complaints). Demonstrates inheritance more clearly for a class design
  assignment.
- **Flag (`boolean isSenior`)** — simpler if the only difference is a label,
  not behavior.

This design uses the **subclass approach** since the assignment explicitly
rewards demonstrating OOP structure. Let me know if you'd rather use the flag
version instead — it's a one-line change in the class diagram.
