package com.student.model;

public class Student {
    private int id;
    private String enrollmentNo;
    private String name;
    private int age;
    private String department;
    private double cgpa;

    public Student(int id, String enrollmentNo, String name, int age, String department, double cgpa) {
        this.id = id;
        this.enrollmentNo = enrollmentNo;
        this.name = name;
        this.age = age;
        this.department = department;
        this.cgpa = cgpa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getDepartment() {
        return department;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }
}
