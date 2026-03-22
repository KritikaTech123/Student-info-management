# Student Information System (Java + HTML/CSS/JS)

This project demonstrates core programming concepts:
- Variables and data types (`int`, `double`, `String`)
- Classes and objects (`Student`, `StudentService`, `StudentHttpServer`)
- In-memory data management (no SQL database)

## Tech Stack
- Java (backend + HTTP server)
- HTML, CSS, JavaScript (frontend)
- No SQL / no external database

## Project Structure

- `src/main/java/com/student/model/Student.java` - student entity class
- `src/main/java/com/student/service/StudentService.java` - CRUD operations in memory
- `src/main/java/com/student/server/StudentHttpServer.java` - API + static file server
- `src/main/java/com/student/Main.java` - app entry point
- `web/index.html` - UI layout
- `web/style.css` - styling
- `web/app.js` - frontend logic and API calls
- `render.yaml` - Render deployment config

## How to Run

From the project root folder:

```powershell
javac -d out src/main/java/com/student/model/*.java src/main/java/com/student/service/*.java src/main/java/com/student/server/*.java src/main/java/com/student/Main.java
java -cp out com.student.Main
```

Open browser:

- `http://localhost:8080`

## Deploy (Render Example)

1. Push this project to GitHub.
2. Create a new Render Web Service from your repo.
3. Render auto-detects `render.yaml` (or set commands manually):

```bash
mkdir -p out && javac -d out $(find src/main/java -name "*.java")
java -cp out com.student.Main
```

4. Deploy and open your Render URL.

## Features

- Add student
- View all students
- Edit student
- Delete student
- Search by name/department
- Form validation on server-side

## Notes

- Data is stored in memory and resets whenever the app restarts.
- This is intentionally database-free to match your requirement.
