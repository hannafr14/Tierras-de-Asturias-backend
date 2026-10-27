# Tierras de Asturias | Backend

REST API for Tierras de Asturias: producer and product catalogs, session-based authentication, and order creation and retrieval.

Academic project under development. This is not a production-ready e-commerce platform and does not process real payments.

## Repositories and Documentation

- [Backend](https://github.com/hannafr14/Tierras-de-Asturias-backend)
- [Frontend](https://github.com/hannafr14/Tierras-de-Asturias-Frontend)
- [Backend Wiki](docs/wiki/Home.md)
- [API Reference](docs/wiki/API.md)
- [Screenshots and Project Links](docs/wiki/Media-and-Links.md)

## Design and Planning

- [Desktop mockup](https://www.figma.com/design/KIWo4qY828gz9moQC9s26L/Untitled?node-id=0-1)
- [Mobile design](https://www.figma.com/design/KIWo4qY828gz9moQC9s26L/Untitled?node-id=38-1251)
- [Wireframe](https://www.figma.com/design/KIWo4qY828gz9moQC9s26L/Untitled?node-id=38-1252)
- [Jira backlog, PPTDA](https://hannafrolova14.atlassian.net/jira/software/projects/PPTDA/boards/67/backlog?selectedIssue=PPTDA-392)

These are the project links supplied by the author. Access may require permission. Their inclusion does not confirm feature completion or Jira statuses.

## Technical Diagrams

- [Planned data model](docs/wiki/Architecture-and-Data.md#planned-data-model): the original full-project design, including future payments, profiles and roles.
- [Implemented database ER diagram](docs/wiki/Architecture-and-Data.md#relational-model): the current PMV database.
- [Domain class diagram](docs/wiki/Architecture-and-Data.md#domain-class-diagram): implemented Java entities, attributes, selected methods and associations.

The planned model describes the development roadmap; it is not evidence that every planned feature already exists.

## Technology

Versions declared in [pom.xml](pom.xml): Java 21, Spring Boot 4.1.1, Spring MVC, Spring Security, Spring Data JPA, Jakarta Validation, and MySQL. Docker Compose uses MySQL `8.4`.

Tests use JUnit, Mockito, MockMvc, and Testcontainers; coverage uses JaCoCo. Maven Wrapper is included, so a separate Maven installation is not required.

## Features

- Product and farm listings, name search, and individual details.
- Products associated with each producer.
- Registration, BCrypt passwords, session login, remember-me login, and logout.
- CSRF protection for write operations.
- Order creation as a guest or authenticated customer.
- Paginated own-order history and owner-restricted details.
- Initial JSON data and MySQL persistence using a Docker volume.

## Requirements

- JDK 21 configured in `JAVA_HOME` and available in the terminal.
- Docker Engine/Desktop with Docker Compose.
- Git and initial access to dependency and image registries.
- A private local `REMEMBER_ME_KEY` that must not be published.

## Local Setup

Run the following commands from this repository's root. PowerShell is recommended on Windows:

```powershell
java -version
docker compose up -d mysql
```

For the first local session, generate a key and start the application from the same terminal:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:REMEMBER_ME_KEY = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

Keep a stable key privately if you want the same configuration across restarts. Changing it may invalidate existing cookies. Do not save the real key in Git, screenshots, or documentation.

In Git Bash or Linux/macOS, set the same environment variable and use `./mvnw spring-boot:run`.

Check [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health). API routes use the `/api` prefix.

### VS Code Run Button

A variable set in a terminal only reaches processes started from that terminal. The VS Code Run button needs `REMEMBER_ME_KEY` through VS Code's environment or its local launch configuration. This repository ignores `.vscode/`; secrets must not be committed.

A `.env` file alone does not automatically export the variable to the Java process.

### MySQL Connection

`compose.yaml` publishes the container port on an available host port. Check it with:

```bash
docker compose port mysql 3306
```

Spring Boot's Docker Compose integration is included for development. Do not assume that the published port is always `3306`.

If that integration is disabled or an external database is used, explicitly configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Credentials included in the project are for development and must be replaced before deployment.

## Endpoints

| Area | Routes |
|---|---|
| Products | `GET /api/products?search=...`, `GET /api/products/{id}` |
| Farms | `GET /api/farms?search=...`, `GET /api/farms/{id}`, `GET /api/farms/{id}/products` |
| Account | `GET /api/auth/csrf`, `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout` |
| Orders | `POST /api/orders`, `GET /api/orders/mine?page=0`, `GET /api/orders/mine/{id}` |
| Health | `GET /actuator/health` |

POST requests require valid CSRF even when guest access is allowed. Registration and order creation currently return HTTP `200`, not `201`. See the [API Reference](docs/wiki/API.md) for examples and errors.

## Data and Persistence

The JSON files in `src/main/resources/data/` contain 68 farms and 337 products in the documented revision. They do not include a default login account: register a demonstration user.

The seeder loads the catalog only when both tables are empty. If farms or products already exist, editing JSON and restarting does not update those records. Existing records need a reviewed update and a database backup.

The `mysql-data` volume preserves data when the container is stopped or recreated. Do not delete volumes to apply catalog changes.

## Tests and Build

With Docker available and environment variables configured, preferably stop the development application before running:

```powershell
.\mvnw.cmd "-Dspring.docker.compose.enabled=false" test
.\mvnw.cmd package
```

Integration tests use MySQL through Testcontainers; they must not target the working database. The JaCoCo report is generated at `target/site/jacoco/index.html`.

To check the seeder in isolation:

```powershell
.\mvnw.cmd "-Dtest=DataSeederTest" "-Dspring.docker.compose.enabled=false" test
```

The JAR is generated at `target/backend-0.0.1-SNAPSHOT.jar`. Configure its environment and database connection before running it; the development environment is not automatically carried into the packaged JAR.

## Known Limitations

- Stripe, email password recovery, profile editing, and status administration are not implemented.
- New orders receive `PENDING`; status transitions are not managed.
- Order creation currently accepts amounts and unit prices from the client; they must be recalculated on the server before commercial use.
- Stock is not deducted or reserved when orders are created.
- Shipping and taxes use a demonstration model, not validated tax rules.
- `ddl-auto=update` is used for this development environment; production needs a versioned migration strategy.
- HTTPS, cookie settings, secrets, and business validations need review before deployment with real users.

See [Security](docs/wiki/Security.md) and [Operations and Testing](docs/wiki/Operations-and-Testing.md).

No general license is declared for the code or images. Confirm permissions before redistribution.
