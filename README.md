# Student Information Management System

## Live Link
[Visit Live App](#) - https://student-info-management-1.onrender.com


## Project Overview
This is a full-stack Student Information Management System built with Java for backend logic and HTML/CSS/JavaScript for the user interface. The application supports complete CRUD operations with SQL-backed persistence.

Student data is managed through JDBC using PostgreSQL or SQLite (configurable by environment), with automatic in-memory fallback only if no JDBC driver is available.

This repository is now combined with a complete DBMS syllabus design pack (Unit I-IV), including ER-to-relational mapping, normalized schema, SQL queries, indexing strategy, transactions, and advanced concepts.

## Tech Stack
- Java (HTTP server and REST-style API)
- HTML (page structure)
- CSS (UI styling)
- JavaScript (frontend interactivity and API calls)
- JDBC + PostgreSQL (production)
- JDBC + SQLite (local default)
- Automatic in-memory fallback when JDBC driver is unavailable
- DBMS design artifacts (SQL + relational algebra + normalization notes)

## Combined DBMS Project Coverage (Unit I-IV)
- Unit I (ER Model + Relational Schema): `dbms/schema.sql`
- Unit II (SQL + Relational Algebra): `dbms/queries.sql`, `dbms/dbms-notes.md`
- Unit III (Normalization + Indexing + File Organization concepts): `dbms/schema.sql`, `dbms/dbms-notes.md`
- Unit IV (Transactions + Advanced Concepts): `dbms/queries.sql`, `dbms/schema.sql` (view), `dbms/dbms-notes.md`
- ER Diagram Artifact: `dbms/er-diagram.md` (Mermaid ER diagram rendered by GitHub)

## How The Application Works
1. The Java server starts on a configurable `PORT` and serves both API endpoints and static web files.
2. Frontend JavaScript sends requests to endpoints like `/api/students` for add, edit, delete, and fetch operations.
3. The backend uses JDBC with PostgreSQL/SQLite tables for students, departments, courses, instructors, enrollments, and results.
4. On startup, schema and indexes are auto-created and demo records are seeded if empty.
5. If JDBC driver is unavailable locally, the app safely falls back to in-memory mode.

## Key Validation Rules
- Enrollment number accepts only digits and supports long numeric values.
- CGPA supports decimal values with precision (up to 2 decimal places in UI).
- Department cannot contain numeric characters.
- Age must be in valid range.

## Project Structure
- src/main/java/com/student/Main.java - application entry point
- src/main/java/com/student/model/Student.java - student model
- src/main/java/com/student/service/StudentService.java - in-memory CRUD service
- src/main/java/com/student/service/StudentService.java - JDBC CRUD service with safe in-memory fallback
- src/main/java/com/student/server/StudentHttpServer.java - API routes + static file serving
- web/index.html - UI layout and form
- web/style.css - design and table/form styling
- web/app.js - frontend logic, API calls, and rendering
- dbms/schema.sql - complete normalized relational schema + indexes + view
- dbms/queries.sql - insert scripts, SQL query set, transaction example
- dbms/dbms-notes.md - ER mapping, relational algebra, normalization, file/index notes
- dbms/er-diagram.md - ER diagram for project report/submission
- render.yaml - Render service configuration
- Dockerfile - Docker build configuration for Render
- Procfile - process start command

## Features
- Add student record
- View all students in table format
- Edit existing student data
- Delete student record
- Search by name, department, or enrollment number
- Client + server side feedback for validation errors
- Full DBMS project-ready schema and SQL set for report/viva submission

## How to Use the Combined Version
1. Run the Java app as usual for web demonstration (UI + API + CRUD).
2. Use `dbms/schema.sql` to create the database schema in MySQL/PostgreSQL/any RDBMS with minor syntax adjustments.
3. Execute `dbms/queries.sql` for sample data, SQL operations, and transaction examples.
4. Use `dbms/dbms-notes.md` in your project report to explain normalization, relational algebra, and advanced DBMS concepts.

## Runtime Configuration
- `PORT`: server port (default `8080`)
- `DB_URL`: JDBC URL (overrides everything if set)
- `DATABASE_URL`: Render/Heroku-style PostgreSQL URL (auto-converted to JDBC)

Examples:
- Local default (SQLite): `DB_URL=jdbc:sqlite:student.db`
- Local custom SQLite file: `DB_URL=jdbc:sqlite:data/student.db`
- PostgreSQL JDBC form: `DB_URL=jdbc:postgresql://host:5432/dbname?sslmode=require&user=USER&password=PASS`
- Render style form: `DATABASE_URL=postgresql://USER:PASS@HOST:5432/dbname?sslmode=require`

## Deployment
Deploy this repository on Render as a Docker web service.
Render uses `render.yaml` + `Dockerfile`, so you do not need a Java option in the Render UI.
