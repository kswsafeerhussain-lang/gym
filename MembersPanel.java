package gym;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

/**
 * MembersPanel - Full CRUD + Search/Filter for Members
 * Role-based: User can only view; Admin can add/edit/delete
 */
public class MembersPanel extends JPanel {

    private String role;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;

    // Form fields
    private JTextField fName, fEmail, fPhone, fAddress, fJoinDate, fExpiryDate;
    private JComboBox<String> fMembershipType, fStatus;
    private int selectedId = -1;

    public MembersPanel(String role) {
        this.role = role;
        initComponents();
        loadData("");
    }

    private void initComponents() {
        setBackground(new Color(18, 20, 32));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel lblTitle = new JLabel("👥 Members Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(255, 87, 34));
        titleBar.add(lblTitle, BorderLayout.WEST);
        add(titleBar, BorderLayout.NORTH);

        // Search/Filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);

        txtSearch = new JTextField(20);
        styleField(txtSearch);
        txtSearch.setToolTipText("Search by name, email, or phone");

        cmbFilter = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
        styleCombo(cmbFilter);

        JButton btnSearch = createButton("🔍 Search", new Color(33, 150, 243));
        JButton btnClear = createButton("✖ Clear", new Color(100, 100, 130));

        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnClear.addActionListener(e -> { txtSearch.setText(""); cmbFilter.setSelectedIndex(0); loadData(""); });
        cmbFilter.addActionListener(e -> loadData(txtSearch.getText().trim()));

        searchPanel.add(new JLabel("  ") {{ setForeground(Color.WHITE); }});
        searchPanel.add(createLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(createLabel("Status:"));
        searchPanel.add(cmbFilter);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);

        // Table
        String[] cols = {"ID", "Name", "Email", "Phone", "Membership", "Join Date", "Expiry", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedToForm();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(new Color(25, 28, 48));
        scrollPane.getViewport().setBackground(new Color(25, 28, 48));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 54, 80)));

        // Form panel
        JPanel formPanel = createFormPanel();

        // Center: table + search
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setOpaque(false);

