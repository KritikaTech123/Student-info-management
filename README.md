# Student Information System (Java + HTML/CSS/JS)

This project demonstrates core programming concepts:
- Variables and data types (int, double, String)
- Classes and objects (Student, StudentService, StudentHttpServer)
- In-memory data management (no SQL database)

## Tech Stack
- Java (backend + HTTP server)
- HTML, CSS, JavaScript (frontend)
- No SQL / no external database

## Project Structure

- src/main/java/com/student/model/Student.java - student entity class
- src/main/java/com/student/service/StudentService.java - CRUD operations in memory
- src/main/java/com/student/server/StudentHttpServer.java - API + static file server
- src/main/java/com/student/Main.java - app entry point
- web/index.html - UI layout
- web/style.css - styling
- web/app.js - frontend logic and API calls
- 
ender.yaml - Render deployment config

## Features

- Add student
- View all students
- Edit student
- Delete student
- Search by name/department
- Form validation on server-side

## Deploy

1. Push this project to GitHub.
2. Create a new Render Web Service from your repo.
3. Render auto-detects 
ender.yaml and deploys automatically.
4. Once deployed, you'll get a Render URL.

## Live Demo

[Visit Live App](#) - Add Render URL here after deployment
