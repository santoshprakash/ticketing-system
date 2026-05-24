# Architecture

## Requirement Analysis

The system is an enterprise service ticketing platform with three portals:

- User Portal for customers
- Admin Portal for administrators
- Service Manager Portal for service teams

The platform must support ticket creation, assignment, servicing, resolution, comments, history, dashboards, and SLA tracking. It must enforce role-based access for `CUSTOMER`, `ADMIN`, and `SERVICE_MANAGER`.

## Approach

The backend uses layered architecture per bounded context:

```text
backend/src/main/java/com/ticketing/system/
|-- auth/
|   |-- controller/
|   |-- application/
|   |-- domain/
|   |-- infrastructure/
|   `-- dto/
|-- user/
|-- ticket/
|-- comment/
|-- notification/
|-- audit/
`-- common/
```

Layer responsibilities:

- `controller`: REST API adapters, request validation entry point, response mapping
- `application`: use cases, transaction boundaries, orchestration
- `domain`: domain models, enums, business rules, invariants
- `infrastructure`: JPA repositories, external integrations, persistence adapters
- `dto`: request and response contracts
- `common`: cross-cutting exception handling, security, validation, and response standards

The frontend uses feature-oriented Angular boundaries:

```text
frontend/src/app/
|-- core/
|-- shared/
|-- features/
|   |-- auth/
|   |-- dashboard/
|   |-- tickets/
|   |-- users/
|   `-- admin/
`-- layout/
```

## Architecture Decisions

- Use DTOs for every API boundary to prevent persistence model leakage.
- Keep business rules in application/domain layers instead of controllers.
- Use Flyway for repeatable database evolution across environments.
- Use JWT and Spring Security for stateless authentication and role-based authorization.
- Use separate portal modules in the frontend while sharing common auth, guards, interceptors, UI components, and models.
- Use Docker Compose for local infrastructure and Kubernetes/OpenShift manifests for production-style deployment.

## Risks

- SLA tracking requires precise time-zone handling, clock consistency, and background processing.
- Ticket assignment can become a contention point if multiple managers act on the same ticket concurrently.
- Dashboard metrics can become expensive without aggregation, indexing, and caching strategy.
- Authorization mistakes can expose customer tickets or administrative actions.
- Notification and audit requirements can grow into asynchronous workflows.

## Dependencies

- Java 21
- Spring Boot 3
- Spring Security
- JPA/Hibernate
- PostgreSQL
- Flyway
- Angular 18
- Angular Material
- Tailwind CSS
- RxJS
- Docker and Docker Compose
- Kubernetes/OpenShift
- GitHub Actions

## Scalability Direction

- Keep use cases stateless so backend instances can scale horizontally.
- Add database indexes around ticket status, assignee, creator, priority, SLA due time, and creation time.
- Introduce async processing for notifications, audit enrichment, and SLA breach detection when needed.
- Use pagination and filtering for ticket lists and history queries from the first implementation.
