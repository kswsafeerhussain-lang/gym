package gym;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * TrainersPanel - Full CRUD + Search for Trainers
 */
public class TrainersPanel extends JPanel {

    private String role;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JTextField fName, fEmail, fPhone, fSpecialization, fSalary, fHireDate;
    private JComboBox<String> fStatus;
    private int selectedId = -1;

    public TrainersPanel(String role) {
        this.role = role;
        initComponents();
        loadData("");
    }

    private void initComponents() {
        setBackground(new Color(18, 20, 32));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("🏋 Trainers Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(33, 150, 243));
        add(lblTitle, BorderLayout.NORTH);

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(20); styleField(txtSearch);
        JButton btnSearch = createButton("🔍 Search", new Color(33, 150, 243));
        JButton btnClear = createButton("✖ Clear", new Color(100, 100, 130));
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnClear.addActionListener(e -> { txtSearch.setText(""); loadData(""); });
        searchPanel.add(createLabel("Search:")); searchPanel.add(txtSearch);
        searchPanel.add(btnSearch); searchPanel.add(btnClear);

        // Table
        String[] cols = {"ID", "Name", "Email", "Phone", "Specialization", "Salary", "Hire Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(25, 28, 48));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 54, 80)));
        scrollPane.getViewport().setBackground(new Color(30, 32, 45));
        scrollPane.setBackground(new Color(30, 32, 45));
        scrollPane.getVerticalScrollBar().setBackground(new Color(30, 32, 45));

        // Form
        JPanel formPanel = new JPanel(new GridLayout(0, 4, 10, 8));
        formPanel.setBackground(new Color(22, 25, 40));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 54, 80)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        fName = new JTextField(); styleField(fName);
        fEmail = new JTextField(); styleField(fEmail);
        fPhone = new JTextField(); styleField(fPhone);
        fSpecialization = new JTextField(); styleField(fSpecialization);
        fSalary = new JTextField(); styleField(fSalary);
        fHireDate = new JTextField(); styleField(fHireDate); fHireDate.setToolTipText("YYYY-MM-DD");
        fStatus = new JComboBox<>(new String[]{"Active", "Inactive"}); styleCombo(fStatus);

        formPanel.add(createLabel("Name*:")); formPanel.add(fName);
        formPanel.add(createLabel("Email:")); formPanel.add(fEmail);
        formPanel.add(createLabel("Phone*:")); formPanel.add(fPhone);
        formPanel.add(createLabel("Specialization*:")); formPanel.add(fSpecialization);
        formPanel.add(createLabel("Salary*:")); formPanel.add(fSalary);
        formPanel.add(createLabel("Hire Date* (YYYY-MM-DD):")); formPanel.add(fHireDate);
        formPanel.add(createLabel("Status:")); formPanel.add(fStatus);
        formPanel.add(new JLabel()); // spacer

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setOpaque(false);
        if ("Admin".equals(role)) {
            JButton btnAdd = createButton("➕ Add", new Color(76, 175, 80));
            JButton btnUpdate = createButton("✏ Update", new Color(255, 152, 0));
            JButton btnDelete = createButton("🗑 Delete", new Color(244, 67, 54));
            JButton btnClearForm = createButton("🔄 Clear", new Color(100, 100, 130));
            btnAdd.addActionListener(e -> addTrainer());
            btnUpdate.addActionListener(e -> updateTrainer());
            btnDelete.addActionListener(e -> deleteTrainer());
            btnClearForm.addActionListener(e -> clearForm());
            actionPanel.add(btnAdd); actionPanel.add(btnUpdate);
            actionPanel.add(btnDelete); actionPanel.add(btnClearForm);
        } else {
            JLabel v = new JLabel("  👁 View Only Mode"); v.setForeground(new Color(150,150,170)); actionPanel.add(v);
        }

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, bottomPanel);
        split.setDividerLocation(350); split.setOpaque(false); split.setBorder(null); split.setDividerSize(6);
        add(split, BorderLayout.CENTER);
    }

