package com.sims.view;

import com.sims.controller.StudentController;
import com.sims.model.Student;
import com.sims.util.DBConnection;
import com.sims.util.ValidationException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main graphical interface for the Student Information Management System.
 *
 * <h2>Design Pattern – Behavioral: Observer</h2>
 * <p><b>Academic Justification</b>: Every button click (Add, Update, Delete, Clear,
 * Export), table selection change, and search box keystroke fires events handled by
 * {@link java.awt.event.ActionListener}, {@link javax.swing.event.ListSelectionListener},
 * and {@link javax.swing.event.DocumentListener} (Observer Pattern). The View remains
 * completely decoupled from persistence details, delegating operations to
 * {@link StudentController}.</p>
 *
 * <h2>Threading & Responsiveness</h2>
 * <p>All database queries and persistence mutations are executed off the Event
 * Dispatch Thread (EDT) using {@link SwingWorker}, keeping the UI completely responsive
 * during network or database operations.</p>
 */
public class StudentManagementFrame extends JFrame {

    private final StudentController controller;

    // ── Form Components ──────────────────────────────────────────────────────
    private JLabel lblIdValue;
    private JTextField txtRollNo;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JComboBox<String> cmbDept;
    private JComboBox<Integer> cmbYear;
    private JComboBox<String> cmbSection;
    private JRadioButton rbMale;
    private JRadioButton rbFemale;
    private JRadioButton rbOther;
    private ButtonGroup bgGender;
    private JTextField txtDob;
    private JTextArea txtAddress;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // ── Table & Search Components ────────────────────────────────────────────
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterDept;
    private JButton btnRefresh;
    private JButton btnExport;
    private JTable tableStudents;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;
    private JLabel lblCount;

    // ── Internal State ───────────────────────────────────────────────────────
    private List<Student> loadedStudents = new ArrayList<>();
    private Long selectedStudentId = null;

    private static final String[] DEPARTMENTS = {
        "B.Tech Information Technology",
        "B.E. Computer Science and Engineering",
        "B.Tech Artificial Intelligence and Data Science",
        "B.Tech Computer Science and Business Systems",
        "B.E. Electronics and Communication Engineering",
        "B.E. Electrical and Electronics Engineering",
        "B.E. Mechanical Engineering",
        "B.E. Civil Engineering",
        "M.E. Computer Science and Engineering",
        "MBA", "MCA"
    };

    private static final String[] TABLE_COLUMNS = {
        "ID", "Roll No", "Full Name", "Department", "Year", "Sec", "Gender", "Phone", "Email", "DOB"
    };

