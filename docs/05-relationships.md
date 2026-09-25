# Class Relationships

## Inheritance (is-a)

```
User
 └── Admin
      └── SeniorAdmin
```

## Composition / Association (has-a)

```
ComplaintTracker "has many" Complaint        (1 --- *)
Complaint "filed by" User                    (* --- 1)
Complaint "assigned to" Admin                (* --- 0..1)
Complaint "has" StatusChange (history)       (1 --- *)
```

## Text-based UML summary

```
+------------------+          +---------------------+
|  ComplaintTracker |1------*|      Complaint        |
+------------------+          +---------------------+
| - complaints      |          | - complaintId        |
| - thresholds       |          | - type                |
+------------------+          | - description         |
| + fileComplaint()  |         | - priority: Priority   |
| + getStatus()       |         | - dateFiled            |
| + filterByPriority()|         | - status: Status       |
| + sortByPriority()  |         | - filedBy: User        |
| + checkAndEscalate()|         | - assignedAdmin: Admin |
+------------------+          | - statusHistory: List  |
                                +---------------------+
                                          | 1
                                          | *
                                +---------------------+
                                |    StatusChange       |
                                +---------------------+
                                | - status               |
                                | - timestamp             |
                                | - note                  |
                                +---------------------+

+-----------+
|   User     |
+-----------+
| - userId    |
| - name      |
| - contact   |
+-----------+
| + fileComplaint()|
| + checkStatus()  |
+-----------+
     ▲
     | extends
+-----------+
|   Admin    |
+-----------+
| + updateStatus()  |
| + closeComplaint()|
| + assignSelf()    |
+-----------+
     ▲
     | extends
+-----------------+
|  SeniorAdmin      |
+-----------------+
| + reviewEscalated()|
+-----------------+
```

## Why this shape

- **`ComplaintTracker` as the aggregator** keeps a single source of truth —
  no complaint data lives loose in `User` or `Admin` objects, avoiding sync
  bugs.
- **`Admin extends User`** avoids duplicating name/contact fields and lets
  an `Admin` be passed anywhere a `User` is expected (e.g., an admin could
  theoretically file a complaint too).
- **`StatusChange` as its own small class** (rather than a `String` log)
  keeps the audit trail structured and queryable, not just a text blob.
