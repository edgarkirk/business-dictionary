# Implementation Notes — Business Dictionary Service (Java)

---

## Layer: acceptance

**What you implemented:**
- `pom.xml` — Spring Boot 3.3.0 parent POM; dependencies: web, data-jpa, validation, flyway-core, flyway-database-postgresql, postgresql (runtime), springdoc-openapi-starter-webmvc-ui, h2 (test), spring-boot-starter-test, archunit-junit5 (test).
- `BusinessDictionaryApplication.java` — minimal `@SpringBootApplication` bootstrap class (required for `@SpringBootTest` context loading).
- `src/main/resources/application.yaml` — production config (PostgreSQL, Flyway enabled, ddl-auto=validate).
- `src/test/resources/application.yaml` — test override (H2 in-memory, Flyway disabled, ddl-auto=create-drop).
- `BusinessDictionaryAcceptanceTest.java` — 14 `@SpringBootTest(RANDOM_PORT)` tests using `TestRestTemplate`; covers all 6 ACs and every documented error path.

**Key decisions:**
- Tests use `Map<String, Object>` for request/response bodies to avoid referencing production classes that do not exist yet.
- Unique business-domain term names per test (Agile, Scrum, Kanban, …) to prevent state collision when a shared H2 context is reused across tests.
- H2 `MODE=PostgreSQL` in the JDBC URL to improve SQL dialect compatibility when Flyway migrations are re-enabled in later layers.
- `@SpringBootApplication` bootstrap class created as minimum required infrastructure (not a production business class); without it `@SpringBootTest` cannot locate the application context.

**Deviations:** None — all endpoints, base path, and field names match ARCHITECTURE.md and task.yaml exactly.

**Known gaps:**
- Flyway is disabled in the test profile; the persistence layer must re-enable it (or use `spring.flyway.locations` pointing at an H2-compatible migration) once the `V1__create_business_dictionary_table.sql` migration file is in place.
- `returns_404_for_missing_term` and `returns_404_when_updating_nonexistent_term` pass in RED state because Spring returns 404 for unmapped routes; they will continue to pass correctly once real endpoint handlers are implemented.

> **Token usage for acceptance layer**: 0 input, 0 output, 0 cache read, 0 cache write, 0 LLM calls (retries: 0)

---

## Layer: entity/domain

**What you implemented:**
- `BusinessDictionaryTerm.java` — JPA entity mapped to `business_dictionary` table; fields: `id` (UUID, generated), `term` (VARCHAR 100), `normalizedTerm` (VARCHAR 100, unique), `definition` (VARCHAR 1000), `createdAt` (TIMESTAMP WITH TIME ZONE, immutable), `updatedAt` (TIMESTAMP WITH TIME ZONE).
- `JpaAuditingConfig.java` — standalone `@Configuration @EnableJpaAuditing` class (per standards.md — not on `@SpringBootApplication`).
- `V1__create_business_dictionary_table.sql` — Flyway migration creating `business_dictionary` table and `ux_business_dictionary_normalized_term` unique index on `normalized_term`.
- `BusinessDictionaryTermTest.java` — 5 pure unit tests covering: term set, normalized_term lowercasing, definition set, id null before persistence, updateDefinition mutation.

**Key decisions:**
- `normalizedTerm` is set in the entity constructor via `term.toLowerCase()`, keeping normalization logic co-located with the entity.
- `updateDefinition(String)` method provided for the service layer to call without exposing a general setter.
- Protected no-arg constructor satisfies JPA requirement while preventing accidental misuse.
- `@CreatedDate` + `updatable = false` on `created_at` ensures immutability of the creation timestamp.

**Deviations:** None — table name, column names, types, and index name all match ARCHITECTURE.md and task.yaml exactly.

