package com.student.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.student.model.Student;

public class StudentService {
    private enum DbDialect {
        SQLITE,
        POSTGRES
    }

    private final String dbUrl;
    private final DbDialect dbDialect;
    private final boolean allowInMemoryFallback;
    private final boolean seedDemoData;
    private final boolean databaseEnabled;
    private final List<Student> students = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public StudentService() {
        String configuredDbUrl = firstNonBlank(System.getenv("DB_URL"), System.getenv("DATABASE_URL"));
        this.allowInMemoryFallback = configuredDbUrl == null || configuredDbUrl.isBlank();
        this.seedDemoData = !"false".equalsIgnoreCase(firstNonBlank(System.getenv("SEED_DEMO_DATA"), "true"));
        this.dbUrl = normalizeDbUrl(configuredDbUrl);
        this.dbDialect = detectDialect(this.dbUrl);
        this.databaseEnabled = initializeDatabaseOrFallback();
    }

    public synchronized Student addStudent(String enrollmentNo, String name, int age, String department, double cgpa) {
        if (databaseEnabled) {
            return addStudentInDatabase(enrollmentNo, name, age, department, cgpa);
        }
        return addStudentInMemory(enrollmentNo, name, age, department, cgpa);
    }

    public synchronized List<Student> getAllStudents() {
        if (databaseEnabled) {
            return getAllStudentsFromDatabase();
        }
        return Collections.unmodifiableList(new ArrayList<>(students));
    }

    public synchronized Optional<Student> getById(int id) {
        if (databaseEnabled) {
            return getByIdFromDatabase(id);
        }
        return students.stream().filter(s -> s.getId() == id).findFirst();
    }

    public synchronized boolean updateStudent(int id, String enrollmentNo, String name, int age, String department, double cgpa) {
        if (databaseEnabled) {
            return updateStudentInDatabase(id, enrollmentNo, name, age, department, cgpa);
        }
        return updateStudentInMemory(id, enrollmentNo, name, age, department, cgpa);
    }

    public synchronized boolean deleteStudent(int id) {
        if (databaseEnabled) {
            return deleteStudentFromDatabase(id);
        }
        boolean removed = students.removeIf(s -> s.getId() == id);
        if (removed) {
            reindexIds();
        }
        return removed;
    }

    private Student addStudentInMemory(String enrollmentNo, String name, int age, String department, double cgpa) {
        String normalizedEnrollmentNo = normalize(enrollmentNo);
        String normalizedName = normalize(name);
        String normalizedDepartment = normalize(department);
        if (isEnrollmentNoUsed(normalizedEnrollmentNo, null)) {
            throw new IllegalArgumentException("Enrollment number already exists");
        }
        if (isDuplicate(normalizedName, age, normalizedDepartment, cgpa, null)) {
            throw new IllegalArgumentException("Duplicate student record not allowed");
        }

        Student student = new Student(
                nextId.getAndIncrement(),
                normalizedEnrollmentNo,
                normalizedName,
                age,
                normalizedDepartment,
                cgpa
        );
        students.add(student);
        return student;
    }

    private boolean updateStudentInMemory(int id, String enrollmentNo, String name, int age, String department, double cgpa) {
        Optional<Student> optionalStudent = getById(id);
        if (optionalStudent.isEmpty()) {
            return false;
        }

        String normalizedEnrollmentNo = normalize(enrollmentNo);
        String normalizedName = normalize(name);
        String normalizedDepartment = normalize(department);
        if (isEnrollmentNoUsed(normalizedEnrollmentNo, id)) {
            throw new IllegalArgumentException("Enrollment number already exists");
        }
        if (isDuplicate(normalizedName, age, normalizedDepartment, cgpa, id)) {
            throw new IllegalArgumentException("Duplicate student record not allowed");
        }

        Student student = optionalStudent.get();
        student.setEnrollmentNo(normalizedEnrollmentNo);
        student.setName(normalizedName);
        student.setAge(age);
        student.setDepartment(normalizedDepartment);
        student.setCgpa(cgpa);
        return true;
    }

    private void reindexIds() {
        for (int i = 0; i < students.size(); i++) {
            students.get(i).setId(i + 1);
        }
        nextId.set(students.size() + 1);
    }

