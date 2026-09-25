# Class: `User`

Represents anyone who can file complaints and check their status. Base class
for `Admin`.

## Attributes

| Attribute | Type | Description |
|---|---|---|
| `userId` | `String` | Unique identifier (e.g., `USR-001`) |
| `name` | `String` | Full name |
| `contactDetails` | `String` | Email or phone |

## Methods

| Method | Returns | Purpose |
|---|---|---|
| `User(name, contactDetails)` | constructor | Auto-generates `userId` |
| `fileComplaint(tracker, type, description, priority)` | `Complaint` | Delegates to `ComplaintTracker.fileComplaint()`, passing `this` as filer |
| `checkStatus(tracker, complaintId)` | `Status` | Delegates to `ComplaintTracker.getStatus()` |
| `getUserId()`, `getName()`, `getContactDetails()` | — | Standard getters |

## Design note

`User` doesn't hold complaints itself — it just has convenience methods that
call into `ComplaintTracker`, which is the single source of truth for all
complaint data. This keeps `User` lightweight and avoids duplicating state.

## Inheritance

```
User
 └── Admin
      └── SeniorAdmin
```

`Admin` **is-a** `User` (can do everything a `User` can, plus manage
complaints) — see `03-admin-class.md`.
