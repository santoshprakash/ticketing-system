# Enterprise Service Ticketing System

Enterprise-grade full-stack service ticketing system built with Angular 18, Angular Material, Tailwind CSS, Spring Boot 3, Java 21, PostgreSQL, Flyway, Spring Security, JWT authentication, Docker, and GitHub Actions.

## Features

- JWT authentication and refresh tokens
- Role-based authorization: `CUSTOMER`, `ADMIN`, `SERVICE_MANAGER`
- Super admin access management
- Ticket lifecycle management
- Ticket assignment, status changes, comments, attachments, and history
- SLA tracking and escalation data
- Admin user management
- Ticket category management
- Dashboard metrics
- Swagger/OpenAPI documentation
- Flyway database migrations and sample data
- Backend unit and integration tests
- Angular enterprise UI with responsive layout

## Project Structure

```text
ticketing-system/
|-- backend/              # Spring Boot 3 API, security, JPA, Flyway
|-- frontend/             # Angular 18 app, Material UI, Tailwind CSS
|-- docker/               # Docker Compose for local infrastructure
|-- docs/                 # Architecture, API, database, sample data docs
|-- k8s/                  # Kubernetes/OpenShift manifests
|-- .github/workflows/    # GitHub Actions CI
|-- pom.xml               # Maven parent project
`-- README.md
```

## Prerequisites

Install these before running the project:

- Git
- Java 21
- Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop
- PostgreSQL client tools are optional but useful

Verify:

```powershell
git --version
java -version
mvn -version
node -v
npm -v
docker --version
```

## Clone Repository

```powershell
git clone <your-github-repository-url>
cd ticketing-system
```

## Quick Start

Use this flow for local development.

### 1. Start PostgreSQL

```powershell
docker compose -f docker/docker-compose.yml up -d postgres
```

PostgreSQL will run on:

- Host: `localhost`
- Port: `5432`
- Database: `ticketing_system`
- Username: `ticketing_user`
- Password: `ticketing_password`

### 2. Start Backend

```powershell
cd backend
mvn spring-boot:run
```

Backend runs on:

- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`

Flyway runs automatically on backend startup and creates all tables plus sample data.

### 3. Start Frontend

Open a new terminal:

```powershell
cd frontend
npm install
npm start
```

Frontend runs on:

```text
http://localhost:4200
```

## Sample Login Accounts

Default password for seeded demo accounts:

```text
Password123!
```

Useful accounts:

| Role | Email | Notes |
| --- | --- | --- |
| Super Admin | `customer.acme@ticketing.local` | Full access, can manage users, module access, passwords, and categories |
| Admin | `admin.ops@ticketing.local` | Admin role sample user |
| Service Manager | `manager.network@ticketing.local` | Service manager sample user |
| Customer | `customer.acme@ticketing.local` | Promoted to super admin by latest migration |

Note: Older seed users such as `admin@ticketing.local`, `manager@ticketing.local`, and `customer@ticketing.local` may exist for relational sample data. Use the accounts above for UI login.

## Environment Variables

Backend reads configuration from environment variables with defaults in `backend/src/main/resources/application.yml`.

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/ticketing_system` |
| `DB_USERNAME` | `ticketing_user` |
| `DB_PASSWORD` | `ticketing_password` |
| `SERVER_PORT` | `8080` |
| `JWT_SECRET` | `change-me-in-secure-environments-change-me-now-64-bytes-minimum` |
| `JWT_ISSUER` | `ticketing-system` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |

For production, always replace `JWT_SECRET` with a strong secret managed outside source control.

Frontend API base URL is configured in:

```text
frontend/src/environments/environment.ts
```

Default:

```ts
apiBaseUrl: 'http://localhost:8080/api/v1'
```

## Run With Docker Compose

The current compose file starts PostgreSQL and backend.

```powershell
docker compose -f docker/docker-compose.yml up --build
```

Services:

- PostgreSQL: `localhost:5432`
- Backend: `localhost:8080`

Run frontend separately:

```powershell
cd frontend
npm install
npm start
```

Stop Docker services:

```powershell
docker compose -f docker/docker-compose.yml down
```

Remove database volume and reset all local data:

```powershell
docker compose -f docker/docker-compose.yml down -v
```

## Backend Commands

From `backend/`:

```powershell
mvn clean compile
mvn test
mvn package
mvn spring-boot:run
```

From repository root:

```powershell
mvn test
```

Run packaged backend jar:

```powershell
cd backend
mvn package
java -jar target/ticketing-system-0.0.1-SNAPSHOT.jar
```

Run packaged backend jar with explicit DB settings:

```powershell
java -jar target/ticketing-system-0.0.1-SNAPSHOT.jar `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/ticketing_system `
  --spring.datasource.username=ticketing_user `
  --spring.datasource.password=ticketing_password `
  --server.port=8080
