# Finance Dashboard Backend API

A backend system for a Finance Dashboard built with Spring Boot. It supports financial record management, role-based access control, JWT authentication, and dashboard analytics — built as part of a backend development assessment.

---

## Table of Contents

- [Objective](#objective)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [How to Run Locally](#how-to-run-locally)
- [Live Deployment](#live-deployment)
- [Default Credentials](#default-credentials)
- [API Reference](#api-reference)
- [Role & Permission Matrix](#role--permission-matrix)
- [Data Model](#data-model)
- [Assumptions & Tradeoffs](#assumptions--tradeoffs)
- [Evaluation Criteria Coverage](#evaluation-criteria-coverage)

---

## Objective

Build a backend for a finance dashboard where different users interact with financial records based on their role. The system supports storage and management of financial entries, user roles, permissions, and summary-level analytics.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 17                             |
| Framework    | Spring Boot 3.3                     |
| Security     | Spring Security + JWT (JJWT 0.11)   |
| Persistence  | Spring Data JPA + H2 (in-memory)    |
| Validation   | Jakarta Bean Validation             |
| Build Tool   | Maven                               |
| Testing      | JUnit 5 + Mockito                   |
| Deployment   | AWS EC2 (Ubuntu 24.04, t2.micro)    |

---

## Project Structure

```
FinanceDashboardApplication/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/example/FinanceDashboardApplication/
    │   │   ├── FinanceDashboardApplication.java       # Entry point
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java                # Spring Security + JWT setup
    │   │   │   └── DataSeeder.java                    # Seeds default users + records on startup
    │   │   ├── controller/
    │   │   │   ├── AuthController.java                # Login, Register
    │   │   │   ├── UserController.java                # User CRUD (ADMIN only)
    │   │   │   ├── FinancialRecordController.java     # Records CRUD + filters
    │   │   │   └── DashboardController.java           # Summary analytics
    │   │   ├── dto/
    │   │   │   ├── ApiResponse.java                   # Generic response wrapper
    │   │   │   ├── AuthResponse.java
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── CreateUserRequest.java
    │   │   │   ├── UpdateUserRequest.java
    │   │   │   ├── UserResponse.java
    │   │   │   ├── RecordRequest.java
    │   │   │   ├── RecordResponse.java
    │   │   │   └── DashboardSummary.java
    │   │   ├── exception/
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── GlobalExceptionHandler.java        # Centralized error handling
    │   │   ├── model/
    │   │   │   ├── Role.java                          # Enum: VIEWER, ANALYST, ADMIN
    │   │   │   ├── TransactionType.java               # Enum: INCOME, EXPENSE
    │   │   │   ├── User.java                          # JPA entity
    │   │   │   └── FinancialRecord.java               # JPA entity with soft delete
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   └── FinancialRecordRepository.java     # Custom JPQL queries
    │   │   ├── security/
    │   │   │   ├── JwtUtils.java                      # Token generation + validation
    │   │   │   ├── JwtAuthenticationFilter.java       # Request filter
    │   │   │   ├── AuthEntryPoint.java                # 401 JSON response
    │   │   │   ├── UserDetailsImpl.java               # Spring Security wrapper
    │   │   │   └── UserDetailsServiceImpl.java        # Loads user by email
    │   │   └── service/
    │   │       ├── AuthService.java
    │   │       ├── UserService.java
    │   │       ├── FinancialRecordService.java
    │   │       └── DashboardService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/FinanceDashboardApplication/service/
            ├── AuthServiceTest.java
            └── FinancialRecordServiceTest.java
```

---

## How to Run Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- STS (Spring Tool Suite) or any IDE

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/drup123/finance-dashboard-api.git
cd finance-dashboard-api

# 2. Build
mvn clean package -DskipTests

# 3. Run
mvn spring-boot:run
```

The server starts at **http://localhost:8080**

H2 Console (dev only): **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: *(leave empty)*

---

## Live Deployment

The API is deployed on **AWS EC2 (Free Tier)** and accessible at:

```
http://3.111.157.216:8080
```

### Test the live API

```
POST http://3.111.157.216:8080/api/auth/login
```

```json
{
  "email": "admin@finance.com",
  "password": "admin123"
}
```

---

## Default Credentials

These users are seeded automatically on startup by `DataSeeder.java`:

| Role    | Email                  | Password     |
|---------|------------------------|--------------|
| ADMIN   | admin@finance.com      | admin123     |
| ANALYST | analyst@finance.com    | analyst123   |
| VIEWER  | viewer@finance.com     | viewer123    |

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <token>
```

All responses follow this format:
```json
{
  "success": true,
  "message": "Success",
  "data": { }
}
```

---

### Auth Endpoints

#### POST `/api/auth/login`
No token required.

**Request:**
```json
{
  "email": "admin@finance.com",
  "password": "admin123"
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "tokenType": "Bearer",
    "email": "admin@finance.com",
    "name": "Super Admin",
    "role": "ADMIN"
  }
}
```

---

#### POST `/api/auth/register`
No token required. Open endpoint for self-registration.

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "pass1234",
  "role": "VIEWER"
}
```

**Response `201`:** Returns token and user details.

---

### User Endpoints — ADMIN only

| Method | Endpoint         | Description           |
|--------|------------------|-----------------------|
| GET    | `/api/users`     | List all users        |
| GET    | `/api/users/{id}`| Get user by ID        |
| POST   | `/api/users`     | Create a user         |
| PUT    | `/api/users/{id}`| Update name/role/status|
| DELETE | `/api/users/{id}`| Soft-deactivate user  |

**POST /api/users Request:**
```json
{
  "name": "New Analyst",
  "email": "newanalyst@finance.com",
  "password": "pass1234",
  "role": "ANALYST"
}
```

**PUT /api/users/{id} Request:**
```json
{
  "name": "Updated Name",
  "role": "ANALYST",
  "active": true
}
```

---

### Financial Record Endpoints

| Method | Endpoint            | Role Required       | Description              |
|--------|---------------------|---------------------|--------------------------|
| GET    | `/api/records`      | VIEWER/ANALYST/ADMIN| List records with filters|
| GET    | `/api/records/{id}` | VIEWER/ANALYST/ADMIN| Get record by ID         |
| POST   | `/api/records`      | ANALYST/ADMIN       | Create a record          |
| PUT    | `/api/records/{id}` | ADMIN               | Update a record          |
| DELETE | `/api/records/{id}` | ADMIN               | Soft-delete a record     |

**GET /api/records — Query Parameters (all optional):**

| Param       | Type           | Description                     |
|-------------|----------------|---------------------------------|
| `type`      | INCOME/EXPENSE | Filter by transaction type      |
| `category`  | string         | Case-insensitive match          |
| `startDate` | yyyy-MM-dd     | Records on or after this date   |
| `endDate`   | yyyy-MM-dd     | Records on or before this date  |
| `page`      | int            | Page index, default 0           |
| `size`      | int            | Page size, default 10           |

**Example:**
```
GET /api/records?type=EXPENSE&startDate=2024-01-01&endDate=2024-03-31&page=0&size=5
```

**POST /api/records Request:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Freelance",
  "date": "2024-04-10",
  "notes": "Consulting project payment"
}
```

---

### Dashboard Endpoints

#### GET `/api/dashboard/summary` — VIEWER, ANALYST, ADMIN

Returns aggregated financial data.

**Response `200`:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalIncome": 174500.00,
    "totalExpenses": 40700.00,
    "netBalance": 133800.00,
    "categoryWiseTotals": {
      "Salary":    { "INCOME": 150000.00 },
      "Rent":      { "EXPENSE": 24000.00 },
      "Freelance": { "INCOME": 24500.00 },
      "Marketing": { "EXPENSE": 8000.00 },
      "Utilities": { "EXPENSE": 3500.00 },
      "Software":  { "EXPENSE": 5200.00 }
    },
    "monthlyTrends": {
      "2024-03": { "INCOME": 59500.00, "EXPENSE": 17200.00 },
      "2024-02": { "INCOME": 65000.00, "EXPENSE": 8000.00 },
      "2024-01": { "INCOME": 50000.00, "EXPENSE": 15500.00 }
    },
    "recentActivity": [ ]
  }
}
```

---

## Role & Permission Matrix

| Action                         | VIEWER | ANALYST | ADMIN |
|--------------------------------|--------|---------|-------|
| Login / Register               | ✅     | ✅      | ✅    |
| View financial records         | ✅     | ✅      | ✅    |
| View dashboard summary         | ✅     | ✅      | ✅    |
| Create financial records       | ❌     | ✅      | ✅    |
| Update financial records       | ❌     | ❌      | ✅    |
| Delete financial records       | ❌     | ❌      | ✅    |
| View all users                 | ❌     | ❌      | ✅    |
| Create / Update / Delete users | ❌     | ❌      | ✅    |

Access control is enforced using `@PreAuthorize` annotations on every controller method. Unauthorized access returns `403 Forbidden`.

---

## Data Model

### `users`

| Column     | Type          | Notes                          |
|------------|---------------|--------------------------------|
| id         | BIGINT (PK)   | Auto-generated                 |
| name       | VARCHAR       | Display name                   |
| email      | VARCHAR       | Unique, used as login username |
| password   | VARCHAR       | BCrypt hashed                  |
| role       | VARCHAR       | VIEWER / ANALYST / ADMIN       |
| active     | BOOLEAN       | Soft-disable flag              |
| created_at | TIMESTAMP     | Set on insert                  |
| updated_at | TIMESTAMP     | Updated on every save          |

### `financial_records`

| Column     | Type           | Notes                        |
|------------|----------------|------------------------------|
| id         | BIGINT (PK)    | Auto-generated               |
| amount     | DECIMAL(19,2)  | Must be greater than 0       |
| type       | VARCHAR        | INCOME or EXPENSE            |
| category   | VARCHAR        | Free-text, stored trimmed    |
| date       | DATE           | Business date of the record  |
| notes      | VARCHAR(500)   | Optional description         |
| deleted    | BOOLEAN        | Soft-delete flag             |
| created_by | BIGINT (FK)    | References users.id          |
| created_at | TIMESTAMP      | Set on insert                |
| updated_at | TIMESTAMP      | Updated on every save        |

---

## Assumptions & Tradeoffs

| Topic | Decision |
|-------|----------|
| **Database** | H2 in-memory used for simplicity and zero-config setup. Can be swapped to MySQL or PostgreSQL by changing `spring.datasource.*` in `application.properties` — no code changes needed. MySQL is also configured locally for development. |
| **Authentication** | JWT stateless tokens. Expire after 24 hours. No refresh token flow implemented — can be added as an enhancement. |
| **Self-registration** | `POST /api/auth/register` is kept open for testing convenience so assessors can test all roles easily. In production this would be restricted to ADMIN only or limited to VIEWER role. Admin-managed creation is available at `POST /api/users`. |
| **Soft deletes** | Both users and records are soft-deleted by setting a flag rather than removing the row. This preserves audit history and maintains referential integrity. |
| **ANALYST permissions** | Analysts can create records but not update or delete them. This matches a real-world "data entry" role where analysts log transactions but admins own corrections and deletions. |
| **Pagination** | All list endpoints are paginated with a default page size of 10. The dashboard recent-activity list is fixed at the 10 most recent records. |
| **Error handling** | All errors — validation failures, authentication errors, not-found, server errors — return the same `ApiResponse` envelope with appropriate HTTP status codes for a consistent client contract. |
| **Password policy** | Minimum 6 characters enforced via `@Size` validation. BCrypt with default strength of 10 rounds used for hashing. |
| **Getters and Setters** | Lombok was not used. All getters and setters are written manually to maintain full control and avoid IDE annotation processing issues. |
| **Testing** | Unit tests cover service-layer business logic with mocked repositories using Mockito. Controller-level integration tests can be added using `@SpringBootTest` with `MockMvc`. |

---

## Evaluation Criteria Coverage

| Criteria | How it is addressed |
|----------|---------------------|
| **Backend Design** | Layered architecture — Controller → Service → Repository. Clear separation of concerns. DTOs used to decouple API layer from entities. |
| **Logical Thinking** | Role-based access enforced via `@PreAuthorize`. Business rules like soft delete, pagination, and category filtering implemented in service layer. |
| **Functionality** | All 13 endpoints working and tested. Seeded data available immediately on startup. Live API accessible on AWS EC2. |
| **Code Quality** | Consistent naming, single responsibility per class, no business logic in controllers, centralized exception handling. |
| **Database & Data Modeling** | Two normalized tables with proper relationships, constraints, and audit timestamps. Soft delete on both entities. |
| **Validation & Reliability** | `@Valid` on all request bodies, custom exceptions for not-found and bad-request, `GlobalExceptionHandler` returns structured error responses. |
| **Documentation** | This README covers setup, all endpoints with sample requests and responses, role matrix, data model, and design decisions. |
| **Additional Thoughtfulness** | Deployed on AWS EC2, H2 in-memory with MySQL support, DataSeeder for instant demo data, consistent API response envelope. |