    private boolean isDuplicate(String name, int age, String department, double cgpa, Integer excludeId) {
        for (Student student : students) {
            if (excludeId != null && student.getId() == excludeId) {
                continue;
            }

            boolean sameName = student.getName().equalsIgnoreCase(name);
            boolean sameAge = student.getAge() == age;
            boolean sameDepartment = student.getDepartment().equalsIgnoreCase(department);
            boolean sameCgpa = Double.compare(student.getCgpa(), cgpa) == 0;
            if (sameName && sameAge && sameDepartment && sameCgpa) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnrollmentNoUsed(String enrollmentNo, Integer excludeId) {
        for (Student student : students) {
            if (excludeId != null && student.getId() == excludeId) {
                continue;
            }
            if (student.getEnrollmentNo().equals(enrollmentNo)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private String normalizeDbUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "jdbc:sqlite:student.db";
        }

        String trimmed = raw.trim();
        if (trimmed.startsWith("jdbc:")) {
            return trimmed;
        }
        if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
            return toJdbcPostgresUrl(trimmed);
        }
        return "jdbc:sqlite:" + trimmed;
    }

    private DbDialect detectDialect(String url) {
        if (url.startsWith("jdbc:postgresql:")) {
            return DbDialect.POSTGRES;
        }
        return DbDialect.SQLITE;
    }

    private String toJdbcPostgresUrl(String url) {
        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            String[] credentials = userInfo == null ? new String[0] : userInfo.split(":", 2);
            String user = credentials.length > 0 ? credentials[0] : "";
            String password = credentials.length > 1 ? credentials[1] : "";
            String query = uri.getQuery();
            StringBuilder queryBuilder = new StringBuilder();
            if (query != null && !query.isBlank()) {
                queryBuilder.append(query);
            }
            if (queryBuilder.toString().toLowerCase().contains("sslmode=") == false) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append("sslmode=require");
            }
            if (!user.isEmpty()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append("user=").append(user);
            }
            if (!password.isEmpty()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append("password=").append(password);
            }
            String jdbc = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
            return queryBuilder.length() > 0 ? jdbc + "?" + queryBuilder : jdbc;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid DATABASE_URL format", ex);
        }
    }

    private boolean initializeDatabaseOrFallback() {
        try {
            Class.forName(dbDialect == DbDialect.POSTGRES ? "org.postgresql.Driver" : "org.sqlite.JDBC");
            initializeSchema();
            seedDatabaseIfEmpty();
            System.out.println("StudentService storage mode: " + dbDialect + " (" + dbUrl + ")");
            return true;
        } catch (ClassNotFoundException | SQLException ex) {
            if (!allowInMemoryFallback) {
                throw new RuntimeException("Configured database startup failed. Fix DB_URL/DATABASE_URL and restart.", ex);
            }
            seedInMemory();
            System.err.println("JDBC storage unavailable. Falling back to in-memory mode: " + ex.getMessage());
            return false;
        }
    }