```

## Frontend Commands

From `frontend/`:

```powershell
npm install
npm start
npm run build
npm test
```

Angular dev server:

```text
http://localhost:4200
```

Production build output:

```text
frontend/dist/ticketing-system
```

## API Testing

### Swagger UI

Start backend, then open:

```text
http://localhost:8080/swagger-ui.html
```

Authenticate:

1. Call `POST /api/v1/auth/login`.
2. Copy the `accessToken`.
3. Click `Authorize` in Swagger.
4. Enter:

```text
Bearer <accessToken>
```

### Postman Collections

Collections are available under:

```text
docs/postman/
```

Important collections:

- `service-ticketing-auth.postman_collection.json`
- `service-ticketing-tickets.postman_collection.json`
- `service-ticketing-complete.postman_collection.json`

## Database

Flyway migrations live in:

```text
backend/src/main/resources/db/migration
```

Main tables include:

- `roles`
- `users`
- `user_module_access`
- `tickets`
- `ticket_comments`
- `ticket_history`
- `ticket_attachments`
- `assignments`
- `notifications`
- `audit_logs`
- `ticket_types`
- `refresh_tokens`
- `password_reset_tokens`

Reset local database:

```powershell
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up -d postgres
cd backend
mvn spring-boot:run
```

## Important URLs

| Service | URL |
| --- | --- |
| Frontend | `http://localhost:4200` |
| Backend API | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Health Check | `http://localhost:8080/actuator/health` |

## Development Workflow

Recommended local workflow:

```powershell
# Terminal 1
docker compose -f docker/docker-compose.yml up -d postgres

# Terminal 2
cd backend
mvn spring-boot:run

# Terminal 3
cd frontend
npm start
```

Before pushing to GitHub:

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

## Troubleshooting

### Port Already In Use

Check processes on ports:

```powershell
Get-NetTCPConnection -LocalPort 4200,8080,5432 -State Listen
```

Stop a process:

```powershell
Stop-Process -Id <PID> -Force
```

### Backend Cannot Connect To Database

Confirm PostgreSQL is running:

```powershell
docker ps
docker compose -f docker/docker-compose.yml logs postgres
```

Restart database:

```powershell
docker compose -f docker/docker-compose.yml restart postgres
```

### Flyway Migration Failed

For local development, reset the database volume:

```powershell
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up -d postgres
cd backend
mvn spring-boot:run
```

### Frontend Still Shows Old UI

Restart Angular dev server and hard refresh browser:

```powershell
cd frontend
npm start
```

Then use:

```text
Ctrl + Shift + R
```

### VS Code Java Problems Stay After Fixes

VS Code may cache Java diagnostics. Run:

```text
Ctrl + Shift + P -> Java: Clean Java Language Server Workspace -> Restart and delete
Ctrl + Shift + P -> Developer: Reload Window
```

### Angular Bundle Budget Warning

`npm run build` may show an initial bundle budget warning. This is a warning, not a build failure. The build is successful if Angular prints `Application bundle generation complete`.

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

## Security Notes

- Do not commit production secrets.
- Replace `JWT_SECRET` for every deployed environment.
- Use HTTPS in production.
- Restrict CORS origins in production.
- Use a managed PostgreSQL service or secured database credentials.
- Seed/demo accounts are for local development only.

## Sample App UI 
<img width="1866" height="964" alt="image" src="https://github.com/user-attachments/assets/013aac49-a7ed-44ca-a0f4-a335f2f2172b" />

<img width="1878" height="834" alt="image" src="https://github.com/user-attachments/assets/8e054902-f73c-4b1b-9f39-4cc07988a02d" />
<img width="554" height="635" alt="image" src="https://github.com/user-attachments/assets/0096c3e1-0dbc-42e0-90ee-2a7a7348442e" />



## License

Add your project license before publishing this repository publicly.
