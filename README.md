# Business Dictionary Service

Business Dictionary Service provides centralized management of business terms and definitions.

## Purpose

The service helps teams use consistent corporate terminology by storing approved business terms and their definitions.

## Initial Scope

- Create a business term.
- Retrieve a business term by name.
- Update the definition of an existing term.

## Technology Stack

- Java 21
- Spring Boot 3.x
- Maven
- PostgreSQL
- Flyway

## API

Base path:

```text
/api/v1/dictionary
```

Initial endpoints:

| Method | Path | Description |
|---|---|---|
| POST | `/terms` | Create a term |
| GET | `/terms/{term}` | Retrieve a term by name |
| PUT | `/terms/{term}` | Update a term definition |

## Documentation

Task-specific requirements and architecture documents are stored under:

```text
docs/tasks/
```

Current task package:

```text
docs/tasks/DF-001/
```
