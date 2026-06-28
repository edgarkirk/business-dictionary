# Implementation Notes — DF-001 Business Dictionary

---

## Layer: full-implementation

**What you implemented:**
- `pom.xml` — Spring Boot 3.4.1, Spring Data JPA, H2, Flyway, ArchUnit, Validation
- `domain/BusinessTerm.java` — JPA entity with `normalized_term` (lowercase) for case-insensitive uniqueness
- `persistence/BusinessTermRepository.java` — `JpaRepository` + `findByNormalizedTerm`
- `application/DictionaryService.java` (interface) + `DictionaryServiceImpl.java` — create/read/update; catches `DataIntegrityViolationException` → `DuplicateTermException`
- `application/exception/TermNotFoundException.java`, `DuplicateTermException.java`
- `api/DictionaryController.java` — `POST /api/v1/dictionary/terms`, `GET /api/v1/dictionary/terms/{term}`, `PUT /api/v1/dictionary/terms/{term}`
- `api/request/CreateTermRequest.java`, `UpdateTermRequest.java` — Bean Validation (`@NotBlank`, `@Size`)
- `api/response/TermResponse.java` (record), `ErrorResponse.java` (record)
- `config/JpaAuditingConfig.java` — `@EnableJpaAuditing`
- `config/GlobalExceptionHandler.java` — maps `TermNotFoundException`→404, `DuplicateTermException`→409, `MethodArgumentNotValidException`→400, `Exception`→500
- `BusinessDictionaryApplication.java` — entry point
- `resources/application.yaml` — PostgreSQL datasource, Flyway enabled
- `resources/db/migration/V1__create_business_dictionary_table.sql` — creates `business_dictionary` table + unique index on `normalized_term`
- `test/resources/application.yaml` — H2 in-memory datasource, Flyway enabled
- Tests: `DictionaryControllerTest` (@WebMvcTest, 9 tests), `BusinessTermRepositoryTest` (@DataJpaTest, 6 tests), `DictionaryServiceTest` (Mockito, 7 tests), `DictionaryAcceptanceTest` (@SpringBootTest, 10 tests), `ArchitectureTest` (ArchUnit, 4 tests) — **36 tests total, all green**

**Key decisions:**
- Case-insensitive uniqueness is enforced via a `normalized_term` column (stored as `term.toLowerCase()`), with a unique index. This avoids a `LOWER()` function index which is less portable.
- `DataIntegrityViolationException` from JPA is caught at the service layer and translated to `DuplicateTermException`, keeping the API layer free of persistence concerns.
- `@DataJpaTest` uses `replace=NONE` with H2 from test properties + explicit `spring.flyway.enabled=false`/`spring.jpa.hibernate.ddl-auto=create-drop` overrides to avoid Flyway classpath interference with schema auto-creation.
- ArchUnit domain isolation rule uses fully-qualified `com.epam.businessdictionary.*` package names to avoid false positives from `jakarta.persistence` (which also contains `persistence` as a segment).

**Deviations:**
- `application.yaml` uses PostgreSQL as the default datasource (as per architecture); H2 is used only in tests via `test/resources/application.yaml`.

**Known gaps:**
- No pagination on list endpoint (not in spec — GET by single term only).
- No authentication/authorization (not in scope for DF-001).

---

## Layer: full-implementation fix

**What you implemented:**
- `src/main/resources/db/migration/V1__create_business_dictionary_table.sql` — changed `created_at TIMESTAMP` and `updated_at TIMESTAMP` to `TIMESTAMP WITH TIME ZONE` to match ARCHITECTURE.md requirement for timezone-aware audit fields.

**Key decisions:**
- Minimal single-file change; no other files touched. H2 correctly maps `TIMESTAMP WITH TIME ZONE` → `timestamp(6) with time zone` in DDL (confirmed in test output). All 36 tests remain green.

**Deviations:** None.

**Known gaps:** None introduced by this fix.

---

## Layer: full-implementation fix #2 (Critic findings)

**What you implemented:**
- `api/DictionaryController.java` — added `public` modifier (was package-private)
- `application/DictionaryServiceImpl.java` — added `public` modifier (was package-private)
- `config/GlobalExceptionHandler.java` — added `public` modifier (was package-private)
- `config/JpaAuditingConfig.java` — added `auditingDateTimeProvider` bean (`DateTimeProvider`) that truncates `Instant.now()` to microsecond precision; wired via `@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")`

**Key decisions:**
- `BusinessTermRepository` and `JpaAuditingConfig` were already public — no change needed for those findings.
- Flyway test finding was a false positive: H2 2.x supports all SQL in `V1__create_business_dictionary_table.sql` (`UUID`, `TIMESTAMP WITH TIME ZONE`, unique index), so `spring.flyway.enabled: true` is correct and was left unchanged.
- The microsecond `DateTimeProvider` fixes `ac05_update_preserves_created_at_and_changes_updated_at`: Spring Data auditing now writes microsecond-precision `Instant` values, matching H2 `TIMESTAMP WITH TIME ZONE` storage precision and eliminating the 1-µs rounding mismatch between the create response (in-memory) and update response (DB-read).

**Deviations:** None.

**Known gaps:** None introduced by this fix.

