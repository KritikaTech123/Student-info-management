package com.student.service;

import com.student.model.Student;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentService {
    private final List<Student> students = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public StudentService() {
    }

    public synchronized Student addStudent(String enrollmentNo, String name, int age, String department, double cgpa) {
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

    public synchronized List<Student> getAllStudents() {
        return Collections.unmodifiableList(new ArrayList<>(students));
    }

    public synchronized Optional<Student> getById(int id) {
        return students.stream().filter(s -> s.getId() == id).findFirst();
    }

    public synchronized boolean updateStudent(int id, String enrollmentNo, String name, int age, String department, double cgpa) {
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

    public synchronized boolean deleteStudent(int id) {
        boolean removed = students.removeIf(s -> s.getId() == id);
        if (removed) {
            reindexIds();
        }
        return removed;
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
}
