-- Student Information Management System
-- DBMS syllabus-aligned schema (Unit I-IV)
-- Compatible with common SQL databases with minor syntax adjustments.

-- ==========================
-- 1. MASTER TABLES
-- ==========================
CREATE TABLE departments (
    department_id INT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    office_room VARCHAR(20)
);

CREATE TABLE students (
    student_id INT PRIMARY KEY,
    enrollment_no VARCHAR(20) NOT NULL UNIQUE,
    student_name VARCHAR(120) NOT NULL,
    age INT NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10),
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20),
    department_id INT NOT NULL,
    admission_year INT NOT NULL,
    cgpa DECIMAL(3,2) NOT NULL,
    CONSTRAINT fk_student_department
        FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE instructors (
    instructor_id INT PRIMARY KEY,
    instructor_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) UNIQUE,
    department_id INT NOT NULL,
    CONSTRAINT fk_instructor_department
        FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE courses (
    course_id INT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_title VARCHAR(150) NOT NULL,
    credits INT NOT NULL,
    department_id INT NOT NULL,
    instructor_id INT,
    CONSTRAINT fk_course_department
        FOREIGN KEY (department_id) REFERENCES departments(department_id),
    CONSTRAINT fk_course_instructor
        FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
);

-- ==========================
-- 2. RELATION TABLES
-- ==========================
CREATE TABLE enrollments (
    enrollment_id INT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    enrolled_on DATE NOT NULL,
    CONSTRAINT uq_student_course_sem UNIQUE (student_id, course_id, semester, academic_year),
    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE TABLE results (
    result_id INT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    marks_obtained DECIMAL(5,2) NOT NULL,
    grade VARCHAR(2) NOT NULL,
    grade_points DECIMAL(3,2) NOT NULL,
    published_on DATE,
    CONSTRAINT fk_result_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

-- ==========================
-- 3. INDEXING (Unit III)
-- ==========================
CREATE INDEX idx_students_department_id ON students(department_id);
CREATE INDEX idx_courses_department_id ON courses(department_id);
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_results_grade ON results(grade);

-- ==========================
-- 4. VIEW (Advanced concept)
-- ==========================
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
LEFT JOIN results r ON r.enrollment_id = e.enrollment_id;