        if ("Admin".equals(role)) {
            JButton btnAdd = createButton("➕ Add", new Color(76, 175, 80));
            JButton btnUpdate = createButton("✏ Update", new Color(255, 152, 0));
            JButton btnDelete = createButton("🗑 Delete", new Color(244, 67, 54));
            JButton btnClearForm = createButton("🔄 Clear Form", new Color(100, 100, 130));

            btnAdd.addActionListener(e -> addMember());
            btnUpdate.addActionListener(e -> updateMember());
            btnDelete.addActionListener(e -> deleteMember());
            btnClearForm.addActionListener(e -> clearForm());

            actionPanel.add(btnAdd);
            actionPanel.add(btnUpdate);
            actionPanel.add(btnDelete);
            actionPanel.add(btnClearForm);
        } else {
            JLabel lblView = new JLabel("  👁 View Only Mode (User Role)");
            lblView.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblView.setForeground(new Color(150, 150, 170));
            actionPanel.add(lblView);
        }

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, bottomPanel);
        splitPane.setDividerLocation(380);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(0, 4, 10, 8));
        formPanel.setBackground(new Color(22, 25, 40));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 54, 80)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        fName = new JTextField(); styleField(fName);
        fEmail = new JTextField(); styleField(fEmail);
        fPhone = new JTextField(); styleField(fPhone);
        fAddress = new JTextField(); styleField(fAddress);
        fJoinDate = new JTextField(); styleField(fJoinDate); fJoinDate.setToolTipText("YYYY-MM-DD");
        fExpiryDate = new JTextField(); styleField(fExpiryDate); fExpiryDate.setToolTipText("YYYY-MM-DD");
        fMembershipType = new JComboBox<>(new String[]{"Basic", "Standard", "Premium"}); styleCombo(fMembershipType);
        fStatus = new JComboBox<>(new String[]{"Active", "Inactive"}); styleCombo(fStatus);

        formPanel.add(createLabel("Name*:")); formPanel.add(fName);
        formPanel.add(createLabel("Email:")); formPanel.add(fEmail);
        formPanel.add(createLabel("Phone*:")); formPanel.add(fPhone);
        formPanel.add(createLabel("Address:")); formPanel.add(fAddress);
        formPanel.add(createLabel("Membership*:")); formPanel.add(fMembershipType);
        formPanel.add(createLabel("Join Date* (YYYY-MM-DD):")); formPanel.add(fJoinDate);
        formPanel.add(createLabel("Expiry Date* (YYYY-MM-DD):")); formPanel.add(fExpiryDate);
        formPanel.add(createLabel("Status:")); formPanel.add(fStatus);

        return formPanel;
    }

    private void loadData(String search) {
        tableModel.setRowCount(0);
        String statusFilter = (String) cmbFilter.getSelectedItem();

        StringBuilder query = new StringBuilder(
                "SELECT id, name, email, phone, membership_type, join_date, expiry_date, status FROM members WHERE 1=1");

        if (!search.isEmpty()) {
            query.append(" AND (name LIKE ? OR email LIKE ? OR phone LIKE ?)");
        }
        if (!"All".equals(statusFilter)) {
            query.append(" AND status='" + statusFilter + "'");
        }
        query.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {

            if (!search.isEmpty()) {
                String like = "%" + search + "%";
                ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                        rs.getString("phone"), rs.getString("membership_type"),
                        rs.getString("join_date"), rs.getString("expiry_date"), rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedToForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        fName.setText((String) tableModel.getValueAt(row, 1));
        fEmail.setText(tableModel.getValueAt(row, 2) != null ? (String) tableModel.getValueAt(row, 2) : "");
        fPhone.setText((String) tableModel.getValueAt(row, 3));
        fMembershipType.setSelectedItem(tableModel.getValueAt(row, 4));
        fJoinDate.setText((String) tableModel.getValueAt(row, 5));
        fExpiryDate.setText((String) tableModel.getValueAt(row, 6));
        fStatus.setSelectedItem(tableModel.getValueAt(row, 7));
    }

    // Validate form inputs
    private boolean validateForm() {
        if (fName.getText().trim().isEmpty()) { showError("Name is required!"); fName.requestFocus(); return false; }
        if (fPhone.getText().trim().isEmpty()) { showError("Phone is required!"); fPhone.requestFocus(); return false; }
        if (!fPhone.getText().trim().matches("\\d{10,15}")) { showError("Phone must be 10-15 digits!"); fPhone.requestFocus(); return false; }
        if (!fEmail.getText().trim().isEmpty() && !fEmail.getText().trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Invalid email format!"); fEmail.requestFocus(); return false;
        }
        if (!fJoinDate.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) { showError("Join Date format: YYYY-MM-DD"); fJoinDate.requestFocus(); return false; }
        if (!fExpiryDate.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) { showError("Expiry Date format: YYYY-MM-DD"); fExpiryDate.requestFocus(); return false; }
        return true;
    }

    private void addMember() {
        if (!validateForm()) return;
        String sql = "INSERT INTO members (name,email,phone,address,membership_type,join_date,expiry_date,status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fName.getText().trim());
            ps.setString(2, fEmail.getText().trim());
            ps.setString(3, fPhone.getText().trim());
            ps.setString(4, fAddress.getText().trim());
            ps.setString(5, (String) fMembershipType.getSelectedItem());
            ps.setString(6, fJoinDate.getText().trim());
            ps.setString(7, fExpiryDate.getText().trim());
            ps.setString(8, (String) fStatus.getSelectedItem());
            ps.executeUpdate();
            showSuccess("Member added successfully!");
            clearForm(); loadData("");
        } catch (SQLException e) {
            showError("Add Error: " + e.getMessage());
        }
    }

    private void updateMember() {
        if (selectedId == -1) { showError("Please select a member to update!"); return; }
        if (!validateForm()) return;
        String sql = "UPDATE members SET name=?,email=?,phone=?,address=?,membership_type=?,join_date=?,expiry_date=?,status=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fName.getText().trim());
            ps.setString(2, fEmail.getText().trim());
            ps.setString(3, fPhone.getText().trim());
            ps.setString(4, fAddress.getText().trim());
            ps.setString(5, (String) fMembershipType.getSelectedItem());
            ps.setString(6, fJoinDate.getText().trim());
            ps.setString(7, fExpiryDate.getText().trim());
            ps.setString(8, (String) fStatus.getSelectedItem());
            ps.setInt(9, selectedId);
            ps.executeUpdate();
            showSuccess("Member updated successfully!");
            clearForm(); loadData("");
        } catch (SQLException e) {
            showError("Update Error: " + e.getMessage());
        }
    }

    private void deleteMember() {
        if (selectedId == -1) { showError("Please select a member to delete!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this member?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM members WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            showSuccess("Member deleted!");
            clearForm(); loadData("");
        } catch (SQLException e) {
            showError("Delete Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        fName.setText(""); fEmail.setText(""); fPhone.setText("");
        fAddress.setText(""); fJoinDate.setText(""); fExpiryDate.setText("");
        fMembershipType.setSelectedIndex(0); fStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    // ---- Utility methods ----
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(180, 180, 200));
        return lbl;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(38, 42, 65));
        f.setForeground(new Color(220, 220, 240));
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 65, 90)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(new Color(38, 42, 65));
        c.setForeground(new Color(220, 220, 240));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(25, 28, 48));
        t.setForeground(new Color(210, 210, 230));
        t.setGridColor(new Color(40, 44, 68));
        t.setRowHeight(28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setSelectionBackground(new Color(255, 87, 34, 80));
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(new Color(30, 34, 55));
        t.getTableHeader().setForeground(new Color(255, 87, 34));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        t.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        t.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
