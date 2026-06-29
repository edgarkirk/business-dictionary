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
