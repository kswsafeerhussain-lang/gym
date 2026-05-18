package gym;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * UsersPanel - Admin-only user management
 * Add/Delete users, change roles
 */
public class UsersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField fUsername, fPassword;
    private JComboBox<String> fRole;
    private int selectedId = -1;

    public UsersPanel() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setBackground(new Color(18, 20, 32));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("⚙ User Management (Admin Only)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(255, 152, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Username", "Role", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                selectedId = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
                fUsername.setText((String) tableModel.getValueAt(table.getSelectedRow(), 1));
                fRole.setSelectedItem(tableModel.getValueAt(table.getSelectedRow(), 2));
                fPassword.setText("");
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(25, 28, 48));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 54, 80)));

        // Form
        JPanel formPanel = new JPanel(new GridLayout(0, 4, 10, 8));
        formPanel.setBackground(new Color(22, 25, 40));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 54, 80)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        fUsername = new JTextField(); styleField(fUsername);
        fPassword = new JTextField(); styleField(fPassword);
        fRole = new JComboBox<>(new String[]{"Admin", "User"}); styleCombo(fRole);

        formPanel.add(createLabel("Username*:")); formPanel.add(fUsername);
        formPanel.add(createLabel("Password*:")); formPanel.add(fPassword);
        formPanel.add(createLabel("Role:")); formPanel.add(fRole);
        formPanel.add(new JLabel()); formPanel.add(new JLabel());

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setOpaque(false);
        JButton btnAdd = createButton("➕ Add User", new Color(76, 175, 80));
        JButton btnDelete = createButton("🗑 Delete User", new Color(244, 67, 54));
        JButton btnClear = createButton("🔄 Clear", new Color(100, 100, 130));

        btnAdd.addActionListener(e -> addUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());

        actionPanel.add(btnAdd); actionPanel.add(btnDelete); actionPanel.add(btnClear);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, bottomPanel);
        split.setDividerLocation(300); split.setOpaque(false); split.setBorder(null); split.setDividerSize(6);
        add(split, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, role, created_at FROM users ORDER BY id")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("username"),
                        rs.getString("role"), rs.getString("created_at")});
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void addUser() {
        String uname = fUsername.getText().trim();
        String pass = fPassword.getText().trim();
        if (uname.isEmpty()) { showError("Username required!"); return; }
        if (pass.length() < 6) { showError("Password must be at least 6 characters!"); return; }
        String sql = "INSERT INTO users (username, password, role) VALUES (?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uname); ps.setString(2, pass); ps.setString(3, (String) fRole.getSelectedItem());
            ps.executeUpdate(); showSuccess("User added!"); clearForm(); loadData();
        } catch (SQLException e) { showError("Error: " + e.getMessage()); }
    }

    private void deleteUser() {
        if (selectedId == -1) { showError("Select a user!"); return; }
        String uname = (String) tableModel.getValueAt(table.getSelectedRow(), 1);
        if ("admin".equals(uname)) { showError("Cannot delete default admin!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete user '" + uname + "'?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, selectedId); ps.executeUpdate(); showSuccess("Deleted!"); clearForm(); loadData();
        } catch (SQLException e) { showError("Error: " + e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1; fUsername.setText(""); fPassword.setText(""); fRole.setSelectedIndex(0); table.clearSelection();
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private JLabel createLabel(String t) { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(new Color(180,180,200)); return l; }
    private void styleField(JTextField f) { f.setBackground(new Color(38,42,65)); f.setForeground(new Color(220,220,240)); f.setCaretColor(Color.WHITE); f.setFont(new Font("Segoe UI",Font.PLAIN,13)); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,65,90)),BorderFactory.createEmptyBorder(4,8,4,8))); }
    private void styleCombo(JComboBox<?> c) { c.setBackground(new Color(38,42,65)); c.setForeground(new Color(220,220,240)); c.setFont(new Font("Segoe UI",Font.PLAIN,13)); }
    private void styleTable(JTable t) { t.setBackground(new Color(25,28,48)); t.setForeground(new Color(210,210,230)); t.setGridColor(new Color(40,44,68)); t.setRowHeight(28); t.setFont(new Font("Segoe UI",Font.PLAIN,13)); t.setSelectionBackground(new Color(255,152,0,80)); t.setSelectionForeground(Color.WHITE); t.getTableHeader().setBackground(new Color(30,34,55)); t.getTableHeader().setForeground(new Color(255,152,0)); t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13)); }
    private JButton createButton(String text, Color bg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setBorder(BorderFactory.createEmptyBorder(7,16,7,16)); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
}