    private void seedInMemory() {
        students.clear();
        nextId.set(1);
        if (!seedDemoData) {
            return;
        }
        students.add(new Student(nextId.getAndIncrement(), "2024001", "Ananya Sharma", 20, "Computer Science", 8.7));
        students.add(new Student(nextId.getAndIncrement(), "2024002", "Rohan Mehta", 21, "Information Technology", 8.2));
        students.add(new Student(nextId.getAndIncrement(), "2024003", "Priya Verma", 19, "Electronics", 9.1));
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(dbUrl);
        if (dbDialect == DbDialect.SQLITE) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    private void initializeSchema() throws SQLException {
        try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {
            if (dbDialect == DbDialect.POSTGRES) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS departments (
                        department_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        department_name VARCHAR(100) NOT NULL UNIQUE,
                        office_room VARCHAR(20)
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        student_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        enrollment_no VARCHAR(20) NOT NULL UNIQUE,
                        student_name VARCHAR(120) NOT NULL,
                        date_of_birth DATE,
                        gender VARCHAR(10),
                        email VARCHAR(150) UNIQUE,
                        phone VARCHAR(20),
                        department_id INTEGER NOT NULL,
                        admission_year INTEGER,
                        age INTEGER NOT NULL,
                        cgpa DOUBLE PRECISION NOT NULL,
                        FOREIGN KEY (department_id) REFERENCES departments(department_id)
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS instructors (
                        instructor_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        instructor_name VARCHAR(120) NOT NULL,
                        email VARCHAR(150) UNIQUE,
                        department_id INTEGER NOT NULL,
                        FOREIGN KEY (department_id) REFERENCES departments(department_id)
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS courses (
                        course_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        course_code VARCHAR(20) NOT NULL UNIQUE,
                        course_title VARCHAR(150) NOT NULL,
                        credits INTEGER NOT NULL,
                        department_id INTEGER NOT NULL,
                        instructor_id INTEGER,
                        FOREIGN KEY (department_id) REFERENCES departments(department_id),
                        FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS enrollments (
                        enrollment_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        student_id INTEGER NOT NULL,
                        course_id INTEGER NOT NULL,
                        semester VARCHAR(20) NOT NULL,
                        academic_year VARCHAR(20) NOT NULL,
                        enrolled_on DATE NOT NULL,
                        UNIQUE (student_id, course_id, semester, academic_year),
                        FOREIGN KEY (student_id) REFERENCES students(student_id),
                        FOREIGN KEY (course_id) REFERENCES courses(course_id)
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS results (
                        result_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        enrollment_id INTEGER NOT NULL UNIQUE,
                        marks_obtained DOUBLE PRECISION NOT NULL,
                        grade VARCHAR(2) NOT NULL,
                        grade_points DOUBLE PRECISION NOT NULL,
                        published_on DATE,
                        FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
                    )
                """);
            } else {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    department_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    department_name TEXT NOT NULL UNIQUE,
                    office_room TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    student_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    enrollment_no TEXT NOT NULL UNIQUE,
                    student_name TEXT NOT NULL,
                    date_of_birth DATE,
                    gender TEXT,
                    email TEXT UNIQUE,
                    phone TEXT,
                    department_id INTEGER NOT NULL,
                    admission_year INTEGER,
                    age INTEGER NOT NULL,
                    cgpa REAL NOT NULL,
                    FOREIGN KEY (department_id) REFERENCES departments(department_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS instructors (
                    instructor_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    instructor_name TEXT NOT NULL,
                    email TEXT UNIQUE,
                    department_id INTEGER NOT NULL,
                    FOREIGN KEY (department_id) REFERENCES departments(department_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    course_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_code TEXT NOT NULL UNIQUE,
                    course_title TEXT NOT NULL,
                    credits INTEGER NOT NULL,
                    department_id INTEGER NOT NULL,
                    instructor_id INTEGER,
                    FOREIGN KEY (department_id) REFERENCES departments(department_id),
                    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS enrollments (
                    enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    course_id INTEGER NOT NULL,
                    semester TEXT NOT NULL,
                    academic_year TEXT NOT NULL,
                    enrolled_on DATE NOT NULL,
                    UNIQUE (student_id, course_id, semester, academic_year),
                    FOREIGN KEY (student_id) REFERENCES students(student_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS results (
                    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    enrollment_id INTEGER NOT NULL UNIQUE,
                    marks_obtained REAL NOT NULL,
                    grade TEXT NOT NULL,
                    grade_points REAL NOT NULL,
                    published_on DATE,
                    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
                )
            """);
            }

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_students_department_id ON students(department_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_courses_department_id ON courses(department_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments(student_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_results_grade ON results(grade)");

            stmt.execute("DROP VIEW IF EXISTS v_student_course_result");
            stmt.execute("""
                CREATE VIEW v_student_course_result AS
                SELECT
                    s.student_id,
                    s.enrollment_no,
                    s.student_name,
                    d.department_name,
                    c.course_code,
                    c.course_title,
                    e.semester,
                    e.academic_year,
                    r.marks_obtained,
                    r.grade,
                    r.grade_points
                FROM students s
                JOIN departments d ON d.department_id = s.department_id
                JOIN enrollments e ON e.student_id = s.student_id
                JOIN courses c ON c.course_id = e.course_id
                LEFT JOIN results r ON r.enrollment_id = e.enrollment_id
            """);
        }
    }

    private void seedDatabaseIfEmpty() throws SQLException {
        if (!seedDemoData) {
            return;
        }
        String countSql = "SELECT COUNT(*) AS total FROM students";
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt("total") > 0) {
                return;
            }
        }

        addStudentInDatabase("2024001", "Ananya Sharma", 20, "Computer Science", 8.7);
        addStudentInDatabase("2024002", "Rohan Mehta", 21, "Information Technology", 8.2);
        addStudentInDatabase("2024003", "Priya Verma", 19, "Electronics", 9.1);
    }

    private Student addStudentInDatabase(String enrollmentNo, String name, int age, String department, double cgpa) {
        String normalizedEnrollmentNo = normalize(enrollmentNo);
        String normalizedName = normalize(name);
        String normalizedDepartment = normalize(department);

        if (normalizedEnrollmentNo.isEmpty() || normalizedName.isEmpty() || normalizedDepartment.isEmpty()) {
            throw new IllegalArgumentException("All fields are required");
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int departmentId = getOrCreateDepartmentId(connection, normalizedDepartment);

                String insertSql = """
                    INSERT INTO students
                    (enrollment_no, student_name, date_of_birth, department_id, admission_year, age, cgpa)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

                try (PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, normalizedEnrollmentNo);
                    ps.setString(2, normalizedName);
                    ps.setDate(3, Date.valueOf(LocalDate.of(2000, 1, 1)));
                    ps.setInt(4, departmentId);
                    ps.setInt(5, LocalDate.now().getYear());
                    ps.setInt(6, age);
                    ps.setDouble(7, cgpa);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            int id = keys.getInt(1);
                            connection.commit();
                            return new Student(id, normalizedEnrollmentNo, normalizedName, age, normalizedDepartment, cgpa);
                        }
                    }
                }

                connection.rollback();
                throw new IllegalStateException("Could not create student");
            } catch (SQLException ex) {
                connection.rollback();
                if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique")) {
                    throw new IllegalArgumentException("Enrollment number already exists");
                }
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error while adding student", ex);
        }
    }

    private List<Student> getAllStudentsFromDatabase() {
        String sql = """
            SELECT s.student_id, s.enrollment_no, s.student_name, s.age, d.department_name, s.cgpa
            FROM students s
            JOIN departments d ON d.department_id = s.department_id
            ORDER BY s.student_id
        """;

        List<Student> result = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("enrollment_no"),
                        rs.getString("student_name"),
                        rs.getInt("age"),
                        rs.getString("department_name"),
                        rs.getDouble("cgpa")
                ));
            }
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException("Database error while reading students", ex);
        }
    }

    private Optional<Student> getByIdFromDatabase(int id) {
        String sql = """
            SELECT s.student_id, s.enrollment_no, s.student_name, s.age, d.department_name, s.cgpa
            FROM students s
            JOIN departments d ON d.department_id = s.department_id
            WHERE s.student_id = ?
        """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Student(
                        rs.getInt("student_id"),
                        rs.getString("enrollment_no"),
                        rs.getString("student_name"),
                        rs.getInt("age"),
                        rs.getString("department_name"),
                        rs.getDouble("cgpa")
                ));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error while reading student", ex);
        }
    }

    private boolean updateStudentInDatabase(int id, String enrollmentNo, String name, int age, String department, double cgpa) {
        String normalizedEnrollmentNo = normalize(enrollmentNo);
        String normalizedName = normalize(name);
        String normalizedDepartment = normalize(department);

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int departmentId = getOrCreateDepartmentId(connection, normalizedDepartment);
                String updateSql = """
                    UPDATE students
                    SET enrollment_no = ?, student_name = ?, department_id = ?, age = ?, cgpa = ?
                    WHERE student_id = ?
                """;
                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setString(1, normalizedEnrollmentNo);
                    ps.setString(2, normalizedName);
                    ps.setInt(3, departmentId);
                    ps.setInt(4, age);
                    ps.setDouble(5, cgpa);
                    ps.setInt(6, id);
                    int updatedRows = ps.executeUpdate();
                    connection.commit();
                    return updatedRows > 0;
                }
            } catch (SQLException ex) {
                connection.rollback();
                if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique")) {
                    throw new IllegalArgumentException("Enrollment number already exists");
                }
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error while updating student", ex);
        }
    }

    private boolean deleteStudentFromDatabase(int id) {
        String deleteSql = "DELETE FROM students WHERE student_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Database error while deleting student", ex);
        }
    }

    private int getOrCreateDepartmentId(Connection connection, String departmentName) throws SQLException {
        String selectSql = "SELECT department_id FROM departments WHERE department_name = ?";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, departmentName);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("department_id");
                }
            }
        }

        String insertSql = "INSERT INTO departments(department_name) VALUES(?)";
        try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, departmentName);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, departmentName);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("department_id");
                }
            }
        }

        throw new IllegalStateException("Could not create department");
    }
}
