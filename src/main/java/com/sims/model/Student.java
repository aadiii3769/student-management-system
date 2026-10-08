package com.sims.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Encapsulates a student academic and personal record.
 *
 * <h2>Design Pattern – Model (MVC)</h2>
 * <p><b>Academic Justification</b>: Acts as a pure Plain Old Java Object (POJO)
 * representing the domain entity without any presentation or persistence logic.
 * Encapsulation ensures safe data transfer across Controller, DAO, and View layers.</p>
 */
public class Student {

    private Long studentId;
    private String rollNumber;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private int year;
    private String section;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;

    public Student() {
    }

    public Student(String rollNumber, String fullName, String email, String phone,
                   String department, int year, String section, String gender,
                   LocalDate dateOfBirth, String address) {
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.year = year;
        this.section = section;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(rollNumber, student.rollNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rollNumber);
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", rollNumber='" + rollNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                ", year=" + year +
                '}';
    }
}
