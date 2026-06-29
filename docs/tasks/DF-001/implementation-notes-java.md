# Implementation Notes — DF-001 Business Dictionary Service

## Layer: full-implementation

**What you implemented:**
- `BusinessDictionaryEntry` entity (UUID PK, JPA auditing, `updateDefinition` method)
- `BusinessDictionaryRepository` (Spring Data JPA, `findByNormalizedTerm`)
- `DictionaryService` interface + `DictionaryServiceImpl` (create/read/update, case-insensitive via `toLowerCase()`)
- `DictionaryController` (REST endpoints `POST /terms`, `GET /terms/{term}`, `PUT /terms/{term}`)
- `GlobalExceptionHandler` (`@RestControllerAdvice` — 404, 409, 400, 500)
- `JpaAuditingConfig` (`@EnableJpaAuditing`)
- `V1__create_business_dictionary_table.sql` (Flyway migration — table + unique index on `normalized_term`)
- `application.yaml` (PostgreSQL defaults), `application-test.properties` (H2 in PostgreSQL mode)
- Tests: `DictionaryAcceptanceTest` (`@SpringBootTest`), `BusinessDictionaryRepositoryTest` (`@SpringBootTest(webEnvironment=NONE)`), `DictionaryServiceTest` (Mockito), `DictionaryControllerTest` (`@WebMvcTest`) — 31 tests total, all green

**Key decisions:**
- Case-insensitivity enforced by storing `normalizedTerm = term.toLowerCase()` and querying by that column; original casing of `term` is preserved in the response.
- `DataIntegrityViolationException` from the DB unique index is caught in the service and re-thrown as `DuplicateTermException` (409).
- `GlobalExceptionHandler` lives in `config` package and is package-private per the coding standards.

**Deviations:**
- Repository tests use `@SpringBootTest(webEnvironment=NONE)` instead of `@DataJpaTest`. Reason: `@DataJpaTest` slices exclude Flyway autoconfiguration, and importing `FlywayAutoConfiguration` in a slice test causes a Hibernate schema-validation ordering failure (table not yet created when `EntityManagerFactory` initialises). Full-context `@SpringBootTest(webEnvironment=NONE)` guarantees correct Flyway → Hibernate ordering while still testing only the persistence layer.

**Known gaps:**
- No DELETE endpoint (not in scope per REQUIREMENTS.md).
- Production `application.yaml` datasource credentials are placeholders; should be externalised via environment variables in deployment.
