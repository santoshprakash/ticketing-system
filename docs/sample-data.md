# Sample Data

Migration: `backend/src/main/resources/db/migration/V5__seed_sample_ticketing_data.sql`

The sample password for users added in V5 is:

```text
Password123!
```

## Users

| Role | Email | Name |
| --- | --- | --- |
| `ADMIN` | `admin@ticketing.local` | System Administrator |
| `ADMIN` | `admin.ops@ticketing.local` | Operations Administrator |
| `SERVICE_MANAGER` | `manager@ticketing.local` | Service Manager |
| `SERVICE_MANAGER` | `manager.network@ticketing.local` | Network Service Manager |
| `CUSTOMER` | `customer@ticketing.local` | Demo Customer |
| `CUSTOMER` | `customer.acme@ticketing.local` | Acme Customer |

## Tickets

| Ticket Number | Status | Priority | Category |
| --- | --- | --- | --- |
| `TCK-2026-000001` | `ASSIGNED` | `HIGH` | `ACCESS` |
| `TCK-2026-000002` | `OPEN` | `LOW` | `ACCOUNT` |
| `TCK-2026-000003` | `IN_PROGRESS` | `CRITICAL` | `NETWORK` |
| `TCK-2026-000004` | `RESOLVED` | `MEDIUM` | `BILLING` |

## Seed Coverage

The validated seed data contains:

| Table | Rows |
| --- | ---: |
| `roles` | 3 |
| `users` | 6 |
| `tickets` | 4 |
| `ticket_comments` | 3 |
| `ticket_history` | 5 |
| `ticket_attachments` | 3 |
| `assignments` | 3 |
| `notifications` | 3 |
| `audit_logs` | 3 |
| `refresh_tokens` | 2 |
| `password_reset_tokens` | 2 |

## Validation Commands

```powershell
docker compose -f docker/docker-compose.yml up -d postgres

mvn -pl backend org.flywaydb:flyway-maven-plugin:11.7.2:migrate `
  "-Dflyway.url=jdbc:postgresql://localhost:5432/ticketing_system" `
  "-Dflyway.user=ticketing_user" `
  "-Dflyway.password=ticketing_password" `
  "-Dflyway.locations=filesystem:backend/src/main/resources/db/migration"
```
