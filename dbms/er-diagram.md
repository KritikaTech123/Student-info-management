# ER Diagram - Student Information Management System

```mermaid
erDiagram
    DEPARTMENTS ||--o{ STUDENTS : has
    DEPARTMENTS ||--o{ COURSES : offers
    DEPARTMENTS ||--o{ INSTRUCTORS : contains
    INSTRUCTORS ||--o{ COURSES : teaches
    STUDENTS ||--o{ ENROLLMENTS : registers
    COURSES ||--o{ ENROLLMENTS : has
    ENROLLMENTS ||--|| RESULTS : produces

    DEPARTMENTS {
        int department_id PK
        varchar department_name UK
        varchar office_room
    }

    STUDENTS {
        int student_id PK
        varchar enrollment_no UK
        varchar student_name
        int age
        date date_of_birth
        varchar gender
        varchar email UK
        varchar phone
        int department_id FK
        int admission_year
        decimal cgpa
    }

    INSTRUCTORS {
        int instructor_id PK
        varchar instructor_name
        varchar email UK
        int department_id FK
    }

    COURSES {
        int course_id PK
        varchar course_code UK
        varchar course_title
        int credits
        int department_id FK
        int instructor_id FK
    }

    ENROLLMENTS {
        int enrollment_id PK
        int student_id FK
        int course_id FK
        varchar semester
        varchar academic_year
        date enrolled_on
    }

    RESULTS {
        int result_id PK
        int enrollment_id FK_UK
        decimal marks_obtained
        varchar grade
        decimal grade_points
        date published_on
    }
```

GitHub renders this Mermaid diagram directly, so this file acts as an ER diagram artifact for report submission.