    public StudentManagementFrame() {
        super("Student Information Management System — UIT3361");
        this.controller = new StudentController();

        initUI();
        loadAllStudentsAsync();
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 720));
        setPreferredSize(new Dimension(1200, 760));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(0, 8));
        contentPane.setBorder(new EmptyBorder(10, 14, 10, 14));
        contentPane.setBackground(new Color(245, 247, 250));

        // 1. Header
        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Split (Form on Left, Table on Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createFormPanel(), createTablePanel());
        splitPane.setDividerLocation(430);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        contentPane.add(splitPane, BorderLayout.CENTER);

        // 3. Footer Status Bar
        contentPane.add(createFooterPanel(), BorderLayout.SOUTH);

        setContentPane(contentPane);
        pack();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 220, 235), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        header.setBackground(Color.WHITE);

        // Left: Branding
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("🎓 Student Information Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(new Color(26, 43, 76));

        JLabel lblSub = new JLabel("UIT3361 – Object-Oriented Programming using Java | Standalone Desktop CRUD");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 115, 130));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);
        header.add(titleBox, BorderLayout.WEST);

        // Right: DB Connection Pill
        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        statusBox.setOpaque(false);

        String connectedUrl = DBConnection.getInstance().getConnectedUrl();
        JLabel lblDbBadge = new JLabel("🟢 Oracle JDBC Connected");
        lblDbBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDbBadge.setForeground(new Color(22, 101, 52));
        lblDbBadge.setToolTipText(connectedUrl != null ? connectedUrl : "Oracle Database");
        lblDbBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        lblDbBadge.setBackground(new Color(240, 253, 244));
        lblDbBadge.setOpaque(true);

        statusBox.add(lblDbBadge);
        header.add(statusBox, BorderLayout.EAST);

        return header;
    }

    private JPanel createFormPanel() {
        JPanel formWrapper = new JPanel(new BorderLayout(0, 10));
        formWrapper.setOpaque(false);
        formWrapper.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        " Student Details Entry / Edit ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(30, 58, 138)
                ),
                new EmptyBorder(10, 12, 10, 12)
        ));
        formWrapper.setBackground(Color.WHITE);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: ID (System generated)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        grid.add(new JLabel("Record ID:"), gbc);
        lblIdValue = new JLabel("(New Record)");
        lblIdValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIdValue.setForeground(new Color(100, 116, 139));
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(lblIdValue, gbc);

        // Row 1: Roll Number
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        grid.add(createFormLabel("Roll Number *"), gbc);
        txtRollNo = new JTextField(15);
        txtRollNo.setToolTipText("e.g. 24IT001 or 101");
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(txtRollNo, gbc);

        // Row 2: Full Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        grid.add(createFormLabel("Full Name *"), gbc);
        txtFullName = new JTextField(15);
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(txtFullName, gbc);

        // Row 3: Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        grid.add(createFormLabel("Email *"), gbc);
        txtEmail = new JTextField(15);
        txtEmail.setToolTipText("e.g. student@example.com");
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(txtEmail, gbc);

        // Row 4: Phone
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        grid.add(createFormLabel("Phone (10 digits) *"), gbc);
        txtPhone = new JTextField(15);
        txtPhone.setToolTipText("10-digit mobile number");
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(txtPhone, gbc);

        // Row 5: Department
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        grid.add(createFormLabel("Department *"), gbc);
        cmbDept = new JComboBox<>(DEPARTMENTS);
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(cmbDept, gbc);

        // Row 6: Year & Section
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        grid.add(createFormLabel("Year & Section *"), gbc);
        JPanel yearSecPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        yearSecPanel.setOpaque(false);
        cmbYear = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        cmbYear.setSelectedItem(2);
        cmbSection = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        yearSecPanel.add(new JLabel("Yr:"));
        yearSecPanel.add(cmbYear);
        yearSecPanel.add(new JLabel("Sec:"));
        yearSecPanel.add(cmbSection);
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(yearSecPanel, gbc);

        // Row 7: Gender
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        grid.add(createFormLabel("Gender"), gbc);
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        genderPanel.setOpaque(false);
        rbMale = new JRadioButton("Male", true);
        rbFemale = new JRadioButton("Female");
        rbOther = new JRadioButton("Other");
        rbMale.setOpaque(false);
        rbFemale.setOpaque(false);
        rbOther.setOpaque(false);
        bgGender = new ButtonGroup();
        bgGender.add(rbMale);
        bgGender.add(rbFemale);
        bgGender.add(rbOther);
        genderPanel.add(rbMale);
        genderPanel.add(rbFemale);
        genderPanel.add(rbOther);
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(genderPanel, gbc);

        // Row 8: DOB
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0;
        grid.add(createFormLabel("DOB (YYYY-MM-DD)"), gbc);
        txtDob = new JTextField(15);
        txtDob.setToolTipText("e.g. 2005-04-12");
        gbc.gridx = 1; gbc.weightx = 1.0;
        grid.add(txtDob, gbc);

        // Row 9: Address
        gbc.gridx = 0; gbc.gridy = 9; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        grid.add(createFormLabel("Address"), gbc);
        txtAddress = new JTextArea(3, 15);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane scrollAddr = new JScrollPane(txtAddress);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        grid.add(scrollAddr, gbc);

        formWrapper.add(new JScrollPane(grid), BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnAdd = createStyledButton("➕ Add Student", new Color(22, 101, 52), new Color(34, 197, 94));
        btnUpdate = createStyledButton("✏️ Update", new Color(30, 64, 175), new Color(59, 130, 246));
        btnDelete = createStyledButton("🗑️ Delete", new Color(153, 27, 27), new Color(239, 68, 68));
        btnClear = createStyledButton("🧹 Clear Form", new Color(71, 85, 105), new Color(148, 163, 184));

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        formWrapper.add(btnPanel, BorderLayout.SOUTH);

        // ── Observers (ActionListeners) ─────────────────────────────────────
        btnAdd.addActionListener(this::onAddStudent);
        btnUpdate.addActionListener(this::onUpdateStudent);
        btnDelete.addActionListener(this::onDeleteStudent);
        btnClear.addActionListener(e -> clearForm());

        return formWrapper;
    }

    private JPanel createTablePanel() {
        JPanel tableWrapper = new JPanel(new BorderLayout(0, 8));
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        " Registered Students Database ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(30, 58, 138)
                ),
                new EmptyBorder(8, 10, 8, 10)
        ));
        tableWrapper.setBackground(Color.WHITE);

        // Search and Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        toolbar.add(new JLabel("🔍 Search:"));
        txtSearch = new JTextField(14);
        txtSearch.setToolTipText("Filter live by roll number, name, department, phone, or email");
        toolbar.add(txtSearch);

        toolbar.add(new JLabel("Dept Filter:"));
        List<String> filterDepts = new ArrayList<>();
        filterDepts.add("All Departments");
        for (String d : DEPARTMENTS) filterDepts.add(d);
        cmbFilterDept = new JComboBox<>(filterDepts.toArray(new String[0]));
        toolbar.add(cmbFilterDept);

        btnRefresh = new JButton("↻ Refresh");
        btnExport = new JButton("📥 Export CSV");
        toolbar.add(btnRefresh);
        toolbar.add(btnExport);

        tableWrapper.add(toolbar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only directly in table cells
            }
        };

        tableStudents = new JTable(tableModel);
        tableStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableStudents.setRowHeight(25);
        tableStudents.setShowGrid(true);
        tableStudents.setGridColor(new Color(226, 232, 240));

        JTableHeader th = tableStudents.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(241, 245, 249));
        th.setForeground(new Color(30, 41, 59));

        // Format column widths
        tableStudents.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        tableStudents.getColumnModel().getColumn(1).setPreferredWidth(75);   // Roll
        tableStudents.getColumnModel().getColumn(2).setPreferredWidth(140);  // Name
        tableStudents.getColumnModel().getColumn(3).setPreferredWidth(160);  // Dept
        tableStudents.getColumnModel().getColumn(4).setPreferredWidth(45);   // Year
        tableStudents.getColumnModel().getColumn(5).setPreferredWidth(40);   // Sec
        tableStudents.getColumnModel().getColumn(6).setPreferredWidth(55);   // Gender
        tableStudents.getColumnModel().getColumn(7).setPreferredWidth(95);   // Phone
        tableStudents.getColumnModel().getColumn(8).setPreferredWidth(140);  // Email
        tableStudents.getColumnModel().getColumn(9).setPreferredWidth(85);   // DOB

        // Custom zebra striping renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        };
        for (int i = 0; i < tableStudents.getColumnCount(); i++) {
            tableStudents.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Selection listener: populate form on table row click
        tableStudents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableStudents.getSelectedRow();
                if (row >= 0 && row < loadedStudents.size()) {
                    populateForm(loadedStudents.get(row));
                }
            }
        });

        tableWrapper.add(new JScrollPane(tableStudents), BorderLayout.CENTER);

        // ── Observers for Search & Filtering ────────────────────────────────
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyLiveFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyLiveFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyLiveFilter(); }
        });

        cmbFilterDept.addActionListener(e -> applyLiveFilter());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterDept.setSelectedIndex(0);
            loadAllStudentsAsync();
        });
        btnExport.addActionListener(this::onExportCsv);

        return tableWrapper;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 4, 4, 4));

        lblStatus = new JLabel("System Ready.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(71, 85, 105));

        lblCount = new JLabel("0 students");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCount.setForeground(new Color(30, 58, 138));

        footer.add(lblStatus, BorderLayout.WEST);
        footer.add(lblCount, BorderLayout.EAST);
        return footer;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(51, 65, 85));
        return label;
    }

    private JButton createStyledButton(String text, Color normalColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? normalColor.darker() :
                             getModel().isRollover() ? hoverColor : normalColor;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 12, 7, 12));
        return btn;
    }

    // ── Data Operations (SwingWorker for Asynchronous Off-EDT Execution) ────

    private void loadAllStudentsAsync() {
        lblStatus.setText("Fetching student records from database…");

        new SwingWorker<List<Student>, Void>() {
            @Override
            protected List<Student> doInBackground() {
                return controller.getAllStudents();
            }

            @Override
            protected void done() {
                try {
                    loadedStudents = get();
                    displayStudentsInTable(loadedStudents);
                    lblStatus.setText("✓ Successfully loaded " + loadedStudents.size() + " records from Oracle DB.");
                    lblStatus.setForeground(new Color(22, 101, 52));
                } catch (Exception ex) {
                    lblStatus.setText("❌ Failed to load students: " + ex.getMessage());
                    lblStatus.setForeground(new Color(185, 28, 28));
                    JOptionPane.showMessageDialog(StudentManagementFrame.this,
                            "Error connecting to database:\n" + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void displayStudentsInTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                s.getStudentId(),
                s.getRollNumber(),
                s.getFullName(),
                s.getDepartment(),
                s.getYear(),
                s.getSection() != null ? s.getSection() : "-",
                s.getGender() != null ? s.getGender() : "-",
                s.getPhone(),
                s.getEmail(),
                s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "-"
            });
        }
        lblCount.setText(students.size() + " record(s)");
    }

    private void applyLiveFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedDept = (String) cmbFilterDept.getSelectedItem();
        boolean filterDept = selectedDept != null && !selectedDept.equals("All Departments");

        List<Student> filtered = new ArrayList<>();
        for (Student s : loadedStudents) {
            boolean matchesSearch = keyword.isEmpty() ||
                    (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(keyword)) ||
                    (s.getFullName() != null && s.getFullName().toLowerCase().contains(keyword)) ||
                    (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(keyword)) ||
                    (s.getEmail() != null && s.getEmail().toLowerCase().contains(keyword)) ||
                    (s.getPhone() != null && s.getPhone().contains(keyword));

            boolean matchesDept = !filterDept || (s.getDepartment() != null && s.getDepartment().equalsIgnoreCase(selectedDept));

            if (matchesSearch && matchesDept) {
                filtered.add(s);
            }
        }
        displayStudentsInTable(filtered);
    }

    // ── Form Binding & Actions ───────────────────────────────────────────────

    private void populateForm(Student s) {
        selectedStudentId = s.getStudentId();
        lblIdValue.setText(String.valueOf(s.getStudentId()));
        txtRollNo.setText(s.getRollNumber());
        txtFullName.setText(s.getFullName());
        txtEmail.setText(s.getEmail());
        txtPhone.setText(s.getPhone());
        cmbDept.setSelectedItem(s.getDepartment());
        cmbYear.setSelectedItem(s.getYear());
        if (s.getSection() != null) cmbSection.setSelectedItem(s.getSection());

        if ("Female".equalsIgnoreCase(s.getGender())) rbFemale.setSelected(true);
        else if ("Other".equalsIgnoreCase(s.getGender())) rbOther.setSelected(true);
        else rbMale.setSelected(true);

        txtDob.setText(s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "");
        txtAddress.setText(s.getAddress() != null ? s.getAddress() : "");

        lblStatus.setText("Selected student: " + s.getFullName() + " (" + s.getRollNumber() + ")");
    }

    private void clearForm() {
        selectedStudentId = null;
        lblIdValue.setText("(New Record)");
        txtRollNo.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        cmbDept.setSelectedIndex(0);
        cmbYear.setSelectedItem(1);
        cmbSection.setSelectedIndex(0);
        rbMale.setSelected(true);
        txtDob.setText("");
        txtAddress.setText("");
        tableStudents.clearSelection();
        lblStatus.setText("Form cleared.");
        txtRollNo.requestFocus();
    }

    private Student readStudentFromForm() throws ValidationException {
        Student s = new Student();
        s.setStudentId(selectedStudentId);
        s.setRollNumber(txtRollNo.getText().trim());
        s.setFullName(txtFullName.getText().trim());
        s.setEmail(txtEmail.getText().trim());
        s.setPhone(txtPhone.getText().trim());
        s.setDepartment((String) cmbDept.getSelectedItem());
        s.setYear((Integer) cmbYear.getSelectedItem());
        s.setSection((String) cmbSection.getSelectedItem());

        if (rbFemale.isSelected()) s.setGender("Female");
        else if (rbOther.isSelected()) s.setGender("Other");
        else s.setGender("Male");

        String dobStr = txtDob.getText().trim();
        if (!dobStr.isEmpty()) {
            try {
                s.setDateOfBirth(LocalDate.parse(dobStr));
            } catch (DateTimeParseException ex) {
                throw new ValidationException("Date of Birth", "Date format must be YYYY-MM-DD (e.g. 2005-04-12).");
            }
        }

        s.setAddress(txtAddress.getText().trim());
        return s;
    }

    private void onAddStudent(ActionEvent e) {
        try {
            Student student = readStudentFromForm();
            student.setStudentId(null); // Force new record

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    controller.addStudent(student);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        lblStatus.setText("✓ Added student " + student.getFullName() + " (" + student.getRollNumber() + ")");
                        lblStatus.setForeground(new Color(22, 101, 52));
                        JOptionPane.showMessageDialog(StudentManagementFrame.this,
                                "Student record created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadAllStudentsAsync();
                    } catch (Exception ex) {
                        Throwable root = ex.getCause() != null ? ex.getCause() : ex;
                        handleError(root);
                    }
                }
            }.execute();
        } catch (ValidationException ve) {
            handleValidationException(ve);
        }
    }

    private void onUpdateStudent(ActionEvent e) {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an existing student record from the table to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student student = readStudentFromForm();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    controller.updateStudent(student);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        lblStatus.setText("✓ Updated student " + student.getFullName() + " (" + student.getRollNumber() + ")");
                        lblStatus.setForeground(new Color(22, 101, 52));
                        JOptionPane.showMessageDialog(StudentManagementFrame.this,
                                "Student record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadAllStudentsAsync();
                    } catch (Exception ex) {
                        Throwable root = ex.getCause() != null ? ex.getCause() : ex;
                        handleError(root);
                    }
                }
            }.execute();
        } catch (ValidationException ve) {
            handleValidationException(ve);
        }
    }

    private void onDeleteStudent(ActionEvent e) {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rollNo = txtRollNo.getText().trim();
        String name = txtFullName.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete student:\n" +
                name + " (" + rollNo + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        final long idToDelete = selectedStudentId;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                controller.deleteStudent(idToDelete);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblStatus.setText("✓ Student " + name + " deleted successfully.");
                    lblStatus.setForeground(new Color(22, 101, 52));
                    JOptionPane.showMessageDialog(StudentManagementFrame.this,
                            "Student record deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadAllStudentsAsync();
                } catch (Exception ex) {
                    Throwable root = ex.getCause() != null ? ex.getCause() : ex;
                    handleError(root);
                }
            }
        }.execute();
    }

    private void onExportCsv(ActionEvent e) {
        if (loadedStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records available to export.", "Empty Table", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("student_records.csv"));
        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                controller.exportToCsv(loadedStudents, file);
                JOptionPane.showMessageDialog(this,
                        "Exported " + loadedStudents.size() + " student records to:\n" + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Exception Handling Helpers ───────────────────────────────────────────

    private void handleValidationException(ValidationException ve) {
        lblStatus.setText("❌ Validation Error: " + ve.getMessage());
        lblStatus.setForeground(new Color(185, 28, 28));

        JOptionPane.showMessageDialog(this,
                ve.getMessage(),
                "Input Validation Error", JOptionPane.WARNING_MESSAGE);

        // Auto-focus invalid field
        if ("Roll Number".equals(ve.getFieldName())) txtRollNo.requestFocus();
        else if ("Full Name".equals(ve.getFieldName())) txtFullName.requestFocus();
        else if ("Email".equals(ve.getFieldName())) txtEmail.requestFocus();
        else if ("Phone".equals(ve.getFieldName())) txtPhone.requestFocus();
        else if ("Date of Birth".equals(ve.getFieldName())) txtDob.requestFocus();
    }

    private void handleError(Throwable root) {
        lblStatus.setText("❌ Error: " + root.getMessage());
        lblStatus.setForeground(new Color(185, 28, 28));

        if (root instanceof ValidationException) {
            handleValidationException((ValidationException) root);
        } else {
            JOptionPane.showMessageDialog(this,
                    root.getMessage(),
                    "Operation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
