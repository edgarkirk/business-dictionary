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

---

## Layer: entity/domain

**What you implemented:**
- `domain/BusinessDictionaryEntry.java` — JPA entity mapped to `business_dictionary` table; UUID PK with `GenerationType.UUID`; `term` (VARCHAR 100), `normalized_term` (VARCHAR 100, unique constraint `ux_business_dictionary_normalized_term`), `definition` (VARCHAR 1000); `createdAt`/`updatedAt` via `@CreatedDate`/`@LastModifiedDate`; public `updateDefinition(String)` mutator for service-layer use; `protected` no-arg constructor per JPA contract.
- `config/JpaAuditingConfig.java` — `@Configuration @EnableJpaAuditing`; kept separate from `@SpringBootApplication` to avoid crashing `@WebMvcTest` slices.
- `db/migration/V1__create_business_dictionary_table.sql` — Flyway migration for PostgreSQL creating `business_dictionary` with `TIMESTAMP WITH TIME ZONE` columns and the named unique constraint.
- `domain/BusinessDictionaryEntryTest.java` — 7 `@DataJpaTest` tests via `TestEntityManager` covering: field persistence, audit timestamp population, `createdAt` immutability on update, `updateDefinition` mutation, unique-constraint enforcement, and NOT NULL enforcement for `term` and `definition`.

**Key decisions:**
- Used `Instant` (not `OffsetDateTime`) for audit timestamp fields. Spring Data's `AuditingEntityListener` only supports `Instant`, `LocalDateTime`, `Date`, and `Long` — `OffsetDateTime` is not in the conversion chain and causes `IllegalArgumentException` at persist time. `Instant` maps correctly to `TIMESTAMP WITH TIME ZONE` in PostgreSQL via Hibernate 6.
- `@Column(updatable = false)` on `created_at` ensures the column is excluded from UPDATE statements regardless of field value.

**Deviations:** ARCHITECTURE.md specifies `TIMESTAMP WITH TIME ZONE` columns — fulfilled in both the SQL migration and in Java via `Instant` (which Hibernate 6 maps to that PostgreSQL type). No other deviations.

**Known gaps:** Acceptance tests (10 failures) remain red at this layer — they require the full stack (repository, service, controller). Entity tests: `Tests run: 7, Failures: 0, Errors: 0` — GREEN confirmed.

---

## Layer: persistence/repository

**What you implemented:**
- `persistence/BusinessDictionaryEntryRepository.java` — Spring Data JPA interface extending `JpaRepository<BusinessDictionaryEntry, UUID>`; declares `findByNormalizedTerm(String)` → `Optional<BusinessDictionaryEntry>` for case-insensitive lookup, and `existsByNormalizedTerm(String)` → `boolean` for duplicate-check in the service layer.
- `persistence/BusinessDictionaryEntryRepositoryTest.java` — 7 `@DataJpaTest` tests: `findByNormalizedTerm` (found / not found), `existsByNormalizedTerm` (true / false), `save` sets both audit timestamps, duplicate `normalized_term` throws `DataIntegrityViolationException`, and `createdAt` is unchanged after `updateDefinition`.

**Key decisions:**
- `existsByNormalizedTerm` added alongside `findByNormalizedTerm`; lets the service issue a cheap existence check for 409-conflict detection without loading the full entity.
- Test for `createdAt` immutability reloads the entity from DB after the initial save (via `entityManager.clear()` + `entityManager.find()`) before capturing `originalCreatedAt`; this ensures both sides of the equality assertion share H2's microsecond precision rather than comparing in-memory nanoseconds against a DB-truncated value.

**Deviations:** None.

**Known gaps:** Acceptance tests (10 failures) remain red — require service and controller layers. Repository tests: `Tests run: 7, Failures: 0, Errors: 0` — GREEN confirmed.

---

## Layer: service

**What you implemented:**
- `application/exception/TermNotFoundException.java` — thrown when a term lookup or update finds no matching entry; maps to HTTP 404 in the upcoming controller advice.
- `application/exception/DuplicateTermException.java` — thrown when a create request conflicts with an existing normalized term; maps to HTTP 409.
- `application/DictionaryService.java` — service interface declaring `create`, `findByTerm`, and `update` operations returning `BusinessDictionaryEntry`; follows DIP so controllers depend on the interface, not the impl.
- `application/DictionaryServiceImpl.java` — `@Service @Transactional(readOnly = true)` implementation; `create` and `update` override with `@Transactional`; term normalization (`toLowerCase()`) happens here before every repository call.
- `application/DictionaryServiceTest.java` — 10 `@ExtendWith(MockitoExtension.class)` unit tests covering: successful create, duplicate detection with mixed casing, normalized-term persistence, case-insensitive find (found + not found), and case-insensitive update (success + not found).

**Key decisions:**
- Term normalization (`term.toLowerCase()`) is owned exclusively by the service layer; neither the controller nor the repository performs it.
- `DictionaryServiceImpl` is package-private — only the `DictionaryService` interface is part of the public API of the `application` package.
- `update` calls `repository.save(entry)` after mutation to ensure `@LastModifiedDate` is updated by the auditing listener on flush.

**Deviations:** None — implementation matches ARCHITECTURE.md layer contracts exactly.

**Known gaps:** Acceptance tests (10 failures) remain red — they require the controller layer. Service tests: `Tests run: 10, Failures: 0, Errors: 0` — GREEN confirmed.
