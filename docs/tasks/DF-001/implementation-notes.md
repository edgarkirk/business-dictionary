# DF-001 Implementation Notes

---

## Layer: acceptance

**What you implemented:**
- `pom.xml` — Spring Boot 3.3.0 parent; dependencies: web, data-jpa, validation, flyway-core, flyway-database-postgresql, springdoc-openapi 2.5.0, postgresql (runtime), h2 (test), spring-boot-starter-test, archunit-junit5 1.3.0.
- `BusinessDictionaryApplication.java` — minimal `@SpringBootApplication` bootstrap (required for `@SpringBootTest` context; not a feature class).
- `src/main/resources/application.yaml` — PostgreSQL datasource + Flyway enabled.
- `src/test/resources/application-test.yaml` — H2 in-memory datasource, Flyway disabled (Docker unavailable).
- `BusinessDictionaryAcceptanceTest.java` — 12 `@SpringBootTest(RANDOM_PORT)` tests covering all 6 ACs: AC-01 (201 on create), AC-02 (409 on case-insensitive duplicate), AC-03 (200 on read), AC-04 (404 on missing read), AC-05 (200 on update + 404 on missing update), AC-06 (400 for blank/oversized term and definition — 6 sub-cases).

**Key decisions:**
- Used `TestRestTemplate` with explicit `Content-Type: application/json` headers; avoids form-encoding for POST/PUT bodies.
- Raw `Map<String, Object>` responses keep tests free of production DTOs that don't exist yet.
- H2 with `ddl-auto: create-drop` and Flyway disabled; once entities are added, Hibernate will create the schema automatically for tests.

**Deviations:** None — tests are strictly derived from REQUIREMENTS.md and ARCHITECTURE.md. No production feature classes created.

**Known gaps:**
- AC-04 (`reads_missing_term_returns_404`) and `updates_nonexistent_term_returns_404` accidentally pass in RED phase because Spring returns 404 for any unmapped route — these will continue to pass once the controller is implemented correctly.
- `updatedAt` change assertion (AC-05) verifies field presence only; precise before/after timestamp comparison is deferred to service-layer unit tests.
- `mvn clean verify` result: `Tests run: 12, Failures: 10, Errors: 0` — RED confirmed.
