# Laboratory Project Report

## Course: UIT3361 – Object-Oriented Programming using Java
**Department of Information Technology**  
**Sri Sivasubramaniya Nadar College of Engineering, Kalavakkam**  
**Degree & Branch**: B.Tech Information Technology  
**Semester**: III | **Regulation**: 2024 | **Academic Year**: 2026–2027  

**Project Title**: Student Information Management System (SIMS)  
**Assigned S.No**: 1 (Register Numbers: 1 to 4)  

---

### 1. System Requirements

#### 1.1 Hardware Specifications
* **Processor**: Intel Core i3 / AMD Ryzen 3 (2.0 GHz) or higher
* **Memory (RAM)**: 4 GB minimum (8 GB recommended for running Oracle DB container)
* **Storage**: 1 GB available disk space
* **Display**: 1280 × 720 minimum screen resolution

#### 1.2 Software Specifications
* **Operating System**: Windows 10/11, macOS, or Linux
* **Java Development Kit (JDK)**: OpenJDK / Oracle JDK 17 (LTS)
* **GUI Framework**: Java Swing (Standard Desktop Framework)
* **Database**: Oracle Database 23c Free / XE (Dockerized or Native)
* **Database Driver**: Oracle JDBC Thin Driver (`ojdbc11` version 23.4.0.24.05)
* **Build Automation Tool**: Apache Maven 3.9+ / Maven Wrapper

---

### 2. System Design & Architecture

The application adopts the **Model-View-Controller (MVC)** architectural pattern to ensure clean separation of concerns:

```
+-------------------------------------------------------------------+
|                            VIEW                                   |
|                  (StudentManagementFrame)                         |
|  - Data Entry Form (GridBagLayout)                                |
|  - JTable Records View & Live Search Filter                       |
|  - Event Observers (ActionListener, DocumentListener)             |
+---------------------------------+---------------------------------+
                                  | User Events / Actions
                                  v
+-------------------------------------------------------------------+
|                         CONTROLLER                                |
|                    (StudentController)                            |
|  - Input Validation (Regex: Roll, Email, Phone, Age)              |
|  - Transaction Management (conn.commit / conn.rollback)           |
|  - Business Logic & CSV Export Dispatcher                         |
+---------------------------------+---------------------------------+
                                  | Invokes CRUD Operations
                                  v
+-------------------------------------------------------------------+
|                         DATA ACCESS (DAO)                         |
|                         (StudentDAO)                              |
|  - PreparedStatement SQL Execution (INSERT, UPDATE, DELETE, ...)   |
|  - ResultSet to Model Mapping                                     |
+---------------------------------+---------------------------------+
                                  | JDBC Thin Driver
                                  v
+-------------------------------------------------------------------+
|                        RELATIONAL DATABASE                        |
|                     (Oracle Database - STUDENTS)                  |
+-------------------------------------------------------------------+
```

#### Design Patterns Applied:
1. **Creational — Singleton Pattern** (`DBConnection`):
   Ensures only a single instance of the database connection manager is instantiated and reused across the entire application lifecycle.
2. **Structural — Data Access Object (DAO) Pattern** (`StudentDAO`):
   Abstracts and encapsulates all database interaction logic, decoupling the domain model from underlying SQL dialect and persistence details.
3. **Behavioral — Observer Pattern** (`ActionListener`, `DocumentListener`, `ListSelectionListener`):
   Swing event listeners observe UI actions (clicks, keypresses, selections) and trigger controller routines without hard-coupling view widgets to backend logic.

---

### 3. Core Modules Implemented

1. **Student Registration Module (Enter & Store)**:
   * Captures student details: Roll Number, Full Name, Email, Phone, Department, Year, Section, Gender, Date of Birth, Address.
   * Performs real-time validation prior to persisting to Oracle Database.
2. **Student Directory & View Module (View)**:
   * Displays all active records in an interactive, zebra-striped `JTable`.
   * Formatted column sizing and instant row selection.
3. **Live Search and Filtering Module (Search)**:
   * Real-time search by Roll Number, Full Name, Department, Email, or Phone using Swing `DocumentListener`.
   * Secondary filtering by academic department via dropdown selector.
4. **Student Record Modification Module (Update)**:
   * Clicking any table row pre-populates all form fields.
   * Modifying details and clicking "Update" commits changes via parameterised SQL.
5. **Student Removal Module (Delete)**:
   * Allows deletion of obsolete student profiles with a defensive confirmation dialog to prevent accidental data loss.
6. **Data Export Module**:
   * Exports student directory records to a formatted standard CSV spreadsheet.

---

### 4. JDBC Connectivity & Database Operations

The application establishes communication with Oracle Database using the official Oracle JDBC Thin Driver (`ojdbc11`).

#### Table Schema (`STUDENTS`)
```sql
CREATE TABLE STUDENTS (
    STUDENT_ID    NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY PRIMARY KEY,
    ROLL_NUMBER   VARCHAR2(20) NOT NULL UNIQUE,
    FULL_NAME     VARCHAR2(100) NOT NULL,
    EMAIL         VARCHAR2(100) NOT NULL UNIQUE,
    PHONE         VARCHAR2(15) NOT NULL,
    DEPARTMENT    VARCHAR2(80) NOT NULL,
    YEAR          NUMBER(1) NOT NULL CHECK (YEAR BETWEEN 1 AND 5),
    SECTION       VARCHAR2(5),
    GENDER        VARCHAR2(10) CHECK (GENDER IN ('Male', 'Female', 'Other')),
    DATE_OF_BIRTH DATE,
    ADDRESS       VARCHAR2(250),
    CREATED_AT    TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);
```

