# Student Information Management System (SIMS)

> **Course**: UIT3361 – Object-Oriented Programming using Java  
> **Degree & Branch**: B.Tech Information Technology (Semester III)  
> **Regulation**: 2024 | Academic Year: 2026–2027  
> **Institution**: Sri Sivasubramaniya Nadar College of Engineering (SSN)

---

## 📌 Problem Statement
> **Problem Statement 1 (Register Numbers 1 to 4)**:  
> Develop a Student Information Management System using Java and an appropriate GUI framework. The application should allow the user to enter, store, search, view, update, and delete student information through a graphical interface. The application should provide suitable GUI components for data entry and display, implement appropriate event handling and input validation, and use JDBC to connect the application to a relational database for storing and retrieving student records.

---

## 🚀 Key Features

* **Complete Student CRUD Operations**:
  * **Enter & Store (Create)**: Form entry with input validation saving records into Oracle Database.
  * **View (Read)**: Interactive `JTable` with alternating striping displaying all student records.
  * **Search & Filter**: Live real-time search across Roll Number, Full Name, Department, Email, and Phone, plus Department dropdown filtering.
  * **Update**: Select any record in the table to populate form, modify fields, and save changes to DB.
  * **Delete**: Delete records with confirmation prompt preventing accidental loss.
* **Robust Input Validation**:
  * Regex validation for Roll Number (`^[A-Za-z0-9\-/]{3,20}$`)
  * Full Name check (letters, spaces, periods only)
  * Email format verification (`^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w]{2,})+$`)
  * 10-digit mobile phone validation (`^(?:\\+91)?[6-9]\\d{9}$`)
  * Date of Birth validation and age sanity check (15 to 70 years)
  * Mandatory field enforcement and automatic focus on failing input fields.
* **Event Handling & GUI Programming**:
  * Built using pure **Java Swing** with clean `GridBagLayout`, `BorderLayout`, and `FlowLayout` (no null/absolute layouts).
  * `ActionListener` for buttons (`Add`, `Update`, `Delete`, `Clear`, `Refresh`, `Export`).
  * `ListSelectionListener` for instant record selection from the table to the form.
  * `DocumentListener` for responsive live search filtering as user types.
* **JDBC & Database Integrity**:
  * Direct Oracle JDBC Thin Driver (`ojdbc11`) connectivity.
  * Exclusively parameterised `PreparedStatement` preventing SQL injection.
  * Explicit transaction control (`commit` / `rollback`) with `autoCommit = false`.
  * Automatic table creation and demo seed data on first launch.
* **Exporting**:
  * Export student directory to standard CSV file via `JFileChooser`.

---

## 🏗️ Architecture & Design Patterns

The application follows the **Model-View-Controller (MVC)** architectural pattern:

```
com.sims
├── Main.java                        (Application entry point, sets System L&F)
├── model
│   └── Student.java                 (POJO domain model representing student record)
├── dao
│   └── StudentDAO.java              (Data Access Object: encapsulates all SQL queries)
├── controller
│   └── StudentController.java       (Business logic, validation, and transaction control)
├── view
│   └── StudentManagementFrame.java  (Swing GUI frame, form, table, and event listeners)
└── util
    ├── DBConnection.java            (Singleton pattern for shared Oracle JDBC connection)
    └── ValidationException.java     (Custom checked exception for validation errors)
```

### Academic Design Patterns Implemented:
1. **Creational — Singleton Pattern** (`DBConnection`):
   Guarantees a single shared connection throughout the JVM lifecycle, eliminating expensive connection renegotiation overhead.
2. **Structural — Data Access Object Pattern** (`StudentDAO`):
   Encapsulates all low-level JDBC queries and `ResultSet` mappings, isolating persistence changes from business logic.
3. **Behavioral — Observer Pattern** (`ActionListener`, `ListSelectionListener`, `DocumentListener`):
   Swing components emit events to decoupled observer handlers that delegate actions to the Controller.
4. **Architectural — Model-View-Controller (MVC)**:
   Clear separation of concerns between entity models, presentation views, and business controllers.

---

## 🛠️ Prerequisites & Setup

* **Java JDK**: 17 or higher
* **Database**: Oracle Database (Docker Free container or local Oracle XE/19c/21c/23c)
* **Maven**: Maven 3.8+ (or use included `mvnw` wrapper)

### Database Configuration (`.env`)
Create or edit `.env` in the project root:
```env
DB_URL=jdbc:oracle:thin:@localhost:1522/FREEPDB1
DB_USER=system
DB_PASSWORD=admin
```

---

## ⚡ How to Run

Compile and run using Maven Wrapper:

```bash
# Windows
.\mvnw.cmd compile exec:java

# Linux / macOS
./mvnw compile exec:java
```

Or using standard Maven:
```bash
mvn compile exec:java
```
