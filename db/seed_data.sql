-- =============================================================================
-- Student Information Management System – Sample Seed Data
-- File   : db/seed_data.sql
-- =============================================================================

INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS)
VALUES ('24IT001', 'Aarav Sharma', 'aarav.sharma@example.com', '9876543210', 'B.Tech Information Technology', 3, 'A', 'Male', TO_DATE('2005-04-12', 'YYYY-MM-DD'), '12 Anna Salai, Chennai, TN');

INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS)
VALUES ('24IT002', 'Diya Ramesh', 'diya.ramesh@example.com', '9876543211', 'B.Tech Information Technology', 3, 'A', 'Female', TO_DATE('2005-08-23', 'YYYY-MM-DD'), '45 OMR Road, Chennai, TN');

INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS)
VALUES ('24CS015', 'Karthik Subramanian', 'karthik.s@example.com', '9876543212', 'B.E. Computer Science and Engineering', 2, 'B', 'Male', TO_DATE('2006-01-15', 'YYYY-MM-DD'), '88 Gandhi Nagar, Coimbatore, TN');

INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS)
VALUES ('24CS042', 'Meera Krishnan', 'meera.k@example.com', '9876543213', 'B.E. Computer Science and Engineering', 2, 'A', 'Female', TO_DATE('2006-05-30', 'YYYY-MM-DD'), '7 Rajaji Street, Madurai, TN');

INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS)
VALUES ('24AI008', 'Rohan Verma', 'rohan.verma@example.com', '9876543214', 'B.Tech Artificial Intelligence and Data Science', 2, 'A', 'Male', TO_DATE('2006-09-10', 'YYYY-MM-DD'), '23 Nelson Manickam Road, Chennai, TN');

INSERT INTO STUDENTS (ROLL_NUMBER, FULL_NAME, EMAIL, PHONE, DEPARTMENT, YEAR, SECTION, GENDER, DATE_OF_BIRTH, ADDRESS)
VALUES ('24EC021', 'Sneha Patel', 'sneha.patel@example.com', '9876543215', 'B.E. Electronics and Communication Engineering', 3, 'B', 'Female', TO_DATE('2005-11-04', 'YYYY-MM-DD'), '19 MG Road, Tiruchirappalli, TN');

COMMIT;
