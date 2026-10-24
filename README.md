# Isha World Backend

Spring Boot modular monolith for the Isha World REST API.

## Stack

- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Spring Data JPA
- Spring Security
- MapStruct

## Local Setup

Create a PostgreSQL database:

```bash
createdb isha_world
```

Configure environment variables. **On macOS with Homebrew PostgreSQL**, the default role is your macOS username (not `postgres`):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/isha_world
export DB_USERNAME=$USER          # e.g. pws on this machine
export DB_PASSWORD=               # usually empty for local Homebrew Postgres
export JWT_SECRET=change-me-in-local-dev
```

On Linux or Docker, you typically use `postgres` / `postgres` instead. See `.env.example`.

Run the API:

```bash
./gradlew bootRun
```

Health check:

```bash
curl http://localhost:8080/api/v1/health
```

## Architecture

Package root: `com.ishaworld`

Feature modules follow:

```text
controller/
service/
repository/
entity/
dto/
mapper/
validator/
specification/
```

Rules:

- Controllers return DTOs only.
- Services own business logic.
- Repositories perform database access only.
- Schema changes go through Flyway.
- Use constructor injection.

