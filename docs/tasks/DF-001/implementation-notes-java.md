# Implementation Notes — DF-001 Business Dictionary Service

## Layer: full-implementation

**What you implemented:**
- `BusinessDictionaryEntry` (JPA entity, `@PrePersist` normalises term to lowercase for `normalized_term`)
- `BusinessDictionaryRepository` (Spring Data JPA; `findByNormalizedTerm`)
- `JpaAuditingConfig` (`@EnableJpaAuditing`, made `public` for cross-package `@Import` in tests)
- `TermNotFoundException`, `DuplicateTermException`
- `DictionaryService` (interface) + `DictionaryServiceImpl` (catches `DataIntegrityViolationException` → `DuplicateTermException`)
- `DictionaryController` (package-private; `POST /api/v1/dictionary/terms`, `GET /…/{term}`, `PUT /…/{term}`)
- `CreateTermRequest`, `UpdateTermRequest`, `TermResponse`, `ErrorResponse` (Java records)
- `GlobalExceptionHandler` (`@RestControllerAdvice`; maps domain exceptions → RFC-style JSON error body)
- `V1__create_business_dictionary_table.sql` (Flyway migration; `normalized_term` unique index)
- Tests: `DictionaryServiceImplTest` (6), `BusinessDictionaryRepositoryTest` (5), `DictionaryControllerTest` (9), `DictionaryAcceptanceTest` (11) — 31 total, all green

**Key decisions:**
- Case-insensitive uniqueness is enforced at two levels: DB unique index on `normalized_term` (lowercase) + service-layer `DataIntegrityViolationException` guard, giving a clean 409 response.
- Spring Boot upgraded from 3.3.4 → 3.4.4 to satisfy the standards requirement for `@MockitoBean` (Spring Framework 6.2 API).
- H2 in `MODE=PostgreSQL` used for all test slices (no Docker/Testcontainers) per environment constraint.

**Deviations:**
- `JpaAuditingConfig` declared `public` (standards allow package-private) because `@DataJpaTest` slices require a cross-package `@Import` at compile time.

**Known gaps:**
- No paginated `GET /terms` list endpoint (not in REQUIREMENTS.md).
- Flyway `WARN` about H2 2.3.x being newer than supported 2.2.x — cosmetic only; all migrations run successfully.

> **Token usage for full-implementation layer**: 2148675 input, 33272 output, 2013919 cache read, 134360 cache write, 49 LLM calls (retries: 0)

---

## Token Usage Summary

### Per-Layer Breakdown

| Layer | Input | Output | Cache Read | Cache Write | LLM Calls | Retries |
|-------|------:|-------:|-----------:|------------:|----------:|--------:|
| full-implementation | 2,148,675 | 33,272 | 2,013,919 | 134,360 | 49 | 0 |

### Developer Agent Total
| Metric | Value |
|--------|------:|
| Input tokens | 2,148,675 |
| Output tokens | 33,272 |
| Cache read tokens | 2,013,919 |
| Cache write tokens | 134,360 |
| LLM calls | 49 |
