# Implementation Notes — DF-001 Business Dictionary Service API

## Layer: full-implementation

**What you implemented:**
- `pom.xml` — Spring Boot 3.3.5, Java 21, Web, Data JPA, Flyway, Validation, H2 (test), springdoc-openapi
- `domain/BusinessDictionaryEntry.java` — JPA entity with UUID PK, `@CreatedDate`/`@LastModifiedDate` auditing, `updateDefinition()` mutator
- `persistence/BusinessDictionaryRepository.java` — Spring Data JPA; `findByNormalizedTerm(String)` query method
- `application/BusinessDictionaryService.java` — service interface
- `application/BusinessDictionaryServiceImpl.java` — `@Transactional(readOnly=true)` class-level, `@Transactional` on write methods; duplicate detection via `DataIntegrityViolationException` catch (no TOCTOU race)
- `application/exception/TermNotFoundException.java`, `DuplicateTermException.java` — typed business exceptions
- `api/request/CreateTermRequest.java`, `UpdateDefinitionRequest.java` — Java records with Bean Validation annotations
- `api/response/TermResponse.java`, `ErrorResponse.java` — Java records
- `api/TermController.java` — `@Validated` controller; POST/GET/PUT endpoints at `/api/v1/dictionary/terms`
- `config/JpaAuditingConfig.java` — `@EnableJpaAuditing`; `config/GlobalExceptionHandler.java` — `@RestControllerAdvice` mapping to typed HTTP responses
- `resources/application.yaml` — H2 (main config for tests); `db/migration/V1__create_business_dictionary_table.sql` — table DDL with `normalized_term` unique index
- Tests: 11 acceptance (`@SpringBootTest`), 9 controller (`@WebMvcTest`), 6 repository (`@DataJpaTest`+nested `@TestConfiguration @EnableJpaAuditing`), 7 service (Mockito unit) — 33 total, all green

**Key decisions:**
- `normalized_term` stored as `lower(term)` in the entity constructor and kept in sync on every write; case-insensitive uniqueness enforced at DB level via unique index rather than application-level check
- `@DataJpaTest` uses a nested `@TestConfiguration @EnableJpaAuditing` instead of importing the package-private `JpaAuditingConfig` (package-private per standards.md)
- `Thread.sleep(10)` used only in auditing tests where a measurable `updatedAt` delta is required; all other tests avoid it

**Deviations:**
- Main `application.yaml` uses H2 (not PostgreSQL) because Docker is unavailable; the Flyway migration uses `TIMESTAMP WITH TIME ZONE` syntax which H2 in `MODE=PostgreSQL` accepts
- `createdAt` comparison in acceptance test uses `truncatedTo(ChronoUnit.MILLIS)` because H2 rounds sub-microsecond nanoseconds differently on read-back vs. the initial in-memory `Instant`

**Known gaps:**
- No OpenAPI `@Operation`/`@Tag` annotations added (standards.md recommends them but they are not required by `task.yaml`'s `definition_of_done`)
- Production deployment requires a real PostgreSQL datasource to be configured via environment variables / external config; the H2 config in `application.yaml` is test-only
