# Authentication API

## Folder Structure

```text
backend/src/main/java/com/ticketing/system/auth/
|-- application/
|   |-- AuthService.java
|   |-- AuditLoggingService.java
|   |-- DatabaseUserDetailsService.java
|   `-- SecureTokenService.java
|-- controller/
|   `-- AuthController.java
|-- domain/
|   |-- PasswordResetTokenEntity.java
|   |-- RefreshTokenEntity.java
|   |-- RoleCode.java
|   |-- RoleEntity.java
|   |-- UserEntity.java
|   `-- UserStatus.java
|-- dto/
`-- infrastructure/
```

## API Contracts

Base path: `/api/v1/auth`

### Register

Request:

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "fullName": "Customer User",
  "email": "customer@example.com",
  "password": "Str0ngPassword!",
  "phoneNumber": "+10000000000"
}
```

Response:

```json
{
  "accessToken": "jwt",
  "refreshToken": "opaque-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "customer@example.com",
    "fullName": "Customer User",
    "role": "CUSTOMER"
  }
}
```

### Login

```json
{
  "email": "customer@example.com",
  "password": "Str0ngPassword!"
}
```

### Refresh Token

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

### Forgot Password

```json
{
  "email": "customer@example.com"
}
```

Response is intentionally generic to avoid account enumeration:

```json
{
  "message": "If the account exists, password reset instructions have been sent."
}
```

### Reset Password

```json
{
  "token": "password-reset-token",
  "newPassword": "NewStr0ngPassword!"
}
```

## Validation

- Email must be syntactically valid.
- Password must be 12-128 characters.
- Password must contain uppercase, lowercase, number, and special character.
- Full name must be 2-150 characters.
- Refresh and reset tokens are required.

## Security Decisions

- Passwords are hashed with BCrypt.
- Refresh and reset tokens are opaque and stored only as SHA-256 hashes.
- JWT access tokens contain user id as subject and role claims.
- All auth failures use generic messages where account enumeration is a risk.
- All sensitive auth operations write audit log entries.

## Run Commands

```powershell
docker compose -f docker/docker-compose.yml up -d postgres

cd backend
mvn test
```