    private void loadData(String search) {
        tableModel.setRowCount(0);
        String query = "SELECT id,name,email,phone,specialization,salary,hire_date,status FROM trainers" +
                (search.isEmpty() ? "" : " WHERE name LIKE ? OR phone LIKE ? OR specialization LIKE ?") + " ORDER BY id DESC";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            if (!search.isEmpty()) { String l = "%" + search + "%"; ps.setString(1,l); ps.setString(2,l); ps.setString(3,l); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"),rs.getString("name"),rs.getString("email"),
                        rs.getString("phone"),rs.getString("specialization"),rs.getDouble("salary"),
                        rs.getString("hire_date"),rs.getString("status")});
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: "+e.getMessage()); }
    }

    private void loadSelectedToForm() {
        int row = table.getSelectedRow(); if (row == -1) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        fName.setText((String) tableModel.getValueAt(row, 1));
        fEmail.setText(tableModel.getValueAt(row, 2) != null ? (String) tableModel.getValueAt(row, 2) : "");
        fPhone.setText((String) tableModel.getValueAt(row, 3));
        fSpecialization.setText((String) tableModel.getValueAt(row, 4));
        fSalary.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        fHireDate.setText((String) tableModel.getValueAt(row, 6));
        fStatus.setSelectedItem(tableModel.getValueAt(row, 7));
    }

    private boolean validateForm() {
        if (fName.getText().trim().isEmpty()) { showError("Name required!"); return false; }
        if (fPhone.getText().trim().isEmpty() || !fPhone.getText().trim().matches("\\d{10,15}")) { showError("Valid phone (10-15 digits) required!"); return false; }
        if (fSpecialization.getText().trim().isEmpty()) { showError("Specialization required!"); return false; }
        try { Double.parseDouble(fSalary.getText().trim()); } catch (Exception e) { showError("Salary must be a number!"); return false; }
        if (!fHireDate.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) { showError("Hire Date format: YYYY-MM-DD"); return false; }
        return true;
    }

    private void addTrainer() {
        if (!validateForm()) return;
        String sql = "INSERT INTO trainers (name,email,phone,specialization,salary,hire_date,status) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fName.getText().trim()); ps.setString(2, fEmail.getText().trim());
            ps.setString(3, fPhone.getText().trim()); ps.setString(4, fSpecialization.getText().trim());
            ps.setDouble(5, Double.parseDouble(fSalary.getText().trim()));
            ps.setString(6, fHireDate.getText().trim()); ps.setString(7, (String) fStatus.getSelectedItem());
            ps.executeUpdate(); showSuccess("Trainer added!"); clearForm(); loadData("");
        } catch (SQLException e) { showError("Error: "+e.getMessage()); }
    }

    private void updateTrainer() {
        if (selectedId == -1) { showError("Select a trainer!"); return; }
        if (!validateForm()) return;
        String sql = "UPDATE trainers SET name=?,email=?,phone=?,specialization=?,salary=?,hire_date=?,status=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fName.getText().trim()); ps.setString(2, fEmail.getText().trim());
            ps.setString(3, fPhone.getText().trim()); ps.setString(4, fSpecialization.getText().trim());
            ps.setDouble(5, Double.parseDouble(fSalary.getText().trim()));
            ps.setString(6, fHireDate.getText().trim()); ps.setString(7, (String) fStatus.getSelectedItem());
            ps.setInt(8, selectedId);
            ps.executeUpdate(); showSuccess("Trainer updated!"); clearForm(); loadData("");
        } catch (SQLException e) { showError("Error: "+e.getMessage()); }
    }

    private void deleteTrainer() {
        if (selectedId == -1) { showError("Select a trainer!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete this trainer?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM trainers WHERE id=?")) {
            ps.setInt(1, selectedId); ps.executeUpdate(); showSuccess("Deleted!"); clearForm(); loadData("");
        } catch (SQLException e) { showError("Error: "+e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1; fName.setText(""); fEmail.setText(""); fPhone.setText("");
        fSpecialization.setText(""); fSalary.setText(""); fHireDate.setText("");
        fStatus.setSelectedIndex(0); table.clearSelection();
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private JLabel createLabel(String t) { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(new Color(180,180,200)); return l; }
    private void styleField(JTextField f) { f.setBackground(new Color(38,42,65)); f.setForeground(new Color(220,220,240)); f.setCaretColor(Color.WHITE); f.setFont(new Font("Segoe UI",Font.PLAIN,13)); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,65,90)),BorderFactory.createEmptyBorder(4,8,4,8))); }
    private void styleCombo(JComboBox<?> c) { c.setBackground(new Color(38,42,65)); c.setForeground(new Color(220,220,240)); c.setFont(new Font("Segoe UI",Font.PLAIN,13)); }
    private void styleTable(JTable t) { t.setBackground(new Color(25,28,48)); t.setForeground(new Color(210,210,230)); t.setGridColor(new Color(40,44,68)); t.setRowHeight(28); t.setFont(new Font("Segoe UI",Font.PLAIN,13)); t.setSelectionBackground(new Color(33,150,243,80)); t.setSelectionForeground(Color.WHITE); t.getTableHeader().setBackground(new Color(30,34,55)); t.getTableHeader().setForeground(new Color(33,150,243)); t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13)); }
    private JButton createButton(String text, Color bg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setBorder(BorderFactory.createEmptyBorder(7,16,7,16)); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
}
