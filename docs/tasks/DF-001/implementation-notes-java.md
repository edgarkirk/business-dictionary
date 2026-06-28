# Implementation Notes — DF-001 Business Dictionary Service

---

## Layer: acceptance

**What you implemented:**
- `pom.xml` — Spring Boot 3.3.4, Java 21, dependencies: web, data-jpa, validation, flyway-core, flyway-database-postgresql, postgresql (runtime), springdoc-openapi, h2 (test), spring-boot-starter-test, archunit-junit5.
- `BusinessDictionaryApplication.java` — minimal `@SpringBootApplication` scaffold (no business logic).
- `src/main/resources/application.yaml` — production datasource pointing at PostgreSQL; Flyway enabled.
- `src/test/resources/application.yaml` — H2 in-memory datasource; Flyway disabled; H2Dialect; ddl-auto: none.
- `src/main/resources/db/migration/.gitkeep` — placeholder keeping the migration directory tracked.
- `DictionaryApiAcceptanceTests.java` — 18 `@SpringBootTest(RANDOM_PORT)` tests via `TestRestTemplate` covering all six acceptance criteria and every documented error path.

**Key decisions:**
- Used `WebEnvironment.RANDOM_PORT` + `TestRestTemplate` for true end-to-end HTTP coverage (includes servlet filters, error handling, serialization).
- H2 is used instead of Testcontainers because Docker is unavailable in this environment.
- Flyway is disabled in test config; the migration SQL will be introduced in the persistence layer.
- AC-04 and AC-05/404 tests additionally assert `"code"` and `"message"` keys in the error body, ensuring they fail properly in RED (Spring's default 404 body does not contain those keys).

**Deviations:** None — no production classes were created.

**Known gaps:** Tests for AC-03/AC-05 that involve creating a term first will fail at setup in RED because the POST endpoint does not exist. This is intentional and correct for the RED phase.

> **Token usage for acceptance layer**: 0 input, 0 output, 0 cache read, 0 cache write, 0 LLM calls (retries: 0)
