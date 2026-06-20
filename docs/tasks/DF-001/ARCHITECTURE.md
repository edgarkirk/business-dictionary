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

## 2. Requirement References

- `REQUIREMENTS.md`

## 3. Scope

Implement a Spring Boot backend service for creating, reading, and updating business dictionary terms.

The service must expose REST endpoints, validate input, persist terms in PostgreSQL, and enforce case-insensitive uniqueness.

## 4. Technology Stack

- Java 21
- Spring Boot 3.x
- Maven
- PostgreSQL
- Flyway
- Spring Web
- Spring Data JPA
- Jakarta Bean Validation
- JUnit 5
- Mockito
- Testcontainers

## 5. Architecture Pattern

Use standard layered architecture:

```text
Controller -> Service -> Repository -> Database
```

Layer responsibilities:

| Layer | Responsibility |
|---|---|
| Controller | HTTP request handling, validation trigger, response mapping |
| Service | Business rules, transaction boundaries, orchestration |
| Repository | Persistence access |
| Domain | Persistence entity and domain data representation |
| Config | Cross-cutting REST exception handling |

## 6. Package Structure

```text
com.epam.businessdictionary
├── api
│   ├── DictionaryController.java
│   ├── CreateTermRequest.java
│   ├── UpdateDefinitionRequest.java
│   ├── DictionaryTermResponse.java
│   └── ErrorResponse.java
├── application
│   ├── DictionaryService.java
│   ├── DuplicateTermException.java
│   └── TermNotFoundException.java
├── domain
│   └── DictionaryEntry.java
├── infrastructure
│   └── DictionaryRepository.java
├── config
│   └── RestExceptionHandler.java
└── BusinessDictionaryApplication.java
```

## 7. API Contract

Base path:

```text
/api/v1/dictionary
```

| Method | Path | Success | Errors | Description |
|---|---|---:|---|---|
| POST | `/terms` | 201 | 400, 409 | Create term |
| GET | `/terms/{term}` | 200 | 404 | Read term by name |
| PUT | `/terms/{term}` | 200 | 400, 404 | Update definition |

## 8. Request and Response Models

### CreateTermRequest

```json
{
  "term": "Bounded Context",
  "definition": "A boundary within which a domain model is consistent."
}
```

### UpdateDefinitionRequest

```json
{
  "definition": "Updated business definition."
}
```

### DictionaryTermResponse

```json
{
  "id": "2e41580a-4369-4f7d-9a20-d4d8f0b87a68",
  "term": "Bounded Context",
  "definition": "A boundary within which a domain model is consistent.",
  "createdAt": "2026-06-20T12:00:00Z",
  "updatedAt": "2026-06-20T12:00:00Z"
}
```

### ErrorResponse

```json
{
  "code": "DUPLICATE_TERM",
  "message": "Term already exists"
}
```

Validation errors should use this shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "term",
      "message": "must not be blank"
    }
  ]
}
```

## 9. Database Design

Table:

```text
business_dictionary
```

| Column | Type | Constraints |
|---|---|---|
| id | UUID | primary key |
| term | VARCHAR(100) | not null |
| normalized_term | VARCHAR(100) | not null, unique |
| definition | VARCHAR(1000) | not null |
| created_at | TIMESTAMP WITH TIME ZONE | not null |
| updated_at | TIMESTAMP WITH TIME ZONE | not null |

Migration path:

```text
src/main/resources/db/migration/V1__create_business_dictionary_table.sql
```

## 10. Validation Rules

### term

- Required.
- Must not be blank.
- Maximum length: 100 characters.

### definition

- Required.
- Must not be blank.
- Maximum length: 1000 characters.

## 11. Business Logic

### Create Term

1. Validate request.
2. Normalize term for uniqueness check.
3. Reject if normalized term already exists.
4. Create dictionary entry.
5. Set `createdAt` and `updatedAt`.
6. Persist entry.
7. Return created response.

### Read Term

1. Normalize term from path variable.
2. Search by normalized term.
3. Return `404 Not Found` if missing.
4. Return dictionary term response if found.

### Update Definition

1. Normalize term from path variable.
2. Search by normalized term.
3. Return `404 Not Found` if missing.
4. Update definition.
5. Update `updatedAt`.
6. Persist entry.
7. Return updated response.

## 12. Error Handling

| Scenario | HTTP Status | Error Code |
|---|---:|---|
| Invalid request | 400 | VALIDATION_ERROR |
| Duplicate term | 409 | DUPLICATE_TERM |
| Term not found | 404 | TERM_NOT_FOUND |
| Unexpected error | 500 | INTERNAL_ERROR |

## 13. Observability

Required logs:

- INFO when term creation starts.
- INFO when term is created.
- INFO when term lookup succeeds.
- INFO when definition is updated.
- WARN when duplicate term is rejected.
- WARN when requested term is not found.

Do not log sensitive data.

## 14. Testing Requirements

### DictionaryServiceTest

Required cases:

- Creates term successfully.
- Rejects duplicate term case-insensitively.
- Reads existing term.
- Updates definition.
- Returns not found for missing term.

### DictionaryControllerTest

Required cases:

- Returns `201 Created` for valid create request.
- Returns `409 Conflict` for duplicate term.
- Returns `200 OK` for existing term lookup.
- Returns `404 Not Found` for missing term lookup.
- Returns `200 OK` for valid update request.
- Returns `400 Bad Request` for invalid request.

### DictionaryRepositoryTest

Required cases:

- Persists dictionary entry.
- Finds entry by normalized term.
- Enforces unique normalized term.

## 15. Constraints

- Do not implement delete endpoint.
- Do not implement list endpoint.
- Do not implement authentication.
- Do not implement role management.
- Do not modify deployment, Kubernetes, Helm, or CI files.

## 16. Architecture Decisions

- Use `normalized_term` for case-insensitive uniqueness.
- Use UUID as primary key.
- Use `TIMESTAMP WITH TIME ZONE` for timestamps.
- Use Flyway for database migration.
- Use service-layer transaction boundaries.
- Use custom exceptions for business errors.
- Use a global REST exception handler.
- Keep generated service package independent from the factory implementation.

## 17. Definition of Done

- Required endpoints are implemented.
- Database migration is added.
- Validation rules are enforced.
- Error handling is implemented.
- Required tests are added.
- Build passes.
- Pull request is opened.
