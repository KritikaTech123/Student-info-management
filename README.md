# Student Information Management System

## Live Link
[Visit Live App](#) - Add Railway URL here after deployment

## Project Overview
This is a full-stack Student Information Management System built with Java for backend logic and HTML/CSS/JavaScript for the user interface. The application supports complete CRUD operations without using any SQL database.

Student data is managed in server memory using Java collections. This keeps the project simple for learning and rapid development, while still demonstrating clean API design, validation rules, and frontend-backend integration.

## Tech Stack
- Java (HTTP server and REST-style API)
- HTML (page structure)
- CSS (UI styling)
- JavaScript (frontend interactivity and API calls)
- No SQL database (in-memory storage)

## How The Application Works
1. The Java server starts on a configurable `PORT` and serves both API endpoints and static web files.
2. Frontend JavaScript sends requests to endpoints like `/api/students` for add, edit, delete, and fetch operations.
3. Server-side validation enforces required rules for age, enrollment number, CGPA, and department format.
4. Data is stored in memory, so records reset when the server restarts.

## Key Validation Rules
- Enrollment number accepts only digits and supports long numeric values.
- CGPA supports decimal values with precision (up to 2 decimal places in UI).
- Department cannot contain numeric characters.
- Age must be in valid range.

## Project Structure
- src/main/java/com/student/Main.java - application entry point
- src/main/java/com/student/model/Student.java - student model
- src/main/java/com/student/service/StudentService.java - in-memory CRUD service
- src/main/java/com/student/server/StudentHttpServer.java - API routes + static file serving
- web/index.html - UI layout and form
- web/style.css - design and table/form styling
- web/app.js - frontend logic, API calls, and rendering

## Features
- Add student record
- View all students in table format
- Edit existing student data
- Delete student record
- Search by name, department, or enrollment number
- Client + server side feedback for validation errors

## Deployment
Deploy this repository on Railway as a Java web service.
After deployment, update the Live Link section at the top with your public URL.
