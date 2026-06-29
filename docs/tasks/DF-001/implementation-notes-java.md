# Implementation Notes — DF-001

---

## Layer: full-implementation

**What you implemented:**
- `domain/BusinessDictionaryEntry.java` — JPA entity with `@EntityListeners(AuditingEntityListener.class)` for automatic `createdAt`/`updatedAt`; `normalizedTerm` stores `term.toLowerCase()` for case-insensitive uniqueness
- `persistence/BusinessDictionaryRepository.java` — Spring Data JPA repository with `findByNormalizedTerm` query
- `application/DictionaryService.java` + `DictionaryServiceImpl.java` — service interface + impl; `createTerm` uses `saveAndFlush` to trigger unique-constraint violation inside the try-catch for proper 409 mapping
- `application/exception/TermNotFoundException.java`, `DuplicateTermException.java` — typed domain exceptions
- `api/DictionaryController.java` — `POST /api/v1/dictionary/terms`, `GET /api/v1/dictionary/terms/{term}`, `PUT /api/v1/dictionary/terms/{term}` all package-private per standards
- `api/request/CreateTermRequest.java`, `UpdateTermRequest.java` — Java records with Bean Validation constraints
- `api/response/TermResponse.java`, `ErrorResponse.java` — immutable records; `TermResponse.from(entry)` static factory
- `config/JpaAuditingConfig.java` — `@EnableJpaAuditing`; `config/GlobalExceptionHandler.java` — maps `TermNotFoundException→404`, `DuplicateTermException→409`, `MethodArgumentNotValidException→400`
- `db/migration/V1__create_business_dictionary_table.sql` — Flyway migration creating `business_dictionary` table with `ux_business_dictionary_normalized_term` unique index
- Tests: `BusinessDictionaryAcceptanceTest` (10), `DictionaryControllerTest` (10), `BusinessDictionaryRepositoryTest` (5), `DictionaryServiceTest` (8) — all 33 pass

**Key decisions:**
- Used `saveAndFlush()` in `createTerm` (not `save()`) so the unique-constraint violation fires synchronously inside the try-catch; `save()` would buffer the INSERT until commit, making the exception unreachable.
- `GlobalExceptionHandler` and `JpaAuditingConfig` are package-private (config package) per coding standards; controller tests do not need `@Import` since `@WebMvcTest` auto-discovers `@RestControllerAdvice`.
- Case-insensitive uniqueness implemented via a separate `normalized_term` column (lowercase) with a unique index, rather than a DB function index, for H2/PostgreSQL compatibility.

**Deviations:** None. All ARCHITECTURE.md decisions followed as specified.

**Known gaps:** No `GET /api/v1/dictionary/terms` (list all) endpoint — not in REQUIREMENTS.md. No pagination support added; not required by spec.

> **Token usage for full-implementation layer**: 1695490 input, 28870 output, 1604563 cache read, 90539 cache write, 48 LLM calls (retries: 0)

---

## Token Usage Summary

### Per-Layer Breakdown

| Layer | Input | Output | Cache Read | Cache Write | LLM Calls | Retries |
|-------|------:|-------:|-----------:|------------:|----------:|--------:|
| full-implementation | 1,695,490 | 28,870 | 1,604,563 | 90,539 | 48 | 0 |

### Developer Agent Total
| Metric | Value |
|--------|------:|
| Input tokens | 1,695,490 |
| Output tokens | 28,870 |
| Cache read tokens | 1,604,563 |
| Cache write tokens | 90,539 |
| LLM calls | 48 |
