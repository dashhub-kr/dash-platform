# Repository Guidelines

## Project Structure & Module Organization
- `backend/`: Spring Boot 3.5 Java 17 app with **Presentation → Application → Domain ← Infrastructure** layers. Code in `src/main/java`, mappers in `src/main/resources/mapper`, docs in `docs/`, tests in `src/test/java` (ArchUnit at `architecture/ArchitectureTest.java`).
- `frontend/`: Vue 3 + Vite + Tailwind. Entrypoint `src/main.js`, router `src/router/`, shared styles `src/styles.css`, views under `src/views/`.

## Build, Test, and Development Commands
- Backend dev: `cd backend && ./mvnw spring-boot:run` (expects MySQL per `application.properties`).
- Backend tests: `./mvnw test`; scope to one class with `./mvnw -Dtest=AlgorithmRecordServiceTest test`; package with `./mvnw package`.
- Frontend: `cd frontend && npm install`, `npm run dev`, `npm run build`, `npm run preview`.

## Coding Style & Naming Conventions
- Java: see `backend/docs/ARCHITECTURE.md`. Controllers avoid infrastructure, Application uses Command/Result DTO `record`s, Domain stays framework-free. Imports order java→org→third-party→`com.ssafy.dash` then static. Naming: PascalCase classes, camelCase members, UPPER_SNAKE_CASE constants, packages `module.layer.feature` (e.g., `algorithm.application.dto`).
- Tests share fixtures (`TestFixtures`, `FixtureTime`) for deterministic timestamps.
- Frontend: Vue SFCs PascalCase, routes kebab-case, composables in `composables/`. Prefer Tailwind utilities; extend `styles.css` sparingly.

## Testing Guidelines
- Stack: JUnit 5 + Mockito + AssertJ + ArchUnit; MyBatis test starter enabled. Domain tests guard invariants, Service uses mocks for use-cases, Controller validates HTTP contracts via `@WebMvcTest`. Cover success and boundary cases; details in `backend/docs/TESTING_GUIDE.md`.
- Naming: sentence-style DisplayName for Domain, scenario sentences for Service, `행위_사유` for Controller. Use `verify`/`never` and assert side effects like `updatedAt` or webhook flags.
- Frontend tests are not scaffolded; add Vitest/Cypress as features grow and wire into `npm test`.

## Commit & Pull Request Guidelines
- Commits follow `feat|fix|docs|style|refactor|test|chore` with optional scope (`feat(BE): ...`). Keep them atomic and aligned to one change.
- PRs: link issues, summarize behavior change, include test evidence (`./mvnw test`, UI screenshots). Call out config shifts (DB/OAuth, webhook URLs) and note API/architecture impacts.

## Security & Configuration Tips
- Keep secrets out of git. Override DB creds and GitHub OAuth client settings via env vars or `application-*.properties`; the committed `application.properties` is local-only.
- Actuator lives under `/actuator`; restrict `health.show-details` in non-local profiles. Webhooks default to `push`—adjust `github.webhook.events` per deployment.
