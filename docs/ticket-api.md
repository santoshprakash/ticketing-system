# Ticket Management API

Base path: `/api/v1/tickets`

Authentication: `Authorization: Bearer <accessToken>`

## Architecture Decisions

- The ticket module follows layered architecture: controller, application service, repository, mapper, DTO, and domain packages.
- API contracts use DTO records only; JPA entities are never returned directly.
- Authorization is enforced at controller boundaries with role-based `@PreAuthorize` rules.
- Ticket changes are written through `TicketService` so validation, status transitions, history, assignment, SLA timestamps, and audit logging remain consistent.
- Query APIs use Spring Data pagination, sorting, and specifications to keep filters composable and scalable.

## Folder Structure

```text
backend/src/main/java/com/ticketing/system/ticket/
|-- application/        # Ticket use cases, SLA policy, ticket number generation
|-- controller/         # REST API endpoints and authorization boundaries
|-- domain/             # JPA entities and domain enums
|-- dto/                # Request, response, and filter DTOs
|-- infrastructure/     # JPA repositories and specifications
`-- mapper/             # DTO/entity mapping

backend/src/test/java/com/ticketing/system/ticket/
|-- application/        # Unit tests for business behavior
`-- controller/         # Controller integration tests with validation handling
```

## API Contracts

| Method | Endpoint | Roles | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/v1/tickets` | `CUSTOMER`, `ADMIN` | Create a ticket |
| `POST` | `/api/v1/tickets/search` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | Search tickets with pagination, sorting, and filters |
| `POST` | `/api/v1/tickets/{ticketId}` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | Fetch ticket details |
| `PUT` | `/api/v1/tickets/{ticketId}` | `CUSTOMER`, `ADMIN` | Update ticket summary fields |
| `PATCH` | `/api/v1/tickets/{ticketId}/assignment` | `ADMIN`, `SERVICE_MANAGER` | Assign or reassign a ticket |
| `PATCH` | `/api/v1/tickets/{ticketId}/status` | `ADMIN`, `SERVICE_MANAGER` | Change ticket status |
| `PATCH` | `/api/v1/tickets/{ticketId}/resolve` | `ADMIN`, `SERVICE_MANAGER` | Resolve ticket |
| `PATCH` | `/api/v1/tickets/{ticketId}/reopen` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | Reopen ticket |
| `POST` | `/api/v1/tickets/{ticketId}/comments` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | Add comment |
| `POST` | `/api/v1/tickets/{ticketId}/comments/search` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | List comments |
| `POST` | `/api/v1/tickets/{ticketId}/attachments` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | Register attachment metadata |
| `POST` | `/api/v1/tickets/{ticketId}/attachments/search` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | List attachments |
| `POST` | `/api/v1/tickets/{ticketId}/history/search` | `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER` | List ticket history |

## Request Examples

Create ticket:

```json
{
  "title": "Portal login issue",
  "description": "Customer cannot access the service portal after password reset.",
  "priority": "HIGH",
  "category": "ACCESS"
}
```

Assign ticket:

```json
{
  "assigneeId": "11111111-1111-1111-1111-111111111111"
}
```

Change status:

```json
{
  "status": "IN_PROGRESS",
  "reason": "Service manager started investigation"
}
```

Add comment:

```json
{
  "message": "We verified the account and found the MFA device is out of sync.",
  "internal": false
}
```

Add attachment metadata:

```json
{
  "fileName": "login-error.png",
  "contentType": "image/png",
  "fileSizeBytes": 204800,
  "storageKey": "tickets/2026/TCK-2026-000001/login-error.png",
  "checksumSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
}
```

## Response Example

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "ticketNumber": "TCK-2026-000001",
  "title": "Portal login issue",
  "description": "Customer cannot access the service portal after password reset.",
  "status": "OPEN",
  "priority": "HIGH",
  "category": "ACCESS",
  "createdBy": "33333333-3333-3333-3333-333333333333",
  "assignedTo": null,
  "firstResponseDueAt": "2026-05-24T18:30:00Z",
  "firstRespondedAt": null,
  "resolutionDueAt": "2026-05-25T18:30:00Z",
  "resolvedAt": null,
  "closedAt": null,
  "slaBreached": false,
  "escalationLevel": 0,
  "createdAt": "2026-05-24T15:00:00Z",
  "updatedAt": "2026-05-24T15:00:00Z"
}
```

## Query Parameters

`POST /api/v1/tickets/search` supports:

- `status`: `OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REOPENED`
- `priority`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `category`: exact category filter
- `createdBy`: customer/user UUID
- `assignedTo`: service manager UUID
- `page`, `size`, `sort`: standard Spring Data pagination and sorting

Example:

```text
POST /api/v1/tickets/search?status=OPEN&priority=HIGH&page=0&size=20&sort=createdAt,desc
```

## Validation

- Title: required, 5 to 150 characters.
- Description: required, 20 to 5000 characters.
- Priority: required.
- Category: required, maximum 80 characters.
- Assignee: required UUID and must be an active `SERVICE_MANAGER`.
- Comment message: required, maximum 5000 characters.
- Attachment file size: 1 byte to 25 MB.
- Attachment checksum: optional SHA-256 hex value when supplied.

Validation failures return the standard `ApiErrorResponse` with `VALIDATION_ERROR` and field-level details.

## Status Lifecycle

Allowed transitions:

- `OPEN` -> `ASSIGNED`, `IN_PROGRESS`, `CLOSED`
- `ASSIGNED` -> `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- `IN_PROGRESS` -> `RESOLVED`, `CLOSED`
- `RESOLVED` -> `CLOSED`, `REOPENED`
- `CLOSED` -> `REOPENED`
- `REOPENED` -> `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

## Exception Handling

- Missing records return `404 NOT_FOUND`.
- Business rule violations return `409 BUSINESS_RULE_VIOLATION`.
- Invalid payloads return `400 VALIDATION_ERROR`.
- Security failures return JSON `401` or `403` responses through Spring Security handlers.

## Run Commands

```powershell
# Run the full backend test suite from the repository root
mvn test

# Start local PostgreSQL and backend through Docker Compose
docker compose -f docker/docker-compose.yml up --build

# Run backend locally with Maven
cd backend
mvn spring-boot:run
```

## Test Strategy

- `TicketServiceTest` covers ticket creation, SLA assignment, assignment behavior, and business rules.
- `TicketControllerIntegrationTest` covers REST contract behavior, validation errors, and JSON response shape.
- Existing global exception, JWT, auth, and health tests protect shared platform behavior.

## Scalability Notes

- Ticket search is specification-based and pageable for large datasets.
- SLA timestamps are denormalized on the ticket row for dashboard and escalation queries.
- History, comments, assignments, and attachments are append-friendly child tables.
- The module boundary is microservice-ready: ticket application services depend on repositories and DTOs, while authentication identity is consumed through JWT principal data.
