package com.sims.dao;

import com.sims.model.Student;
import com.sims.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for student records in the {@code STUDENTS} table.
 *
 * <h2>Design Pattern – Structural: Data Access Object (DAO)</h2>
 * <p><b>Academic Justification</b>: The DAO pattern separates low-level data accessing
 * logic (SQL statements, JDBC PreparedStatements, ResultSet mappings) from high-level
 * business logic (Controller) and presentation (View). Changes to the underlying database
 * schema or SQL queries only affect this class without propagating to UI layers.</p>
 *
 * <h2>JDBC Standards Followed</h2>
 * <ul>
 *   <li>All queries use parameterised {@link PreparedStatement} to prevent SQL Injection.</li>
 *   <li>All statements and result sets use {@code try-with-resources} for automatic resource management.</li>
 *   <li>DAOs execute DML but do not commit; the calling Controller handles transaction commit/rollback.</li>
 * </ul>
 */
public class StudentDAO {

    private final Connection conn;

    public StudentDAO() {
        this.conn = DBConnection.getInstance().getConnection();
        ensureTableExists();
    }

    // ── SQL Queries ──────────────────────────────────────────────────────────

    private static final String SQL_INSERT =
            "INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, " +
            "                      YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE STUDENTS SET FULL_NAME = ?, EMAIL = ?, PHONE = ?, DEPARTMENT = ?, " +
            "                   YEAR = ?, SECTION = ?, GENDER = ?, DATE_OF_BIRTH = ?, ADDRESS = ? " +
            "WHERE STUDENT_ID = ?";

    private static final String SQL_DELETE =
            "DELETE FROM STUDENTS WHERE STUDENT_ID = ?";

    private static final String SQL_DELETE_BY_ROLL =
            "DELETE FROM STUDENTS WHERE ROLL_NUMBER = ?";

    private static final String SQL_FIND_ALL =
            "SELECT STUDENT_ID, ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, " +
            "       YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS " +
            "FROM STUDENTS ORDER BY ROLL_NUMBER ASC";

    private static final String SQL_FIND_BY_ID =
            "SELECT STUDENT_ID, ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, " +
            "       YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS " +
            "FROM STUDENTS WHERE STUDENT_ID = ?";

    private static final String SQL_FIND_BY_ROLL =
            "SELECT STUDENT_ID, ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, " +
            "       YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS " +
            "FROM STUDENTS WHERE ROLL_NUMBER = ?";

    private static final String SQL_SEARCH =
            "SELECT STUDENT_ID, ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, " +
            "       YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS " +
            "FROM STUDENTS " +
            "WHERE LOWER(ROLL_NUMBER) LIKE ? " +
            "   OR LOWER(FULL_NAME) LIKE ? " +
            "   OR LOWER(DEPARTMENT) LIKE ? " +
            "   OR LOWER(EMAIL) LIKE ? " +
            "   OR PHONE LIKE ? " +
            "ORDER BY ROLL_NUMBER ASC";

    // ── CRUD Operations ──────────────────────────────────────────────────────

