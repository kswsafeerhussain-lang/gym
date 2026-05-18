package gym;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * PaymentsPanel - Full CRUD + Search for Payments
 * Links payments to members via foreign key
 */
public class PaymentsPanel extends JPanel {

    private String role;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> fMember, fPaymentMethod;
    private JTextField fAmount, fDate, fDescription;
    private int selectedId = -1;

    public PaymentsPanel(String role) {
        this.role = role;
        initComponents();
        loadData("");
    }

    private void initComponents() {
        setBackground(new Color(18, 20, 32));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("💳 Payments Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(156, 39, 176));
        add(lblTitle, BorderLayout.NORTH);

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(20); styleField(txtSearch);
        JButton btnSearch = createButton("🔍 Search", new Color(156, 39, 176));
        JButton btnClear = createButton("✖ Clear", new Color(100, 100, 130));
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnClear.addActionListener(e -> { txtSearch.setText(""); loadData(""); });
        searchPanel.add(createLabel("Search by member:")); searchPanel.add(txtSearch);
        searchPanel.add(btnSearch); searchPanel.add(btnClear);

        // Table
        String[] cols = {"ID", "Member Name", "Amount (PKR)", "Date", "Method", "Description"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) loadSelectedToForm();
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(25, 28, 48));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 54, 80)));

        // Form
        JPanel formPanel = new JPanel(new GridLayout(0, 4, 10, 8));
        formPanel.setBackground(new Color(22, 25, 40));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 54, 80)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        fMember = new JComboBox<>(); styleCombo(fMember); loadMembersIntoCombo();
        fAmount = new JTextField(); styleField(fAmount);
        fDate = new JTextField(); styleField(fDate); fDate.setToolTipText("YYYY-MM-DD");
        fDescription = new JTextField(); styleField(fDescription);
        fPaymentMethod = new JComboBox<>(new String[]{"Cash", "Card", "Online"}); styleCombo(fPaymentMethod);

        formPanel.add(createLabel("Member*:")); formPanel.add(fMember);
        formPanel.add(createLabel("Amount* (PKR):")); formPanel.add(fAmount);
        formPanel.add(createLabel("Date* (YYYY-MM-DD):")); formPanel.add(fDate);
        formPanel.add(createLabel("Payment Method*:")); formPanel.add(fPaymentMethod);
        formPanel.add(createLabel("Description:")); formPanel.add(fDescription);
        formPanel.add(new JLabel()); formPanel.add(new JLabel());

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setOpaque(false);
        if ("Admin".equals(role)) {
            JButton btnAdd = createButton("➕ Add Payment", new Color(76, 175, 80));
            JButton btnUpdate = createButton("✏ Update", new Color(255, 152, 0));
            JButton btnDelete = createButton("🗑 Delete", new Color(244, 67, 54));
            JButton btnClearForm = createButton("🔄 Clear", new Color(100, 100, 130));
            btnAdd.addActionListener(e -> addPayment());
            btnUpdate.addActionListener(e -> updatePayment());
            btnDelete.addActionListener(e -> deletePayment());
            btnClearForm.addActionListener(e -> clearForm());
            actionPanel.add(btnAdd); actionPanel.add(btnUpdate);
            actionPanel.add(btnDelete); actionPanel.add(btnClearForm);
        } else {
            JLabel v = new JLabel("  👁 View Only"); v.setForeground(new Color(150,150,170)); actionPanel.add(v);
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

    private void loadMembersIntoCombo() {
        fMember.removeAllItems();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM members WHERE status='Active' ORDER BY name")) {
            while (rs.next()) {
                fMember.addItem(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) { System.err.println("Combo load error: " + e.getMessage()); }
    }

    private void loadData(String search) {
        tableModel.setRowCount(0);
        String query = "SELECT p.id, m.name, p.amount, p.payment_date, p.payment_method, p.description " +
                "FROM payments p JOIN members m ON p.member_id=m.id" +
                (search.isEmpty() ? "" : " WHERE m.name LIKE ?") + " ORDER BY p.id DESC";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            if (!search.isEmpty()) ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"),
                        "Rs. " + rs.getDouble("amount"), rs.getString("payment_date"),
                        rs.getString("payment_method"), rs.getString("description")});
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void loadSelectedToForm() {
        int row = table.getSelectedRow(); if (row == -1) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        String amountStr = (String) tableModel.getValueAt(row, 2);
        fAmount.setText(amountStr.replace("Rs. ", ""));
        fDate.setText((String) tableModel.getValueAt(row, 3));
        fPaymentMethod.setSelectedItem(tableModel.getValueAt(row, 4));
        fDescription.setText(tableModel.getValueAt(row, 5) != null ? (String) tableModel.getValueAt(row, 5) : "");
    }

    private boolean validateForm() {
        if (fMember.getSelectedItem() == null) { showError("Select a member!"); return false; }
        try { double a = Double.parseDouble(fAmount.getText().trim()); if (a <= 0) throw new Exception(); }
        catch (Exception e) { showError("Amount must be a positive number!"); return false; }
        if (!fDate.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) { showError("Date format: YYYY-MM-DD"); return false; }
        return true;
    }

    private int getSelectedMemberId() {
        String selected = (String) fMember.getSelectedItem();
        if (selected == null) return -1;
        return Integer.parseInt(selected.split(" - ")[0]);
    }

    private void addPayment() {
        if (!validateForm()) return;
        String sql = "INSERT INTO payments (member_id,amount,payment_date,payment_method,description) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, getSelectedMemberId());
            ps.setDouble(2, Double.parseDouble(fAmount.getText().trim()));
            ps.setString(3, fDate.getText().trim());
            ps.setString(4, (String) fPaymentMethod.getSelectedItem());
            ps.setString(5, fDescription.getText().trim());
            ps.executeUpdate(); showSuccess("Payment recorded!"); clearForm(); loadData("");
        } catch (SQLException e) { showError("Error: " + e.getMessage()); }
    }

    private void updatePayment() {
        if (selectedId == -1) { showError("Select a payment!"); return; }
        if (!validateForm()) return;
        String sql = "UPDATE payments SET member_id=?,amount=?,payment_date=?,payment_method=?,description=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, getSelectedMemberId());
            ps.setDouble(2, Double.parseDouble(fAmount.getText().trim()));
            ps.setString(3, fDate.getText().trim());
            ps.setString(4, (String) fPaymentMethod.getSelectedItem());
            ps.setString(5, fDescription.getText().trim());
            ps.setInt(6, selectedId);
            ps.executeUpdate(); showSuccess("Updated!"); clearForm(); loadData("");
        } catch (SQLException e) { showError("Error: " + e.getMessage()); }
    }

    private void deletePayment() {
        if (selectedId == -1) { showError("Select a payment!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete this payment record?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM payments WHERE id=?")) {
            ps.setInt(1, selectedId); ps.executeUpdate(); showSuccess("Deleted!"); clearForm(); loadData("");
        } catch (SQLException e) { showError("Error: " + e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1; fAmount.setText(""); fDate.setText(""); fDescription.setText("");
        fPaymentMethod.setSelectedIndex(0); if (fMember.getItemCount() > 0) fMember.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private JLabel createLabel(String t) { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(new Color(180,180,200)); return l; }
    private void styleField(JTextField f) { f.setBackground(new Color(38,42,65)); f.setForeground(new Color(220,220,240)); f.setCaretColor(Color.WHITE); f.setFont(new Font("Segoe UI",Font.PLAIN,13)); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,65,90)),BorderFactory.createEmptyBorder(4,8,4,8))); }
    private void styleCombo(JComboBox<?> c) { c.setBackground(new Color(38,42,65)); c.setForeground(new Color(220,220,240)); c.setFont(new Font("Segoe UI",Font.PLAIN,13)); }
    private void styleTable(JTable t) { t.setBackground(new Color(25,28,48)); t.setForeground(new Color(210,210,230)); t.setGridColor(new Color(40,44,68)); t.setRowHeight(28); t.setFont(new Font("Segoe UI",Font.PLAIN,13)); t.setSelectionBackground(new Color(156,39,176,80)); t.setSelectionForeground(Color.WHITE); t.getTableHeader().setBackground(new Color(30,34,55)); t.getTableHeader().setForeground(new Color(156,39,176)); t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13)); }
    private JButton createButton(String text, Color bg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setBorder(BorderFactory.createEmptyBorder(7,16,7,16)); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
}
