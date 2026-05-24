# API Design

## API Conventions

- Base path: `/api/v1`
- Content type: `application/json`
- Authentication: `Authorization: Bearer <accessToken>`
- API boundaries must use request and response DTOs.
- All write requests must be validated.
- All list endpoints must support pagination.

## Standard Error Response

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

## Authentication Contracts

### POST `/api/v1/auth/login`

Request:

```json
{
  "email": "customer@example.com",
  "password": "Str0ngPassword!"
}
```

Response:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "customer@example.com",
    "role": "CUSTOMER"
  }
}
```

## Ticket Contracts

### POST `/api/v1/tickets`

Allowed roles: `CUSTOMER`, `ADMIN`

Request:

```json
{
  "title": "Cannot access service portal",
  "description": "Login succeeds but dashboard does not load.",
  "priority": "HIGH",
  "category": "ACCESS"
}
```

Response:

```json
{
  "id": "uuid",
  "ticketNumber": "TCK-2026-000001",
  "title": "Cannot access service portal",
  "status": "OPEN",
  "priority": "HIGH",
  "category": "ACCESS",
  "createdBy": "uuid",
  "assignedTo": null,
  "createdAt": "2026-05-22T08:45:00Z",
  "slaDueAt": "2026-05-23T08:45:00Z"
}
```

Validation:

- `title`: required, 5-150 characters
- `description`: required, 20-5000 characters
- `priority`: required enum
- `category`: required enum

### PATCH `/api/v1/tickets/{ticketId}/assignment`

Allowed roles: `ADMIN`, `SERVICE_MANAGER`

Request:

```json
{
  "assigneeId": "uuid"
}
```

Response:

```json
{
  "id": "uuid",
  "status": "ASSIGNED",
  "assignedTo": "uuid",
  "assignedAt": "2026-05-22T09:00:00Z"
}
```

### PATCH `/api/v1/tickets/{ticketId}/status`

Allowed roles: `SERVICE_MANAGER`, `ADMIN`

Request:

```json
{
  "status": "IN_PROGRESS",
  "reason": "Initial diagnosis started"
}
```

Response:

```json
{
  "id": "uuid",
  "status": "IN_PROGRESS",
  "updatedAt": "2026-05-22T09:15:00Z"
}
```

### POST `/api/v1/tickets/{ticketId}/comments`

Allowed roles: `CUSTOMER`, `SERVICE_MANAGER`, `ADMIN`

Request:

```json
{
  "message": "I can reproduce the issue in Chrome and Edge."
}
```

Response:

```json
{
  "id": "uuid",
  "ticketId": "uuid",
  "authorId": "uuid",
  "message": "I can reproduce the issue in Chrome and Edge.",
  "createdAt": "2026-05-22T09:30:00Z"
}
```

## Dashboard Contracts

### GET `/api/v1/dashboard/metrics`

Allowed roles: `ADMIN`, `SERVICE_MANAGER`

Response:

```json
{
  "openTickets": 42,
  "assignedTickets": 18,
  "inProgressTickets": 9,
  "resolvedTickets": 120,
  "slaBreachedTickets": 3,
  "averageResolutionMinutes": 480
}
```