    /**
     * Inserts a new student record into the database.
     *
     * @param s student entity
     * @throws SQLException on database error
     */
    public void insert(Student s) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, new String[]{"STUDENT_ID"})) {
            ps.setString(1, s.getRollNumber().trim());
            ps.setString(2, s.getFullName().trim());
            ps.setString(3, s.getEmail().trim().toLowerCase());
            ps.setString(4, s.getPhone().trim());
            ps.setString(5, s.getDepartment());
            ps.setInt(6, s.getYear());
            ps.setString(7, s.getSection() != null ? s.getSection().trim() : null);
            ps.setString(8, s.getGender());

            if (s.getDateOfBirth() != null) {
                ps.setDate(9, Date.valueOf(s.getDateOfBirth()));
            } else {
                ps.setNull(9, Types.DATE);
            }

            ps.setString(10, s.getAddress() != null ? s.getAddress().trim() : null);

            ps.executeUpdate();

            // Retrieve generated primary key
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setStudentId(rs.getLong(1));
                }
            }
        }
    }

    /**
     * Updates an existing student's personal and academic details.
     *
     * @param s student entity containing a valid studentId
     * @return number of affected rows (1 if successful, 0 if not found)
     * @throws SQLException on database error
     */
    public int update(Student s) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, s.getFullName().trim());
            ps.setString(2, s.getEmail().trim().toLowerCase());
            ps.setString(3, s.getPhone().trim());
            ps.setString(4, s.getDepartment());
            ps.setInt(5, s.getYear());
            ps.setString(6, s.getSection() != null ? s.getSection().trim() : null);
            ps.setString(7, s.getGender());

            if (s.getDateOfBirth() != null) {
                ps.setDate(8, Date.valueOf(s.getDateOfBirth()));
            } else {
                ps.setNull(8, Types.DATE);
            }

            ps.setString(9, s.getAddress() != null ? s.getAddress().trim() : null);
            ps.setLong(10, s.getStudentId());

            return ps.executeUpdate();
        }
    }

    /**
     * Deletes a student record by internal primary key ID.
     *
     * @param studentId the primary key
     * @return number of affected rows
     * @throws SQLException on database error
     */
    public int delete(long studentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, studentId);
            return ps.executeUpdate();
        }
    }

    /**
     * Deletes a student record by roll number.
     *
     * @param rollNumber the institutional roll number
     * @return number of affected rows
     * @throws SQLException on database error
     */
    public int deleteByRollNumber(String rollNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BY_ROLL)) {
            ps.setString(1, rollNumber);
            return ps.executeUpdate();
        }
    }

    /**
     * Retrieves all student records ordered by roll number.
     *
     * @return list of students, never null
     * @throws SQLException on database error
     */
    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Finds a single student by primary key ID.
     *
     * @param studentId primary key
     * @return Optional containing student if found
     * @throws SQLException on database error
     */
    public Optional<Student> findById(long studentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a single student by unique roll number.
     *
     * @param rollNumber institutional roll number
     * @return Optional containing student if found
     * @throws SQLException on database error
     */
    public Optional<Student> findByRollNumber(String rollNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ROLL)) {
            ps.setString(1, rollNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Searches for students matching a keyword across roll number, name, department, email, or phone.
     *
     * @param keyword search keyword
     * @return list of matching students
     * @throws SQLException on database error
     */
    public List<Student> search(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        List<Student> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // ── Helper Mappings & Auto-Init ──────────────────────────────────────────

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getLong("STUDENT_ID"));
        s.setRollNumber(rs.getString("ROLL_NUMBER"));
        s.setFullName(rs.getString("FULL_NAME"));
        s.setEmail(rs.getString("EMAIL"));
        s.setPhone(rs.getString("PHONE"));
        s.setDepartment(rs.getString("DEPARTMENT"));
        s.setYear(rs.getInt("YEAR"));
        s.setSection(rs.getString("SECTION"));
        s.setGender(rs.getString("GENDER"));

        Date dob = rs.getDate("DATE_OF_BIRTH");
        if (dob != null) {
            s.setDateOfBirth(dob.toLocalDate());
        }

        s.setAddress(rs.getString("ADDRESS"));
        return s;
    }

    /**
     * Automatic table initialisation: ensures the STUDENTS table exists in the connected
     * database and seeds sample records if empty, so the app runs immediately out-of-the-box.
     */
    private void ensureTableExists() {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "STUDENTS", new String[]{"TABLE"})) {
                if (!rs.next()) {
                    // Try lowercase/all-caps check
                    try (ResultSet rs2 = meta.getTables(null, null, "students", new String[]{"TABLE"})) {
                        if (!rs2.next()) {
                            createTableAndSeed();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Best effort table creation
            try {
                createTableAndSeed();
            } catch (Exception ignored) {
            }
        }
    }

    private void createTableAndSeed() {
        String createSql =
                "CREATE TABLE STUDENTS (" +
                "    STUDENT_ID    NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY PRIMARY KEY," +
                "    ROLL_NUMBER   VARCHAR2(20) NOT NULL UNIQUE," +
                "    FULL_NAME     VARCHAR2(100) NOT NULL," +
                "    EMAIL         VARCHAR2(100) NOT NULL UNIQUE," +
                "    PHONE         VARCHAR2(15) NOT NULL," +
                "    DEPARTMENT    VARCHAR2(80) NOT NULL," +
                "    YEAR          NUMBER(1) NOT NULL," +
                "    SECTION       VARCHAR2(5)," +
                "    GENDER        VARCHAR2(10)," +
                "    DATE_OF_BIRTH DATE," +
                "    ADDRESS       VARCHAR2(250)," +
                "    CREATED_AT    TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL" +
                ")";

        try (Statement st = conn.createStatement()) {
            st.execute(createSql);
            conn.commit();

            // Insert initial demonstration records
            seedDemoData();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void seedDemoData() {
        String[] seeds = {
            "INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) VALUES ('24IT001', 'Aarav Sharma', 'aarav.sharma@example.com', '9876543210', 'B.Tech Information Technology', 3, 'A', 'Male', TO_DATE('2005-04-12','YYYY-MM-DD'), '12 Anna Salai, Chennai, TN')",
            "INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) VALUES ('24IT002', 'Diya Ramesh', 'diya.ramesh@example.com', '9876543211', 'B.Tech Information Technology', 3, 'A', 'Female', TO_DATE('2005-08-23','YYYY-MM-DD'), '45 OMR Road, Chennai, TN')",
            "INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) VALUES ('24CS015', 'Karthik Subramanian', 'karthik.s@example.com', '9876543212', 'B.E. Computer Science and Engineering', 2, 'B', 'Male', TO_DATE('2006-01-15','YYYY-MM-DD'), '88 Gandhi Nagar, Coimbatore, TN')",
            "INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) VALUES ('24CS042', 'Meera Krishnan', 'meera.k@example.com', '9876543213', 'B.E. Computer Science and Engineering', 2, 'A', 'Female', TO_DATE('2006-05-30','YYYY-MM-DD'), '7 Rajaji Street, Madurai, TN')",
            "INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) VALUES ('24AI008', 'Rohan Verma', 'rohan.verma@example.com', '9876543214', 'B.Tech Artificial Intelligence and Data Science', 2, 'A', 'Male', TO_DATE('2006-09-10','YYYY-MM-DD'), '23 Nelson Manickam Road, Chennai, TN')",
            "INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) VALUES ('24EC021', 'Sneha Patel', 'sneha.patel@example.com', '9876543215', 'B.E. Electronics and Communication Engineering', 3, 'B', 'Female', TO_DATE('2005-11-04','YYYY-MM-DD'), '19 MG Road, Tiruchirappalli, TN')"
        };

        try (Statement st = conn.createStatement()) {
            for (String sql : seeds) {
                st.executeUpdate(sql);
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }
}