**Known gaps:**
- Flyway migration is PostgreSQL-only (uses `TIMESTAMP WITH TIME ZONE`); H2 test profile keeps `flyway.enabled=false` and relies on `ddl-auto=create-drop`, which is safe because H2 2.x with `MODE=PostgreSQL` handles `OffsetDateTime` correctly via Hibernate dialect.
- Acceptance tests remain in RED (12 of 14 fail) as expected — controllers, services, and repositories are not yet implemented.

> **Token usage for entity/domain layer**: 0 input, 0 output, 0 cache read, 0 cache write, 0 LLM calls (retries: 0)

---

## Layer: repository

**What you implemented:**
- `BusinessDictionaryTermRepository.java` — Spring Data JPA interface extending `JpaRepository<BusinessDictionaryTerm, UUID>` with one derived query method: `findByNormalizedTerm(String)` returning `Optional<BusinessDictionaryTerm>`.
- `JpaAuditingConfig.java` (modified) — added a `DateTimeProvider` bean (`offsetDateTimeProvider`) returning `OffsetDateTime.now()` and referenced it via `@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")`.
- `BusinessDictionaryTermRepositoryTest.java` — 6 `@DataJpaTest` tests: save + find by normalized term, empty result for unknown term, lookup is case-sensitive to stored normalized form, duplicate normalized term throws `DataIntegrityViolationException`, auditing fields set after persist, `createdAt` unchanged and definition updated after mutation.

**Key decisions:**
- Single custom query method `findByNormalizedTerm` derived from the field name — no custom JPQL needed; callers (service layer) are responsible for lowercasing the input before calling this method.
- `@Import(JpaAuditingConfig.class)` added explicitly to the test class for clarity; `@DataJpaTest` may pick it up automatically but explicit import removes ambiguity.
- `entityManager.clear()` called after each `saveAndFlush` in the update test to bypass the Hibernate L1 cache and verify DB-persisted values.

**Deviations:**
- `JpaAuditingConfig` was modified (previous layer) to add a `DateTimeProvider` that returns `OffsetDateTime`. This was required to fix a Spring Data auditing incompatibility: the default `TemporalDateTimeProvider` provides `LocalDateTime`, which cannot be auto-converted to `OffsetDateTime` in H2. This change is backward-compatible with PostgreSQL in production.

**Known gaps:**
- Acceptance tests remain RED (12 of 14 fail) — service and controller layers are not yet implemented.

> **Token usage for repository layer**: 0 input, 0 output, 0 cache read, 0 cache write, 0 LLM calls (retries: 0)

---

## Layer: service

**What you implemented:**
- `application/exception/TermNotFoundException.java` — `RuntimeException` thrown when a term lookup finds no match (maps to HTTP 404).
- `application/exception/DuplicateTermException.java` — `RuntimeException` thrown when a create request conflicts with an existing normalized term (maps to HTTP 409).
- `application/BusinessDictionaryService.java` — public interface with three methods: `createTerm`, `getTerm`, `updateTerm`.
- `application/BusinessDictionaryServiceImpl.java` — package-private `@Service @Transactional(readOnly = true)` implementation; write methods annotated with `@Transactional`; constructor injection of repository.
- `application/BusinessDictionaryServiceTest.java` — 8 Mockito unit tests covering happy paths and all typed exception paths for each method.

**Key decisions:**
- All lookups normalize the input via `term.toLowerCase()` before calling `findByNormalizedTerm`, so case-insensitivity is enforced uniformly in the service layer.
- `BusinessDictionaryServiceImpl` is package-private (no `public` modifier) — only the `BusinessDictionaryService` interface is public, enforcing DIP.
- `updateTerm` calls `repository.save(entity)` after `entity.updateDefinition(definition)` to ensure `@LastModifiedDate` is triggered by JPA auditing.

**Deviations:** None.

**Known gaps:**
- Acceptance tests remain RED (12 of 14 fail) — controller layer not yet implemented.

> **Token usage for service layer**: 0 input, 0 output, 0 cache read, 0 cache write, 0 LLM calls (retries: 0)
