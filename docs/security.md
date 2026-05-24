# Security

## Security Model

Authentication uses JWT access tokens issued after successful login. Authorization is enforced through Spring Security and method-level or route-level role checks.

## Roles

- `CUSTOMER`
- `ADMIN`
- `SERVICE_MANAGER`

## Authorization Rules

- Customers can create tickets, view their own tickets, comment on their own tickets, and view ticket history relevant to their own tickets.
- Service managers can view assigned or service-queue tickets, update servicing state, add comments, and resolve tickets.
- Admins can manage users, assign tickets, view system-wide dashboards, and access audit information.

## Secure Coding Rules

- Never expose password hashes, secrets, or internal security claims in DTO responses.
- Never expose JPA entities directly from controllers.
- Validate every request DTO with Jakarta Bean Validation.
- Use centralized exception handling and avoid leaking stack traces.
- Use least-privilege authorization checks on every protected endpoint.
- Store secrets in environment variables or platform secret stores, not source code.
- Use HTTPS in deployed environments.

## Exception Handling

Security exceptions should produce consistent responses:

- `401 UNAUTHORIZED`: missing, invalid, or expired authentication
- `403 FORBIDDEN`: authenticated user lacks permission
- `404 NOT_FOUND`: resource does not exist or must not be disclosed

## Audit Requirements

Audit events should be recorded for login attempts, user role changes, ticket assignment, ticket status transitions, resolution, and administrative operations.
