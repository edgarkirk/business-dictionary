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
