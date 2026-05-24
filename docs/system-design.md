# Enterprise Service Ticketing System Design

## 1. High-Level Architecture

The Service Ticketing System is a modular monolith designed for microservice readiness. It serves three portals over a shared backend API:

- Customer Portal
- Admin Portal
- Service Manager Portal

Core capabilities:

- Authentication
- Authorization
- Ticket lifecycle
- Dashboard
- Notifications
- SLA management
- Audit logging

```mermaid
flowchart LR
    Customer[Customer Portal] --> Api[Spring Boot API]
    Admin[Admin Portal] --> Api
    Manager[Service Manager Portal] --> Api

    Api --> Auth[Auth Module]
    Api --> Ticket[Ticket Module]
    Api --> Sla[SLA Module]
    Api --> Notification[Notification Module]
    Api --> Audit[Audit Module]
    Api --> Dashboard[Dashboard Module]

    Auth --> Db[(PostgreSQL)]
    Ticket --> Db
    Sla --> Db
    Notification --> Db
    Audit --> Db
    Dashboard --> Db
```

Architecture decisions:

- Start as a modular monolith to reduce distributed-system complexity while preserving service boundaries.
- Use DTOs at all API boundaries.
- Keep business rules in application/domain layers.
- Persist all schema changes through Flyway.
- Use stateless JWT authentication for horizontal scalability.
- Design ticket, SLA, notification, and audit modules as future extractable services.

Key risks:

- Dashboard metrics can overload transactional tables without indexing or aggregation.
- SLA breach calculation requires consistent clocks and clear timezone handling.
- Ticket assignment requires concurrency controls.
- Authorization bugs can leak customer or operational data.
- Notification delivery should not block ticket workflows.

Dependencies:

- Angular 18
- Angular Material
- Tailwind CSS
- RxJS
- Spring Boot 3
- Spring Security
- PostgreSQL
- JPA/Hibernate
- Flyway
- Docker
- OpenShift

## 2. Frontend Architecture

The frontend is an Angular 18 application organized by core infrastructure, shared UI, and feature portals.

```text
frontend/src/app/
|-- core/
|   |-- auth/
|   |-- guards/
|   |-- interceptors/
|   |-- models/
|   `-- services/
|-- shared/
|   |-- components/
|   |-- directives/
|   `-- pipes/
|-- features/
|   |-- auth/
|   |-- customer-portal/
|   |-- admin-portal/
|   |-- service-manager-portal/
|   |-- tickets/
|   |-- dashboard/
|   |-- notifications/
|   `-- sla/
`-- layout/
```

Frontend responsibilities:

- Route users into the correct portal based on role.
- Use route guards for protected pages.
- Use HTTP interceptors for JWT attachment, correlation IDs, and API error normalization.
- Keep Material UI components wrapped in shared reusable components when domain-specific behavior is needed.
- Use RxJS for state streams, request orchestration, polling, and dashboard refresh flows.

## 3. Backend Architecture

The backend is a Spring Boot 3 API using layered architecture per module.

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
|-- sla/
|-- dashboard/
|-- notification/
|-- audit/
`-- common/
    |-- exception/
    |-- response/
    |-- security/
    `-- validation/
