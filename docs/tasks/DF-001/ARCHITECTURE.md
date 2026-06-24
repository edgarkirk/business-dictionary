# Architecture Package: Business Dictionary Service

## 1. Metadata

| Field | Value |
|---|---|
| Task ID | DF-001 |
| Jira Story | DARK-101 |
| Service Name | business-dictionary |
| Architecture Type | Greenfield Spring Boot Backend Service |
| Owner | Architecture Agent |
| Status | Ready for Development |

## 2. References

- Business requirements: `REQUIREMENTS.md`
- Machine-readable contract (components, API, DB schema, test scenarios): `task.yaml`

## 3. Scope

REST backend service for creating, reading, and updating business dictionary terms. Single deployable Spring Boot application backed by PostgreSQL. No UI, no authentication in this scope.

## 4. Technology Stack

| Concern | Choice |
|---|---|
| Runtime | Java 21, Spring Boot 3.x |
| Build | Maven |
| Database | PostgreSQL |
| Schema versioning | Flyway |
| Persistence | Spring Data JPA |
| Input validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito, Testcontainers |

## 5. Layer Architecture

Standard layered architecture with strict one-direction dependency flow:

```
REST Client → Controller → Service → Repository → Database
```

| Layer | Responsibility |
|---|---|
| Controller | Accept HTTP requests, trigger validation, delegate to Service, map responses |
| Service | Enforce business rules, own transaction boundaries, throw typed business exceptions |
| Repository | Persistence operations only — no business logic |
| Domain | Data entity — no dependencies on any application layer |
| Config | Cross-cutting concerns: REST exception mapping, JPA auditing |

**Dependency rules:**
- Controllers depend on Service interfaces, never on Repository types directly.
- Services depend on Repository interfaces, never on Controller types.
- Domain has no dependencies on Service, Controller, or Config layers.
- Request/response DTOs do not cross the service boundary — the service operates on domain objects.

## 6. API Surface

Base path: `/api/v1/dictionary`

| Method | Path | Success | Client Errors |
|---|---|---:|---|
| POST | `/terms` | 201 Created | 400 invalid input, 409 duplicate term |
| GET | `/terms/{term}` | 200 OK | 404 not found |
| PUT | `/terms/{term}` | 200 OK | 400 invalid input, 404 not found |

Exact request/response field definitions are in `task.yaml`.

## 7. Data Model

A single table stores dictionary entries. Each entry holds the original term alongside a normalized form used for case-insensitive uniqueness enforcement and lookup. Both values are persisted; lookup is always by normalized form.

Exact column definitions, types, and migration path are in `task.yaml`.

## 8. Architecture Decisions

| Decision | Rationale |
|---|---|
| Layered architecture | Clear separation of concerns; each layer is independently testable |
| Separate `normalized_term` column | Decouples uniqueness and lookup from database collation; portable and explicit |
| UUID primary key | Avoids sequential ID exposure; safe for distributed or replicated environments |
| `TIMESTAMP WITH TIME ZONE` for audit fields | Timezone-aware; eliminates conversion bugs across environments |
| Flyway for schema migrations | Version-controlled, reproducible, and rollback-capable schema changes |
| Service defined as interface + implementation | Enables controller-layer mocking in tests; enforces dependency inversion |
| Global REST exception handler | Single mapping point from domain errors to HTTP responses; keeps service layer free of HTTP concerns |
| Typed business exceptions | Explicit domain error semantics; exception type drives HTTP status mapping without string matching |

## 9. Constraints

- No delete endpoint.
- No list or search endpoint.
- No authentication or authorization.
- No role management.
- Do not modify deployment, Kubernetes, Helm, or CI configuration.

## 10. Definition of Done

- All three endpoints are implemented and return the correct HTTP status codes.
- Case-insensitive uniqueness is enforced at the database level via the normalized term.
- Input validation rejects invalid requests with HTTP 400.
- All typed business exceptions map to the documented HTTP error codes.
- Database migration exists and is applied on startup.
- All tests pass with the configured build command.
- Pull request is opened against the base branch.