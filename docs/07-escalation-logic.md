# Escalation Logic — Deep Dive

## The rule

A complaint auto-escalates if it has been in `OPEN` or `IN_PROGRESS` status
for longer than its priority's allowed window:

| Priority | Escalation threshold |
|---|---|
| HIGH | 2 days |
| MEDIUM | 5 days |
| LOW | 10 days |

These are stored in `ComplaintTracker.escalationThresholds` as a
`Map<Priority, Integer>`, so they're configurable without touching logic
code — just change the map values (or load them from a config file if you
want to extend this later).

## Trigger model: explicit vs automatic

Two designs were considered:

1. **Explicit trigger (recommended)** — a menu action or scheduled call
   invokes `tracker.checkAndEscalate()`. Simple, predictable, easy to test
   and demo (call it, show the before/after).
2. **Automatic on every access** — every time `getStatus()` or
   `filterByPriority()` is called, the tracker silently re-checks escalation
   first. Feels more "real-time" but makes behavior harder to test
   deterministically, and adds hidden side effects to read-only methods
   (bad practice — a getter shouldn't mutate state).

**This design uses the explicit trigger.** It matches good OOP practice
(no surprise mutations from a "check" method) and is far easier to write
clean test cases for.

## What happens on escalation

1. `Complaint.status` → `ESCALATED`.
2. A new `StatusChange` is appended: `"Escalated: overdue by N day(s)"`.
3. (Optional extension) `assignedAdmin` is reassigned from a regular `Admin`
   to a `SeniorAdmin`, simulating routing to a higher authority.
4. The complaint remains visible in `getAllComplaints()` and
   `filterByPriority()` like any other — `ESCALATED` is just another status
   value, not a separate list.

## Edge cases to handle

- A complaint already `RESOLVED`/`CLOSED` is skipped — no re-escalation.
- A complaint already `ESCALATED` is skipped on subsequent scans — it won't
  double-log escalation notes.
- If `escalationThresholds` doesn't have an entry for a given priority
  (shouldn't happen with an enum, but defensively), default to the `LOW`
  threshold rather than throwing.