```

Layer responsibilities:

- `controller`: REST endpoints, request DTO validation, response DTO mapping.
- `application`: use cases, transactions, orchestration, authorization checks where business-specific.
- `domain`: enums, domain entities, value objects, lifecycle rules.
- `infrastructure`: JPA repositories, external adapters, persistence mappings.
- `dto`: request/response contracts only.
- `common`: centralized exceptions, security support, shared validation, API response standards.

## 4. Database Architecture

PostgreSQL is the system of record. Flyway owns schema migrations.

Main tables:

- `users`
- `roles`
- `tickets`
- `ticket_comments`
- `ticket_history`
- `ticket_assignments`
- `sla_policies`
- `sla_events`
- `notifications`
- `audit_events`

```mermaid
erDiagram
    USERS ||--o{ TICKETS : creates
    USERS ||--o{ TICKET_COMMENTS : writes
    USERS ||--o{ TICKET_ASSIGNMENTS : assigned
    TICKETS ||--o{ TICKET_COMMENTS : has
    TICKETS ||--o{ TICKET_HISTORY : records
    TICKETS ||--o{ TICKET_ASSIGNMENTS : has
    TICKETS ||--o{ SLA_EVENTS : tracks
    SLA_POLICIES ||--o{ TICKETS : applies
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ AUDIT_EVENTS : performs

    USERS {
        uuid id PK
        string email
        string password_hash
        string full_name
        string role
        string status
        timestamp created_at
        timestamp updated_at
    }

    TICKETS {
        uuid id PK
        string ticket_number
        string title
        text description
        string status
        string priority
        string category
        uuid created_by FK
        uuid assigned_to FK
        uuid sla_policy_id FK
        timestamp created_at
        timestamp updated_at
        timestamp sla_due_at
        timestamp resolved_at
    }

    TICKET_COMMENTS {
        uuid id PK
        uuid ticket_id FK
        uuid author_id FK
        text message
        boolean internal
        timestamp created_at
    }

    TICKET_HISTORY {
        uuid id PK
        uuid ticket_id FK
        uuid actor_id FK
        string event_type
        jsonb old_value
        jsonb new_value
        timestamp created_at
    }

    SLA_POLICIES {
        uuid id PK
        string priority
        string category
        int response_minutes
        int resolution_minutes
        boolean active
    }

    SLA_EVENTS {
        uuid id PK
        uuid ticket_id FK
        string event_type
        timestamp due_at
        timestamp breached_at
    }

    NOTIFICATIONS {
        uuid id PK
        uuid recipient_id FK
        string channel
        string status
        string subject
        text body
        timestamp created_at
        timestamp sent_at
    }

    AUDIT_EVENTS {
        uuid id PK
        uuid actor_id FK
        string action
        string resource_type
        uuid resource_id
        jsonb metadata
        timestamp created_at
    }
```

Index strategy:

- `tickets(status)`
- `tickets(created_by)`
- `tickets(assigned_to)`
- `tickets(priority, category)`
- `tickets(sla_due_at)`
- `ticket_history(ticket_id, created_at)`
- `notifications(recipient_id, status)`
- `audit_events(actor_id, created_at)`

## 5. Deployment Architecture

Docker is used for local packaging. OpenShift is the target runtime.

```mermaid
flowchart TB
    Dev[Developer] --> Git[GitHub Repository]
    Git --> Actions[GitHub Actions CI]
    Actions --> Registry[Container Registry]
    Registry --> OpenShift[OpenShift Cluster]

    subgraph OpenShift
        Route[OpenShift Route / Ingress]
        FrontendPod[Angular Nginx Pod]
        BackendPod1[Spring Boot Pod]
        BackendPod2[Spring Boot Pod]
        Pg[(Managed PostgreSQL / StatefulSet)]
        Secrets[Secrets]
        Config[ConfigMaps]
    end

    Route --> FrontendPod
    FrontendPod --> BackendPod1
    FrontendPod --> BackendPod2
    BackendPod1 --> Pg
    BackendPod2 --> Pg
    BackendPod1 --> Secrets
    BackendPod2 --> Secrets
    BackendPod1 --> Config
    BackendPod2 --> Config
```

Deployment decisions:

- Run backend as stateless replicas.
- Store secrets in OpenShift Secrets.
- Store non-sensitive environment configuration in ConfigMaps.
- Use readiness and liveness probes.
- Use rolling deployments.
- Prefer managed PostgreSQL for production where available.

## 6. API Design Strategy

Base path: `/api/v1`

API principles:

- RESTful resources.
- DTO-only request and response bodies.
- Jakarta Bean Validation on request DTOs.
- Consistent error response format.
- Pagination for all list endpoints.
- Role authorization declared per endpoint.
- Correlation ID returned on every response.

Core contracts:

```text
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout

GET    /api/v1/users
GET    /api/v1/users/{userId}
POST   /api/v1/users
PATCH  /api/v1/users/{userId}/status

POST   /api/v1/tickets
GET    /api/v1/tickets
GET    /api/v1/tickets/{ticketId}
PATCH  /api/v1/tickets/{ticketId}/assignment
PATCH  /api/v1/tickets/{ticketId}/status
POST   /api/v1/tickets/{ticketId}/comments
GET    /api/v1/tickets/{ticketId}/comments
GET    /api/v1/tickets/{ticketId}/history

GET    /api/v1/dashboard/metrics

GET    /api/v1/sla/policies
POST   /api/v1/sla/policies
PATCH  /api/v1/sla/policies/{policyId}

GET    /api/v1/notifications
PATCH  /api/v1/notifications/{notificationId}/read

GET    /api/v1/audit/events
```

Validation examples:

- Ticket title: required, 5-150 characters.
- Ticket description: required, 20-5000 characters.
- Priority: required enum.
- Assignee: valid active `SERVICE_MANAGER`.
- Status transitions: must follow lifecycle rules.

## 7. Security Architecture

Security controls:

- JWT access tokens for API authentication.
- Strong password hashing.
- Role-based endpoint authorization.
- Object-level authorization for customer ticket access.
- CORS restricted to trusted frontend origins.
- HTTPS-only deployment.
- Secrets managed outside source code.
- Audit logging for sensitive operations.
- No JPA entity exposure in API responses.
- Centralized exception handling without stack trace leakage.

Role permissions:

```text
CUSTOMER
- Create tickets
- View own tickets
- Comment on own tickets
- View own ticket history

SERVICE_MANAGER
- View assigned/service-queue tickets
- Accept or service assigned tickets
- Add comments
- Resolve tickets
- View operational dashboard metrics

ADMIN
- Manage users
- Assign tickets
- Manage SLA policies
- View all tickets
- View audit logs
- View global dashboard metrics
```

## 8. Component Diagram

```mermaid
flowchart TB
    subgraph Angular
        AuthUI[Auth Feature]
        CustomerUI[Customer Portal]
        AdminUI[Admin Portal]
        ManagerUI[Service Manager Portal]
        SharedUI[Shared Components]
        CoreUI[Core Guards / Interceptors]
    end

    subgraph SpringBoot
        AuthApi[Auth Controller]
        TicketApi[Ticket Controller]
        DashboardApi[Dashboard Controller]
        SlaApi[SLA Controller]
        NotificationApi[Notification Controller]
        AuditApi[Audit Controller]

        AuthUseCases[Auth Use Cases]
        TicketUseCases[Ticket Use Cases]
        SlaUseCases[SLA Use Cases]
        NotificationUseCases[Notification Use Cases]
        AuditUseCases[Audit Use Cases]

        Repos[JPA Repositories]
    end

    Angular --> SpringBoot
    AuthApi --> AuthUseCases
    TicketApi --> TicketUseCases
    DashboardApi --> TicketUseCases
    SlaApi --> SlaUseCases
    NotificationApi --> NotificationUseCases
    AuditApi --> AuditUseCases
    AuthUseCases --> Repos
    TicketUseCases --> Repos
    SlaUseCases --> Repos
    NotificationUseCases --> Repos
    AuditUseCases --> Repos
    Repos --> Db[(PostgreSQL)]
```

## 9. Sequence Diagrams

### Authentication

```mermaid
sequenceDiagram
    actor User
    participant Angular
    participant AuthApi
    participant AuthService
    participant UserRepo
    participant JwtProvider

    User->>Angular: Submit email and password
    Angular->>AuthApi: POST /api/v1/auth/login
    AuthApi->>AuthService: authenticate(request)
    AuthService->>UserRepo: findByEmail(email)
    UserRepo-->>AuthService: User
    AuthService->>AuthService: verify password and status
    AuthService->>JwtProvider: create access token
    JwtProvider-->>AuthService: JWT
    AuthService-->>AuthApi: LoginResponseDto
    AuthApi-->>Angular: 200 OK with token
```

### Ticket Creation

```mermaid
sequenceDiagram
    actor Customer
    participant Angular
    participant TicketApi
    participant TicketService
    participant SlaService
    participant TicketRepo
    participant AuditService
    participant NotificationService

    Customer->>Angular: Create ticket
    Angular->>TicketApi: POST /api/v1/tickets
    TicketApi->>TicketApi: Validate request DTO
    TicketApi->>TicketService: createTicket(command)
    TicketService->>SlaService: resolvePolicy(priority, category)
    SlaService-->>TicketService: SLA policy
    TicketService->>TicketRepo: save(ticket)
    TicketRepo-->>TicketService: persisted ticket
    TicketService->>AuditService: record ticket created
    TicketService->>NotificationService: notify service queue
    TicketService-->>TicketApi: TicketResponseDto
    TicketApi-->>Angular: 201 Created
```

### Ticket Assignment

```mermaid
sequenceDiagram
    actor ManagerOrAdmin
    participant Angular
    participant TicketApi
    participant TicketService
    participant UserService
    participant TicketRepo
    participant HistoryService
    participant AuditService

    ManagerOrAdmin->>Angular: Assign ticket
    Angular->>TicketApi: PATCH /api/v1/tickets/{id}/assignment
    TicketApi->>TicketService: assignTicket(ticketId, assigneeId)
    TicketService->>UserService: validate active service manager
    UserService-->>TicketService: assignee valid
    TicketService->>TicketRepo: lock and load ticket
    TicketRepo-->>TicketService: ticket
    TicketService->>TicketService: apply assignment rule
    TicketService->>TicketRepo: save(ticket)
    TicketService->>HistoryService: record assignment
    TicketService->>AuditService: record assignment
    TicketService-->>TicketApi: AssignmentResponseDto
    TicketApi-->>Angular: 200 OK
```

### SLA Breach Detection

```mermaid
sequenceDiagram
    participant Scheduler
    participant SlaService
    participant TicketRepo
    participant NotificationService
    participant AuditService

    Scheduler->>SlaService: detectBreaches()
    SlaService->>TicketRepo: find unresolved tickets past slaDueAt
    TicketRepo-->>SlaService: breached tickets
    loop For each breached ticket
        SlaService->>SlaService: mark SLA breached
        SlaService->>NotificationService: notify manager/admin
        SlaService->>AuditService: record SLA breach
    end
```

## 10. Microservice Readiness Strategy

The first implementation should remain a modular monolith. Extract services only when operational need is proven.

Extraction candidates:

- Auth Service
- Ticket Service
- SLA Service
- Notification Service
- Audit Service
- Dashboard/Reporting Service

Readiness practices:

- Keep modules loosely coupled through application services.
- Avoid direct repository access across modules.
- Use IDs instead of passing mutable domain objects across module boundaries.
- Keep module-owned database tables clearly grouped.
- Publish domain events internally for ticket-created, ticket-assigned, ticket-resolved, and SLA-breached.
- Make notification processing asynchronous-ready.

## 11. Logging Strategy

Logging standards:

- Structured JSON logs in deployed environments.
- Include `traceId`, `userId`, `role`, `requestPath`, `httpMethod`, and response status.
- Never log passwords, tokens, secrets, or full authorization headers.
- Log authentication failures without revealing whether email exists.
- Log ticket lifecycle events through audit tables, not only application logs.
- Use severity consistently: `INFO` for lifecycle, `WARN` for recoverable policy/security concerns, `ERROR` for unexpected failures.

Operational logging targets:

- Request access logs.
- Security events.
- Ticket state transitions.
- SLA breach detection.
- Notification send failures.
- Database migration startup.

## 12. Error Handling Strategy

Use centralized exception handling through `@RestControllerAdvice`.

Standard error response:

```json
{
  "timestamp": "2026-05-22T08:45:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/tickets",
  "traceId": "01HY..."
}
```

Exception categories:

- `ValidationException`: invalid request DTO or business validation failure.
- `UnauthorizedException`: missing or invalid authentication.
- `ForbiddenException`: authenticated user lacks permission.
- `NotFoundException`: resource does not exist or should not be disclosed.
- `ConflictException`: invalid state transition, concurrent assignment, duplicate data.
- `ExternalServiceException`: notification or integration failure.
- `SystemException`: unexpected server-side failure.

HTTP mapping:

- `400 BAD_REQUEST`: validation failure.
- `401 UNAUTHORIZED`: authentication failure.
- `403 FORBIDDEN`: authorization failure.
- `404 NOT_FOUND`: missing resource.
- `409 CONFLICT`: lifecycle or concurrency conflict.
- `500 INTERNAL_SERVER_ERROR`: unexpected failure.

## 13. Test Strategy

Unit tests:

- Domain lifecycle rules.
- SLA due-date calculation.
- Authorization decision helpers.
- Application use cases.
- DTO validation.
- Exception mapping.

Integration tests:

- Auth login.
- Ticket creation.
- Ticket assignment.
- Ticket status transition.
- Ticket comments.
- Ticket history creation.
- Dashboard metrics.
- Role-based access restrictions.
- Flyway migration startup.

End-to-end tests:

- Customer creates and tracks ticket.
- Service manager assigns, services, comments, and resolves ticket.
- Admin manages users and SLA policies.
- Unauthorized users are blocked from protected portal routes.

## 14. Run Commands

```powershell
# Start PostgreSQL
docker compose -f docker/docker-compose.yml up -d

# Run backend tests
cd backend
mvn test
```

Frontend commands will be finalized after Angular workspace generation.
