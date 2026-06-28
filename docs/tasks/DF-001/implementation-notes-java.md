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
