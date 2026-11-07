# University Management System

## Overview
This project implements a Spring Boot university management backend, a static HTML/JavaScript frontend, and Docker Compose infrastructure for PostgreSQL. It provides JWT authentication, role-based access control, and CRUD operations for university records.

Students cannot self-register. An administrator provisions their account and student profile; students then sign in with the credentials supplied by the administrator.

## Technology stack
- Java 21
- Spring Boot 3.3.4
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL and Flyway
- Springdoc OpenAPI
- Docker Compose
- HTML, CSS, and JavaScript

## Authorization matrix
| Operation | Admin | Teacher | Student |
|---|---|---|---|
| Manage users | Full CRUD | No | No |
| Provision user accounts | Yes | No | No |
| Manage teachers | Full CRUD | No | No |
| Manage students | Full CRUD | No | No |
| View own profile | Yes | Yes | Yes |
| Update own profile | Yes | Yes | Yes |
| Manage courses | Full CRUD | Own courses | No |
| View courses | Yes | Yes | Yes |
| Manage enrollments | Yes | No | Create own |

## Authentication flow
1. An administrator creates the account with `POST /api/v1/users`.
2. The student signs in with `POST /api/v1/auth/login`.
3. The API returns a JWT.
4. The client sends the JWT in the `Authorization: Bearer <token>` header.

## Provision a user account
This endpoint is restricted to administrators:

```http
POST /api/v1/users
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "John Student",
  "email": "john@student.edu",
  "password": "Password123",
  "role": "STUDENT"
}
```

Create the associated student profile with the admin-only student endpoint after provisioning the user.

## Installation
1. Copy `.env.example` to `.env` and edit values.
2. Run `docker compose up --build` from the project root.
3. Access the frontend at http://localhost:3100.
4. Access the backend at http://localhost:8081.
5. Swagger UI is available at http://localhost:8081/swagger-ui.html.

## Database migration
The application uses PostgreSQL by default. Flyway creates the schema from `backend/src/main/resources/db/migration/V1__init.sql`. Migration V3 adds REST-managed short links used by the admin navigation.

## Admin pages

After signing in, the admin navigation opens each area on its own page:

- `/admin/dashboard` — administrator overview
- `/admin/students` — student accounts and profiles
- `/admin/teachers` — teacher accounts and profiles
- `/admin/courses` — course catalog
- `/admin/enrollments` — enrollment records
- `/students/{id}` — an individual student profile
- `/teachers/{id}` — an individual teacher profile
- `/courses/{id}` — an individual course profile

The corresponding REST API resources are:

- `GET /api/v1/students/{id}`
- `GET /api/v1/teachers/{id}`
- `GET /api/v1/courses/{id}`
- `GET /api/v1/courses/mine` (teacher's assigned courses)
- `GET /api/v1/users/{id}` (administrator-only user account details)

There is no separate `admin/{id}` entity. Administrator pages are grouped below `/admin`, and administrator operations use `/api/v1/users`, `/api/v1/students`, `/api/v1/teachers`, `/api/v1/courses`, and `/api/v1/enrollments`.

The frontend calls `POST /api/v1/short-links` for each page and uses the returned `/s/{code}` URL for navigation. Short-link redirects are public so they can be opened directly in a browser; creating links remains administrator-only.

## Testing
From the backend directory run:

```bash
mvn test
```
