# AGENTS.md — Repository Guidelines (Merged)

> **Precedence rule:** This file is the **source of truth**. If any older docs (e.g., `CORE_RULES.md`) conflict with this file, **follow this file**.

---

## 0) Tech Stack & Baselines

* Java **21**
* Spring Boot **3.4.x**
* Spring Data JPA (Hibernate)
* Database: Oracle (follow project config)
* Base package: `vn.com.viettel`

---

## 1) Project Structure & Module Organization

### Source layout

* Source code: `src/main/java/vn/com/viettel` organized by layer:

  * `controllers`, `services`, `repositories`, `entities`, `dto`, `config`, `utils`
* Resources: `src/main/resources` for `application.properties`, logging (`logback.xml`), and i18n (`messages_*.properties`).
* Tests: `src/test/java` mirrors main packages, with service tests under `vn/com/viettel/services/impl`.
* Build/infra: `pom.xml`, `docker/Dockerfile`, `Jenkinsfile.groovy`.

### Notes

* If the codebase already uses a dedicated JPA repo package (e.g., `repositories.jpa`), **follow what exists** in that module and do not restructure.

---

## 2) Build, Test, and Development Commands

* Build jar: `./mvnw clean package` (use `mvnw.cmd` on Windows)
* Run locally: `./mvnw spring-boot:run` (starts `vn.com.viettel.ServiceApplication`)
* Unit tests: `./mvnw test` (JUnit 5 + Mockito)
* Coverage report: `./mvnw test org.jacoco:jacoco-maven-plugin:0.8.11:report`
* Docker image: `docker build -f docker/Dockerfile -t evn/backend:latest .`

---

## 3) Core Engineering Rules (Must-Follow)

### 3.1 Time & Date

* **Do not use `Instant`** in domain/entities.
* Prefer `LocalDateTime` for server-side timestamps.
* Use `LocalDateTime.now()` for server-side timestamp generation.

### 3.2 Soft Delete (Mandatory)

* **No hard delete** unless the existing codebase explicitly requires it for a specific table and has prior art.
* Convention:

  * DB column: `IS_DELETED CHAR(1) ∈ {'Y','N'}`
  * Entity field: `Boolean isDeleted`
* **Always filter `isDeleted = false`** in every list/search/detail query.

**Standard mapping (use existing converters in project):**

```java
@Convert(converter = org.hibernate.type.YesNoConverter.class)
@Column(name = "IS_DELETED")
private Boolean isDeleted;

@Convert(converter = org.hibernate.type.YesNoConverter.class)
@Column(name = "IS_ACTIVE")
private Boolean isActive;
```

**Important:**

* **Never compare `'Y'/'N'` in code**. Repositories/specs must work with `Boolean`.

### 3.3 Entity Rules (JPA — Flat Mapping)

* Entities **must only contain fields that exist in the table**.
* **No derived/helper fields** inside entities.
* **Avoid JPA relations** (`@ManyToOne`, `@OneToMany`, etc.). Use FK IDs (`Long planId`, `Long taskId`, ...) instead.
* If the existing module already uses relations heavily, do **not** refactor them out in a single PR. For new tables/features, follow the flat mapping rule.

### 3.4 Exception & Error Handling

* **Absolutely do not use**:

  * `throw new RuntimeException(...)`
  * `ResponseStatusException`
* Throw the project’s `CustomException` format:

```java
throw new CustomException(HttpStatus.<STATUS>.value(), msg("<message.key>", params...));
```

### 3.5 Repository Validation: EXISTS-FIRST

* Validations like “id exists?” must use:

  * `existsBy...AndIsDeletedFalse(...)`, or
  * a lightweight query (`select 1`), or
  * projection queries (preferred for batches)

**Forbidden patterns:**

* `findById(...)` then null-check just to validate existence
* `findAllById(...)` then compare size
* loading entities/collections only to check existence

**Batch validate IDs in *one* query (mandatory):**

```java
@Query("select e.id from Entity e where e.id in :ids and e.isDeleted = false")
List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
```

### 3.6 Reuse Existing Repositories (No Duplicates)

* If a repository already exists for an entity/module:

  * **Do not recreate it**
  * **Do not rename it**
  * **Do not move its package**
* If you need a new method, **add it to the existing repository**.

### 3.7 Swagger / OpenAPI

**DTOs**

* Every DTO class + field must have `@Schema`.
* Required input fields must use validation annotations (`@NotNull`, `@NotBlank`, `@Size`, …) and `requiredMode = REQUIRED`.
* Output-only fields: `@Schema(accessMode = Schema.AccessMode.READ_ONLY)`.

**Controllers**

* Use `@Tag`, `@Operation`, `@ApiResponses`/`@ApiResponse`/`@Content`.
* Validate requests with `@Valid`.

### 3.8 Audit + User Enrichment (Avoid N+1)

If the table/entity has audit columns:

* `createdBy`, `updatedBy` (Long)
* `createdAt`, `updatedAt` (`LocalDateTime`)

Response DTO should expose:

* `UserDto createdByUser`
* `UserDto updatedByUser` (when applicable)

Batch enrichment rule:

* Collect all user IDs (`createdBy`, `updatedBy`, `actorId`, …) and load **in one query** via `SysUserRepository`.
* If org enrichment is needed, batch load via `SysOrgRepository`.

Current user:

* Get from `SecurityContextHolder`
* Fallback to `Constants.DEFAULT_USER_ID` when null (if that’s the established convention)

### 3.9 Mapper Rules (Mapping Guarantee)

* Mappers (MapStruct/ModelMapper) must **not** map read-only/output fields from DTO → Entity.
* Update flows must map a **whitelist** of allowed input fields only.
* Recommended MapStruct pattern:

  * `@BeanMapping(ignoreByDefault = true)`
  * `@Mapping` for each allowed input field

### 3.10 Quick Guardrails

* No `Instant` in domain/entities
* No `Optional.get()`
* No hard delete
* No validation via loading entities
* `CustomException` only
* Always filter `isDeleted=false`

---

## 4) Coding Style & Naming Conventions

* No repo-wide formatter is configured; follow existing code style in the touched package.
* Java conventions:

  * `UpperCamelCase` for classes
  * `lowerCamelCase` for methods/fields
  * packages lowercase
* Layer naming: use `*Controller`, `*Service`, `*ServiceImpl`, `*Repository`, `*Dto` as seen in the codebase.
* Prefer Lombok and MapStruct patterns already in use; avoid mixing in new frameworks for the same task.

---

## 5) Testing Guidelines

* Use JUnit Jupiter and Mockito.
* Tests under `src/test/java` mirroring production packages.
* Test class naming: `*Test.java` (see `services/impl/*Test.java`).
* Add tests for new logic; JaCoCo runs in CI.

---

## 6) Commit & Pull Request Guidelines

* Commits: short, imperative messages (e.g., `fix bug`, `auth module: add ...`, `chore: ...`).
* PRs should include:

  * Short summary
  * Linked issue/ticket (if any)
  * Test evidence (e.g., `./mvnw test` output)
* If behavior/API changes, update `API_Documentation.md` and mention it in the PR.

---

## 7) Security & Configuration Tips

* Do not commit secrets.
* Local overrides belong in `.env` (git-ignored) or environment variables.
* Keep `application.properties` sanitized.
* Use `application-k8s.properties` for cluster-specific defaults.
