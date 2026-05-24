# Enterprise Service Ticketing System

Enterprise-grade full-stack service ticketing system built with Angular 18, Spring Boot 3, Java 21, PostgreSQL, JWT authentication, Docker, Kubernetes/OpenShift, and CI/CD automation.

## Roles

- `CUSTOMER`: Creates tickets, tracks progress, comments, and confirms resolution.
- `SERVICE_MANAGER`: Reviews, assigns, services, comments, and resolves tickets.
- `ADMIN`: Manages users, roles, service configuration, global metrics, and operational oversight.

## Modules

- User Portal
- Admin Portal
- Service Manager Portal

## Core Features

- Ticket creation
- Ticket assignment
- Ticket servicing
- Ticket resolution
- Ticket comments
- Ticket history
- Dashboard metrics
- SLA tracking

## Project Structure

```text
ticketing-system/
|-- backend/              # Spring Boot API, domain logic, persistence, security
|-- frontend/             # Angular application, Material UI, Tailwind CSS, RxJS
|-- docker/               # Local Docker and Docker Compose assets
|-- docs/                 # Architecture, API, database, and security documentation
|-- k8s/                  # Kubernetes/OpenShift manifests
`-- .github/workflows/    # GitHub Actions pipelines
```

## Architecture Principles

- Clean architecture with explicit module boundaries
- SOLID, testable, reusable application services
- Security-first API design with JWT authentication
- Database migrations managed through Flyway
- Production-focused observability, configuration, and deployment readiness

## Documentation

- [System Design](docs/system-design.md)
- [Architecture](docs/architecture.md)
- [Backend Development Setup](docs/backend-development.md)
- [Frontend Architecture](docs/frontend-architecture.md)
- [Authentication API](docs/authentication-api.md)
- [Ticket Management API](docs/ticket-api.md)
- [API Design](docs/api-design.md)
- [Database Design](docs/database-design.md)
- [Sample Data](docs/sample-data.md)
- [Security](docs/security.md)
- [Testing Strategy](docs/testing-strategy.md)

Postman collections:

- [Authentication Collection](docs/postman/service-ticketing-auth.postman_collection.json)
- [Ticket Collection](docs/postman/service-ticketing-tickets.postman_collection.json)
- [Complete API Collection With Sample Responses](docs/postman/service-ticketing-complete.postman_collection.json)

## Engineering Rules

- Every feature must include folder structure, API contracts, validation, exception handling, DTOs, unit tests, and integration tests.
- Every implementation must explain architecture decisions, dependencies, risks, run commands, test strategy, and scalability impact.
- Code must follow layered architecture and avoid exposing persistence entities directly through API contracts.
- Security, authorization, input validation, and auditability are mandatory for user-facing and administrative flows.

## Run Commands

```powershell
# Start PostgreSQL
docker compose -f docker/docker-compose.yml up -d

# Run backend tests
cd backend
mvn test
```

```powershell
# Run all modules from the repository root
mvn test
```

Frontend commands will be added after the Angular workspace is generated.
