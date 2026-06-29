# Implementation Notes — DF-001 Business Dictionary Service

---

## Layer: full-implementation

**What you implemented:**
- `pom.xml` — Spring Boot 3.3.5, JPA, Flyway, Validation, H2 (test), springdoc-openapi
- `V1__create_business_dictionary_table.sql` — `business_dictionary` table with unique index on `normalized_term`
- `BusinessDictionaryEntry` — JPA entity with UUID PK (`GenerationType.UUID`), `@CreatedDate`/`@LastModifiedDate` auditing
- `BusinessDictionaryRepository` — Spring Data JPA, `findByNormalizedTerm(String)` returning `Optional`
- `DictionaryService` / `DictionaryServiceImpl` — service interface + transactional implementation; uses `saveAndFlush` to surface `DataIntegrityViolationException` inline
- `CreateTermRequest`, `UpdateTermRequest` — immutable records with Bean Validation constraints
- `TermResponse`, `ErrorResponse` — immutable response records
- `DictionaryController` — `@Validated` REST controller under `/api/v1/dictionary/terms`
- `JpaAuditingConfig` — `@EnableJpaAuditing`
- `GlobalExceptionHandler` — maps `TermNotFoundException`→404, `DuplicateTermException`→409, `MethodArgumentNotValidException`→400
- 33 tests: 11 acceptance (`@SpringBootTest`), 9 controller (`@WebMvcTest`), 5 repository (`@DataJpaTest`), 8 service (Mockito unit)

**Key decisions:**
- `saveAndFlush()` used in service (create and update) so `DataIntegrityViolationException` is caught within the `@Transactional` boundary, not deferred to commit
- `term.toLowerCase(Locale.ROOT)` for portable case normalization independent of JVM locale
- `@DataJpaTest` uses `@AutoConfigureTestDatabase(replace = NONE)` + `@ActiveProfiles("test")` to run Flyway against H2 in PostgreSQL mode, validating migration SQL
- `JpaAuditingConfig` is package-private; tests rely on component scan rather than explicit `@Import` to avoid cross-package visibility issues

**Deviations:** None — implementation matches ARCHITECTURE.md and task.yaml exactly.

**Known gaps:** None. All three endpoints, all validation rules, all AC criteria covered.
