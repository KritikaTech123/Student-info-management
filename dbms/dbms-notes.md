# DBMS Mapping for Student Information Management

This document maps the project to a complete DBMS syllabus (Unit I-IV).

## Unit I: ER Modeling and Relational Model
- Entities: Student, Department, Instructor, Course, Enrollment, Result.
- Key relationships:
  - Department 1:N Students
  - Department 1:N Courses
  - Student M:N Course (resolved using Enrollment)
  - Enrollment 1:1 Result
- ER to relational conversion is implemented in [schema.sql](schema.sql).

## Unit II: SQL and Relational Algebra
- SQL coverage includes DDL, DML, joins, group-by, subqueries, views, and aggregate functions in [queries.sql](queries.sql).
- Example relational algebra formulations:
  - Student with departments:
    - $\pi_{student\_id, student\_name, department\_name}(Students \bowtie Departments)$
  - Top scorers with marks > avg:
    - $\pi_{student\_name, marks}(Results \bowtie Enrollments \bowtie Students)\;\sigma_{marks > avg(marks)}$

## Unit III: Normalization, File Organization, Indexing
- Normalization status:
  - 1NF: Atomic attributes (no repeating groups).
  - 2NF: Non-key attributes depend on whole keys in associative tables.
  - 3NF/BCNF: Transitive dependencies separated into Department, Course, Instructor tables.
- Indexing choices in [schema.sql](schema.sql):
  - department-based lookup indexes
  - enrollment student/course indexes
  - grade index for result analytics
- File organization concept:
  - Heap table defaults for write-heavy inserts
  - Secondary B-tree indexes for fast predicate search

## Unit IV: Transactions and Advanced Concepts
- ACID transaction sample included in [queries.sql](queries.sql).
- View `v_student_course_result` demonstrates logical data abstraction.
- Optional extensions (advanced DBMS):
  - Stored procedures for bulk enrollment
  - Trigger to auto-calculate grade from marks
  - Partitioning enrollment/results by academic year

## How to use with this repository
1. Keep current Java web app for frontend/backend behavior demo.
2. Use SQL files in `dbms/` as the complete DBMS design and query layer for documentation/viva/project file.
3. If needed, migrate Java service from in-memory list to JDBC using this schema.
