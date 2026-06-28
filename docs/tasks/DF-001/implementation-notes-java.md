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
