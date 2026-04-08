-- Sample data manipulation and SQL query set for DBMS syllabus coverage.

-- ==========================
-- A. INSERT SAMPLE DATA
-- ==========================
INSERT INTO departments (department_id, department_name, office_room) VALUES
(1, 'Computer Science', 'A-101'),
(2, 'Information Technology', 'B-204'),
(3, 'Electronics', 'C-112');

INSERT INTO instructors (instructor_id, instructor_name, email, department_id) VALUES
(1, 'Dr. Meera Singh', 'meera.singh@college.edu', 1),
(2, 'Dr. Amit Rao', 'amit.rao@college.edu', 2);

INSERT INTO students (student_id, enrollment_no, student_name, age, date_of_birth, gender, email, phone, department_id, admission_year, cgpa) VALUES
(1, '2024001', 'Ananya Sharma', 20, '2005-01-19', 'F', 'ananya@college.edu', '9876500011', 1, 2024, 8.70),
(2, '2024002', 'Rohan Mehta', 21, '2004-08-12', 'M', 'rohan@college.edu', '9876500022', 2, 2024, 8.20),
(3, '2024003', 'Priya Verma', 19, '2005-03-07', 'F', 'priya@college.edu', '9876500033', 3, 2024, 9.10);

INSERT INTO courses (course_id, course_code, course_title, credits, department_id, instructor_id) VALUES
(1, 'CS101', 'Database Management Systems', 4, 1, 1),
(2, 'IT201', 'Web Technologies', 3, 2, 2),
(3, 'EC110', 'Digital Electronics', 4, 3, NULL);

INSERT INTO enrollments (enrollment_id, student_id, course_id, semester, academic_year, enrolled_on) VALUES
(1, 1, 1, 'Sem-1', '2025-26', '2025-08-01'),
(2, 2, 2, 'Sem-1', '2025-26', '2025-08-01'),
(3, 3, 3, 'Sem-1', '2025-26', '2025-08-01');

INSERT INTO results (result_id, enrollment_id, marks_obtained, grade, grade_points, published_on) VALUES
(1, 1, 88.50, 'A', 9.00, '2025-12-20'),
(2, 2, 81.00, 'B', 8.00, '2025-12-20'),
(3, 3, 93.00, 'A', 9.50, '2025-12-20');

-- ==========================
-- B. CORE SQL QUERIES (Unit II)
-- ==========================

-- 1. List all students with department name.
SELECT s.student_id, s.enrollment_no, s.student_name, d.department_name
FROM students s
JOIN departments d ON d.department_id = s.department_id
ORDER BY s.student_name;

-- 2. Find all students enrolled in a specific course.
SELECT s.student_name, c.course_title, e.semester
FROM enrollments e
JOIN students s ON s.student_id = e.student_id
JOIN courses c ON c.course_id = e.course_id
WHERE c.course_code = 'CS101';

-- 3. Department-wise student count.
SELECT d.department_name, COUNT(*) AS total_students
FROM students s
JOIN departments d ON d.department_id = s.department_id
GROUP BY d.department_name
ORDER BY total_students DESC;

-- 4. Students scoring above average marks.
SELECT s.student_name, r.marks_obtained
FROM results r
JOIN enrollments e ON e.enrollment_id = r.enrollment_id
JOIN students s ON s.student_id = e.student_id
WHERE r.marks_obtained > (
    SELECT AVG(marks_obtained) FROM results
);

-- 5. Student CGPA-style aggregate based on grade points.
SELECT s.student_id, s.student_name, ROUND(AVG(r.grade_points), 2) AS avg_grade_points
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN results r ON r.enrollment_id = e.enrollment_id
GROUP BY s.student_id, s.student_name
ORDER BY avg_grade_points DESC;

-- 6. Query from view.
SELECT *
FROM v_student_course_result
WHERE grade IN ('A', 'B')
ORDER BY student_name;

-- ==========================
-- C. TRANSACTION EXAMPLE (Unit IV)
-- ==========================
-- Generic transaction block; syntax can vary by database.
START TRANSACTION;

INSERT INTO enrollments (enrollment_id, student_id, course_id, semester, academic_year, enrolled_on)
VALUES (4, 1, 2, 'Sem-2', '2025-26', CURRENT_DATE);

INSERT INTO results (result_id, enrollment_id, marks_obtained, grade, grade_points, published_on)
VALUES (4, 4, 90.00, 'A', 9.25, CURRENT_DATE);

COMMIT;

-- If any statement fails in the block, use ROLLBACK instead of COMMIT.
