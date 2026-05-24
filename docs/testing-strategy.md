# Testing Strategy

## Backend

Unit tests:

- Domain rules
- Application use cases
- DTO validation
- Security utilities
- Exception mapping

Integration tests:

- REST API contracts
- Spring Security authorization
- JPA repository behavior
- Flyway migration startup
- Ticket lifecycle workflows

## Frontend

Unit tests:

- Components
- Services
- Guards
- Interceptors
- RxJS state flows

End-to-end tests:

- Customer ticket creation
- Manager assignment and servicing
- Admin dashboard access
- Authorization redirects

## Commands

```powershell
cd backend
mvn test
```

Frontend test commands will be added when the Angular workspace is generated.