#### Core Database Operations:
* **Insert (Create)**:
  `INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
* **Select All (Read)**:
  `SELECT * FROM STUDENTS ORDER BY ROLL_NUMBER ASC`
* **Search / Filter (Read with Predicate)**:
  `SELECT * FROM STUDENTS WHERE LOWER(ROLL_NUMBER) LIKE ? OR LOWER(FULL_NAME) LIKE ? OR LOWER(DEPARTMENT) LIKE ? ...`
* **Update (Modify)**:
  `UPDATE STUDENTS SET FULL_NAME = ?, EMAIL = ?, PHONE = ?, DEPARTMENT = ?, YEAR = ?, SECTION = ?, GENDER = ?, DATE_OF_BIRTH = ?, ADDRESS = ? WHERE STUDENT_ID = ?`
* **Delete (Remove)**:
  `DELETE FROM STUDENTS WHERE STUDENT_ID = ?`

---

### 5. Important Code Snippets

#### 5.1 Singleton Pattern (`DBConnection.java`)
```java
public final class DBConnection {
    private static volatile DBConnection instance;
    private Connection connection;

    private DBConnection() throws SQLException {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String url = dotenv.get("DB_URL", "jdbc:oracle:thin:@localhost:1522/FREEPDB1");
        String user = dotenv.get("DB_USER", "system");
        String pass = dotenv.get("DB_PASSWORD", "admin");
        this.connection = DriverManager.getConnection(url, user, pass);
        this.connection.setAutoCommit(false); // Manual transaction demarcation
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    try { instance = new DBConnection(); }
                    catch (SQLException e) { throw new RuntimeException(e); }
                }
            }
        }
        return instance;
    }
}
```

#### 5.2 Input Validation (`StudentController.java`)
```java
public void validateStudent(Student s, boolean isNew) throws ValidationException {
    if (!ROLL_REGEX.matcher(s.getRollNumber().trim()).matches()) {
        throw new ValidationException("Roll Number", "Invalid Roll Number format (e.g. 24IT001).");
    }
    if (!EMAIL_REGEX.matcher(s.getEmail().trim()).matches()) {
        throw new ValidationException("Email", "Invalid email format (e.g. student@college.edu).");
    }
    if (!PHONE_REGEX.matcher(s.getPhone().trim()).matches()) {
        throw new ValidationException("Phone", "Phone must be a valid 10-digit mobile number.");
    }
    if (s.getDateOfBirth() != null) {
        int age = Period.between(s.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 15 || age > 70) {
            throw new ValidationException("Date of Birth", "Student age must be between 15 and 70.");
        }
    }
}
```

#### 5.3 Parameterised JDBC Delete with Transaction (`StudentDAO.java` & `StudentController.java`)
```java
// StudentDAO.java
public int delete(long studentId) throws SQLException {
    String sql = "DELETE FROM STUDENTS WHERE STUDENT_ID = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, studentId);
        return ps.executeUpdate();
    }
}

// StudentController.java
public void deleteStudent(long studentId) {
    try {
        int affected = studentDAO.delete(studentId);
        if (affected == 0) throw new RuntimeException("Record not found.");
        conn.commit(); // Atomic commit
    } catch (SQLException e) {
        conn.rollback();
        throw new RuntimeException("Delete failed: " + e.getMessage(), e);
    }
}
```

---

### 6. Screenshots of Application Execution

*(Attach screenshots during report submission)*
1. **Main Window Overview**: Showing form on left, table populated with records on right, connection badge in header.
2. **Add Student with Input Validation**: Dialog highlighting invalid phone number or email format.
3. **Record Selection & Update**: Form populated after clicking table row, updating department, and seeing changes reflected.
4. **Delete Confirmation Dialog**: Defensive warning prompt before deleting a student record.
5. **Live Search Demonstration**: Filtering table instantly as search keyword is typed.

---

### 7. Test Cases and Results

| Test Case ID | Feature / Module | Input Data / Action | Expected Result | Actual Result | Status |
|:---:|:---|:---|:---|:---|:---:|
| **TC01** | Input Validation | Empty Roll Number | Displays "Roll Number is required", focuses field | Validation dialog shown | PASS |
| **TC02** | Input Validation | Phone = `"12345"` (5 digits) | Rejects with "Invalid phone number (must be 10 digits)" | Dialog shown, focus set | PASS |
| **TC03** | Input Validation | Email = `"invalid-email"` | Rejects with "Invalid email address format" | Dialog shown, focus set | PASS |
| **TC04** | Record Insertion | Valid student details (`24IT101`) | Record inserted, committed to DB, table updated | Added and reflected in table | PASS |
| **TC05** | Duplicate Check | Insert existing Roll `24IT101` | Rejects with duplicate roll number error | Duplicate detected | PASS |
| **TC06** | Record Update | Select row, change department to IT, click Update | DB updated, table immediately reflects change | Record updated | PASS |
| **TC07** | Record Delete | Click Delete on selected row, confirm YES | Record removed from DB and table | Deleted successfully | PASS |
| **TC08** | Live Search | Type `"Sharma"` in search box | Table dynamically filters to matching records | Live filtered | PASS |
| **TC09** | Export CSV | Click "Export CSV" and select file | Creates valid `.csv` with all student fields | CSV written properly | PASS |

---

### 8. GitHub Repository Link

* **Project Repository**: `https://github.com/aadiii3769/student-information-system`  
*(Or your newly created repository URL)*
