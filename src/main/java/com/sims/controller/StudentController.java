package com.sims.controller;

import com.sims.dao.StudentDAO;
import com.sims.model.Student;
import com.sims.util.DBConnection;
import com.sims.util.ValidationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Controller managing student business logic, input validation, and JDBC transactions.
 *
 * <h2>Design Pattern – Controller (MVC)</h2>
 * <p><b>Academic Justification</b>: Acts as the intermediary between the presentation
 * layer (Swing GUI) and data access layer (DAO). All domain rules, regex validations,
 * duplicate record checks, and transaction boundaries (commit/rollback) are enforced here.</p>
 */
public class StudentController {

    private final StudentDAO studentDAO;
    private final Connection conn;

    // ── Validation Regular Expressions ───────────────────────────────────────
    private static final Pattern ROLL_REGEX =
            Pattern.compile("^[A-Za-z0-9\\-/]{3,20}$");

    private static final Pattern NAME_REGEX =
            Pattern.compile("^[A-Za-z\\s.']{2,100}$");

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w]{2,})+$");

    private static final Pattern PHONE_REGEX =
            Pattern.compile("^(?:\\+91)?[6-9]\\d{9}$");

    public StudentController() {
        this.conn = DBConnection.getInstance().getConnection();
        this.studentDAO = new StudentDAO();
    }

    // ── Business Operations ──────────────────────────────────────────────────

    /**
     * Validates and inserts a new student record into the database.
     *
     * @param student new student entity
     * @throws ValidationException if any input field fails validation rules
     * @throws RuntimeException on database or transaction failure
     */
    public void addStudent(Student student) throws ValidationException {
        validateStudent(student, true);

        try {
            // Check for duplicate Roll Number
            Optional<Student> existingRoll = studentDAO.findByRollNumber(student.getRollNumber());
            if (existingRoll.isPresent()) {
                throw new ValidationException("Roll Number", "A student with Roll Number '" + student.getRollNumber() + "' already exists.");
            }

            studentDAO.insert(student);
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new RuntimeException("Failed to add student: " + e.getMessage(), e);
        }
    }

    /**
     * Validates and updates an existing student record.
     *
     * @param student student entity with updated fields
     * @throws ValidationException if input validation fails
     * @throws RuntimeException on database or transaction failure
     */
    public void updateStudent(Student student) throws ValidationException {
        if (student.getStudentId() == null || student.getStudentId() <= 0) {
            throw new ValidationException("Student ID", "Invalid Student ID for update operation.");
        }

        validateStudent(student, false);

        try {
            int rows = studentDAO.update(student);
            if (rows == 0) {
                rollbackQuietly();
                throw new RuntimeException("Student record with ID " + student.getStudentId() + " was not found.");
            }
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new RuntimeException("Failed to update student: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a student by internal ID.
     *
     * @param studentId primary key ID
     * @throws RuntimeException on database or transaction failure
     */
    public void deleteStudent(long studentId) {
        try {
            int rows = studentDAO.delete(studentId);
            if (rows == 0) {
                rollbackQuietly();
                throw new RuntimeException("No student found with ID " + studentId);
            }
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new RuntimeException("Failed to delete student: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a student by unique roll number.
     *
     * @param rollNumber institutional roll number
     */
    public void deleteStudentByRoll(String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Roll Number cannot be empty.");
        }
        try {
            int rows = studentDAO.deleteByRollNumber(rollNumber.trim());
            if (rows == 0) {
                rollbackQuietly();
                throw new RuntimeException("No student found with Roll Number " + rollNumber);
            }
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new RuntimeException("Failed to delete student: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all students.
     *
     * @return list of all students
     */
    public List<Student> getAllStudents() {
        try {
            return studentDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch students: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for students by keyword (matches roll number, name, dept, email, phone).
     *
     * @param keyword search keyword
     * @return matching students
     */
    public List<Student> searchStudents(String keyword) {
        try {
            return studentDAO.search(keyword);
        } catch (SQLException e) {
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Exports the given list of students to a clean comma-separated values (CSV) file.
     *
     * @param students list of students to export
     * @param file target destination file
     * @throws IOException on I/O error
     */
    public void exportToCsv(List<Student> students, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // CSV Header
            writer.write("Student ID,Roll Number,Full Name,Email,Phone,Department,Year,Section,Gender,Date of Birth,Address");
            writer.newLine();

            for (Student s : students) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"",
                        s.getStudentId() != null ? s.getStudentId() : 0,
                        escapeCsv(s.getRollNumber()),
                        escapeCsv(s.getFullName()),
                        escapeCsv(s.getEmail()),
                        escapeCsv(s.getPhone()),
                        escapeCsv(s.getDepartment()),
                        s.getYear(),
                        escapeCsv(s.getSection()),
                        escapeCsv(s.getGender()),
                        s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "",
                        escapeCsv(s.getAddress())
                ));
                writer.newLine();
            }
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }

    // ── Input Validation Rules ───────────────────────────────────────────────

    /**
     * Performs comprehensive field-level validation against business requirements.
     *
     * @param s student entity
     * @param isNew whether this is a new record insertion
     * @throws ValidationException on any rule violation
     */
    public void validateStudent(Student s, boolean isNew) throws ValidationException {
        if (s == null) {
            throw new ValidationException("Payload", "Student record cannot be null.");
        }

        // 1. Roll Number
        if (s.getRollNumber() == null || s.getRollNumber().trim().isEmpty()) {
            throw new ValidationException("Roll Number", "Roll Number is required and cannot be empty.");
        }
        String cleanRoll = s.getRollNumber().trim();
        if (!ROLL_REGEX.matcher(cleanRoll).matches()) {
            throw new ValidationException("Roll Number", "Invalid Roll Number format. Must be 3-20 alphanumeric characters (e.g. 24IT001).");
        }

        // 2. Full Name
        if (s.getFullName() == null || s.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full Name", "Student Full Name is required.");
        }
        String cleanName = s.getFullName().trim();
        if (!NAME_REGEX.matcher(cleanName).matches()) {
            throw new ValidationException("Full Name", "Student Full Name must contain only alphabetical characters, spaces, and periods (2-100 characters).");
        }

        // 3. Email
        if (s.getEmail() == null || s.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email", "Email address is required.");
        }
        String cleanEmail = s.getEmail().trim();
        if (!EMAIL_REGEX.matcher(cleanEmail).matches()) {
            throw new ValidationException("Email", "Invalid email address format (e.g. student@college.edu).");
        }

        // 4. Phone
        if (s.getPhone() == null || s.getPhone().trim().isEmpty()) {
            throw new ValidationException("Phone", "Mobile phone number is required.");
        }
        String cleanPhone = s.getPhone().trim().replaceAll("\\s+", "");
        if (!PHONE_REGEX.matcher(cleanPhone).matches()) {
            throw new ValidationException("Phone", "Invalid phone number. Must be a valid 10-digit mobile number starting with 6-9.");
        }

        // 5. Department
        if (s.getDepartment() == null || s.getDepartment().trim().isEmpty()) {
            throw new ValidationException("Department", "Please select a valid academic department.");
        }

        // 6. Year
        if (s.getYear() < 1 || s.getYear() > 5) {
            throw new ValidationException("Year", "Academic year must be between 1 and 5.");
        }

        // 7. Date of Birth & Age Check
        if (s.getDateOfBirth() != null) {
            if (s.getDateOfBirth().isAfter(LocalDate.now())) {
                throw new ValidationException("Date of Birth", "Date of Birth cannot be a future date.");
            }
            int age = Period.between(s.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 15 || age > 70) {
                throw new ValidationException("Date of Birth", "Student age must be between 15 and 70 years (calculated age: " + age + ").");
            }
        }
    }

    private void rollbackQuietly() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
            }
        } catch (SQLException ignored) {
        }
    }
}
