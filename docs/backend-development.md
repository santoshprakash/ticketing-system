# Backend Development Setup

## Requirement Analysis

The backend must provide an enterprise-grade Spring Boot 3 foundation using Java 21, Maven, PostgreSQL, Flyway, Spring Security, JWT, Swagger/OpenAPI, Docker, centralized exception handling, validation, logging, and health checks.

## Approach

- Use Spring Boot layered architecture with explicit packages for controllers, services, repositories, DTOs, entities, mappers, configuration, exceptions, and security.
- Use stateless JWT security and method security for role-based authorization.
- Use Flyway as the only schema evolution mechanism.
- Use Jakarta Bean Validation for request DTO validation.
- Use global exception handling for consistent API errors.
- Use actuator health endpoints for container orchestration readiness.
- Use correlation IDs in logs and API responses.

## Folder Structure

```text
backend/
|-- pom.xml
|-- Dockerfile
`-- src/
    |-- main/
    |   |-- java/com/ticketing/system/
    |   |   |-- config/
    |   |   |-- controller/
    |   |   |-- dto/
    |   |   |-- entity/
    |   |   |-- exception/
    |   |   |-- mapper/
    |   |   |-- repository/
    |   |   |-- security/
    |   |   `-- service/
    |   `-- resources/
    |       |-- application.yml
    |       `-- db/migration/
    `-- test/java/com/ticketing/system/
        |-- exception/
        `-- security/
```

## API Contracts

Base API conventions:

- API base path: `/api/v1`
- OpenAPI JSON: `/api-docs`
- Swagger UI: `/swagger-ui.html`
- Health endpoint: `/actuator/health`
- Protected APIs require `Authorization: Bearer <jwt>`

Standard error response:

```json
{
  "timestamp": "2026-05-22T09:30:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "path": "/api/v1/example",
  "traceId": "correlation-id",
  "fieldErrors": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ]
}
```

## Security Decisions

- JWT tokens are signed with HMAC using a secret of at least 32 bytes.
- Spring Security is stateless; sessions and CSRF are disabled for API usage.
- Swagger, auth endpoints, and actuator health/info are public.
- All other endpoints require authentication by default.
- Password hashing uses BCrypt strength 12.
- Security errors return JSON instead of HTML.

## Risks

- Weak JWT secrets must never be used outside local development.
- Existing manually created schemas can conflict with Flyway history; use fresh local volumes for Flyway-first startup.
- Dashboard and SLA query performance must be validated with production-like data volume.

## Dependencies

- Spring Boot Web
- Spring Boot Security
- Spring Boot Data JPA
- Spring Boot Validation
- Spring Boot Actuator
- Flyway
- PostgreSQL JDBC
- Springdoc OpenAPI
- JJWT
- JUnit 5
- Spring Security Test

## Run Commands

```powershell
# Start PostgreSQL
docker compose -f docker/docker-compose.yml up -d postgres

# Run tests
cd backend
mvn test

# Build backend image and run with PostgreSQL
cd ..
docker compose -f docker/docker-compose.yml up --build
```

## Test Strategy

- Unit tests validate JWT generation/parsing and configuration safeguards.
- Web-layer tests validate global exception handling and DTO validation responses.
- Integration tests validate public health endpoint behavior.
- Future repository integration tests should run against PostgreSQL-compatible infrastructure.

## Scalability Notes

- Backend is stateless and can scale horizontally.
- JWT authentication avoids shared session storage.
- Health probes support OpenShift readiness/liveness checks.
- Flyway keeps schema migration deterministic across environments.
