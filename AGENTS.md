# AGENTS.md — Business Dictionary Service

## Project Overview
- **Task**: DF-001 / DARK-101 — Greenfield Spring Boot REST API for a business dictionary
- **Language**: Java 21 | **Framework**: Spring Boot 3.3.5 | **Build**: Maven (`./mvnw`)
- **Package root**: `com.epam.businessdictionary`
- **Branch**: `ai/DF-001-business-dictionary` → PR #2 on `main`

## Architecture
Layered package structure — no circular dependencies:
```
api/           REST layer (controllers, request/response DTOs, error model)
application/   Business services and domain exceptions
domain/        JPA entities
infrastructure/Spring Data JPA repositories
config/        Cross-cutting config (RestExceptionHandler)
```

## Build & Test
```bash
./mvnw clean compile          # compile main sources
./mvnw test-compile           # compile test sources
./mvnw test -Dtest="DictionaryServiceTest,DictionaryControllerTest"   # unit + web tests (no Docker needed)
./mvnw clean verify           # full build + all tests
```

## Key Implementation Decisions
- **Case-insensitive uniqueness**: stored as `normalizedTerm = term.toLowerCase(Locale.ROOT).strip()` in a `UNIQUE` DB column; lookups also normalise the input.
- **UUID primary key**: generated via `@UuidGenerator` (Hibernate 6) — not `@GeneratedValue(strategy=AUTO)`.
- **Timestamps**: `OffsetDateTime` for `createdAt`/`updatedAt`; `createdAt` is set once in `@PrePersist`, `updatedAt` in both `@PrePersist` and `@PreUpdate`.
- **No delete / list**: explicitly excluded from scope.
- **Flyway**: single migration `V1__create_business_dictionary_table.sql` under `src/main/resources/db/migration/`.
- **H2 in-memory DB** for unit and controller tests; **Testcontainers/PostgreSQL** for `DictionaryRepositoryTest` (requires Docker).

## Error Codes
| HTTP | `code` field          | Trigger |
|------|-----------------------|---------|
| 400  | `VALIDATION_ERROR`    | Bean Validation failure |
| 404  | `TERM_NOT_FOUND`      | `TermNotFoundException` |
| 409  | `DUPLICATE_TERM`      | `DuplicateTermException` |
| 500  | `INTERNAL_ERROR`      | Unexpected exceptions |

## Endpoints
| Method | Path | HTTP success |
|--------|------|-------------|
| POST   | `/api/v1/dictionary/terms` | 201 Created |
| GET    | `/api/v1/dictionary/terms/{term}` | 200 OK |
| PUT    | `/api/v1/dictionary/terms/{term}` | 200 OK |

## Test Classes
| Class | Type | Count |
|-------|------|-------|
| `DictionaryServiceTest` | Mockito unit | 5 |
| `DictionaryControllerTest` | `@WebMvcTest` | 6 |
| `DictionaryRepositoryTest` | Testcontainers | 3 (Docker required) |

## Forbidden Paths (do not modify)
`.github/**`, `deployment/**`, `infrastructure/**`, `k8s/**`, `helm/**`

## Environment Notes
- OpenJDK 21.0.11 and Maven 3.9.9 pre-installed
- Docker **not available** in this sandbox — Testcontainers tests are skipped
- `GITHUB_TOKEN` is available for GitHub API calls
- Use `python3` + `urllib.request` for API calls (no `jq`, no `gh` CLI)
