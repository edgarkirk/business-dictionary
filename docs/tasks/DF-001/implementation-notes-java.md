# Implementation Notes — DF-001 Business Dictionary Service

## Layer: full-implementation

**What you implemented:**
- `pom.xml` — Spring Boot 3.3.4, Spring Data JPA, Flyway, Validation, SpringDoc OpenAPI, H2 (test), PostgreSQL (runtime)
- `BusinessDictionaryEntry` — JPA entity with UUID PK, `term`, `normalizedTerm`, `definition`, and audited `createdAt`/`updatedAt`
- `BusinessDictionaryRepository` — `JpaRepository` with `findByNormalizedTerm(String)`
- `DictionaryService` (interface) + `DictionaryServiceImpl` — create/get/update with `@Transactional`; uses `saveAndFlush` + catch `DataIntegrityViolationException` for duplicate detection
- `DictionaryController` — POST `/api/v1/dictionary/terms` (201), GET `/{term}` (200), PUT `/{term}` (200)
- `CreateTermRequest`, `UpdateTermRequest`, `TermResponse`, `ErrorResponse` — immutable Java records
- `GlobalExceptionHandler` — maps `TermNotFoundException` → 404, `DuplicateTermException` → 409, `MethodArgumentNotValidException` → 400
- `JpaAuditingConfig` — `@EnableJpaAuditing`
- `V1__create_business_dictionary_table.sql` — creates `business_dictionary` table with `ux_business_dictionary_normalized_term` unique index
- `application.yaml` (main), `application.properties` (test, H2 PostgreSQL mode)
- Tests: `DictionaryAcceptanceTest` (8), `DictionaryControllerTest` (9), `BusinessDictionaryRepositoryTest` (5), `DictionaryServiceTest` (6) — 28 total, all passing

**Key decisions:**
- Service returns `BusinessDictionaryEntry` domain objects; mapping to `TermResponse` is done in the controller layer via `TermResponse.from()`, keeping DTOs out of the service boundary.
- `saveAndFlush` used in `createTerm` and `updateTerm` to flush within the transaction and trigger the unique constraint immediately, enabling clean `DataIntegrityViolationException` catch.
- No `DEFAULT gen_random_uuid()` in SQL migration — JPA `GenerationType.UUID` handles ID generation at the application layer, keeping the migration H2-compatible.

**Deviations:** None — implementation matches ARCHITECTURE.md exactly.

**Known gaps:** None. All acceptance criteria (AC-01 through AC-06) are covered by tests.

> **Token usage for full-implementation layer**: 1819346 input, 34558 output, 1755160 cache read, 63872 cache write, 39 LLM calls (retries: 0)

---

## Token Usage Summary

### Per-Layer Breakdown

| Layer | Input | Output | Cache Read | Cache Write | LLM Calls | Retries |
|-------|------:|-------:|-----------:|------------:|----------:|--------:|
| full-implementation | 1,819,346 | 34,558 | 1,755,160 | 63,872 | 39 | 0 |

### Developer Agent Total
| Metric | Value |
|--------|------:|
| Input tokens | 1,819,346 |
| Output tokens | 34,558 |
| Cache read tokens | 1,755,160 |
| Cache write tokens | 63,872 |
| LLM calls | 39 |
