package gym;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ReportsPanel - Summary reports for Gym Management
 * Includes: Revenue summary, Member stats, Trainer list, Equipment status
 */
public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        initComponents();
    }

    private void initComponents() {
        setBackground(new Color(18, 20, 32));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("📊 Reports & Analytics");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(255, 193, 7));
        JButton btnExport = createButton("💾 Export Report (TXT)", new Color(76, 175, 80));
        btnExport.addActionListener(e -> exportReport());
        titlePanel.add(lblTitle, BorderLayout.WEST);
        titlePanel.add(btnExport, BorderLayout.EAST);
        add(titlePanel, BorderLayout.NORTH);

        // Tabbed reports
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(22, 25, 40));
        tabs.setForeground(new Color(200, 200, 220));
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabs.addTab("💰 Revenue Summary", createRevenueReport());
        tabs.addTab("👥 Member Stats", createMemberStats());
        tabs.addTab("🏋 Trainer List", createTrainerReport());
        tabs.addTab("🔧 Equipment Status", createEquipmentReport());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createRevenueReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(22, 25, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Summary cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(0, 100));

        double totalRevenue = 0, monthRevenue = 0;
        int totalPayments = 0;

        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT SUM(amount) as total, COUNT(*) as cnt FROM payments");
            if (rs.next()) { totalRevenue = rs.getDouble("total"); totalPayments = rs.getInt("cnt"); }
            rs = stmt.executeQuery("SELECT SUM(amount) as month FROM payments WHERE strftime('%Y-%m', payment_date) = strftime('%Y-%m', 'now')");
            if (rs.next()) monthRevenue = rs.getDouble("month");
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        summaryPanel.add(createMiniCard("Total Revenue", "Rs. " + String.format("%.2f", totalRevenue), new Color(76, 175, 80)));
        summaryPanel.add(createMiniCard("This Month", "Rs. " + String.format("%.2f", monthRevenue), new Color(33, 150, 243)));
        summaryPanel.add(createMiniCard("Total Transactions", String.valueOf(totalPayments), new Color(255, 152, 0)));

        // Payment table
        String[] cols = {"Member", "Amount (PKR)", "Date", "Method"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT m.name, p.amount, p.payment_date, p.payment_method " +
                     "FROM payments p JOIN members m ON p.member_id=m.id ORDER BY p.id DESC LIMIT 50")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), "Rs. " + rs.getDouble("amount"),
                        rs.getString("payment_date"), rs.getString("payment_method")});
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(25, 28, 48));

        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMemberStats() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(22, 25, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Name", "Phone", "Membership", "Join Date", "Expiry Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name, phone, membership_type, join_date, expiry_date, status FROM members ORDER BY status, expiry_date")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), rs.getString("phone"),
                        rs.getString("membership_type"), rs.getString("join_date"),
                        rs.getString("expiry_date"), rs.getString("status")});
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        JTable table = new JTable(model);
        styleTable(table);
        // Highlight expired members
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                String status = (String) model.getValueAt(r, 5);
                if ("Inactive".equals(status)) {
                    comp.setForeground(new Color(244, 67, 54));
                } else {
                    comp.setForeground(new Color(210, 210, 230));
                }
                comp.setBackground(sel ? new Color(255, 87, 34, 80) : new Color(25, 28, 48));
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(25, 28, 48));
        panel.add(scroll, BorderLayout.CENTER);

        JLabel note = new JLabel("  🔴 Red = Inactive Members");
        note.setForeground(new Color(244, 67, 54));
        note.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTrainerReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(22, 25, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Name", "Phone", "Specialization", "Salary (PKR)", "Hire Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name,phone,specialization,salary,hire_date,status FROM trainers ORDER BY name")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), rs.getString("phone"),
                        rs.getString("specialization"), "Rs. " + rs.getDouble("salary"),
                        rs.getString("hire_date"), rs.getString("status")});
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(25, 28, 48));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEquipmentReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(22, 25, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Name", "Category", "Quantity", "Condition", "Purchase Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name,category,quantity,condition_status,purchase_date FROM equipment ORDER BY condition_status")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), rs.getString("category"),
                        rs.getInt("quantity"), rs.getString("condition_status"), rs.getString("purchase_date")});
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        JTable table = new JTable(model);
        styleTable(table);
        // Color code by condition
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                String cond = (String) model.getValueAt(r, 3);
                Color fg = switch (cond) {
                    case "Good" -> new Color(76, 175, 80);
                    case "Fair" -> new Color(255, 193, 7);
                    case "Poor" -> new Color(244, 67, 54);
                    case "Under Repair" -> new Color(255, 152, 0);
                    default -> new Color(210, 210, 230);
                };
                setForeground(sel ? Color.WHITE : fg);
                setBackground(sel ? new Color(76, 175, 80, 80) : new Color(25, 28, 48));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(25, 28, 48));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legend.setBackground(new Color(22, 25, 40));
        legend.add(colorLabel("🟢 Good", new Color(76, 175, 80)));
        legend.add(colorLabel("  🟡 Fair", new Color(255, 193, 7)));
        legend.add(colorLabel("  🔴 Poor", new Color(244, 67, 54)));
        legend.add(colorLabel("  🟠 Under Repair", new Color(255, 152, 0)));
        panel.add(legend, BorderLayout.SOUTH);
        return panel;
    }

    // Export report to text file
    private void exportReport() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("GymReport_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            pw.println("========================================");
            pw.println("     GYM MANAGEMENT SYSTEM - REPORT    ");
            pw.println("     Generated: " + new Date());
            pw.println("========================================\n");

            try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement()) {
                // Revenue
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt, SUM(amount) as total FROM payments");
                if (rs.next()) {
                    pw.println("=== REVENUE SUMMARY ===");
                    pw.println("Total Transactions: " + rs.getInt("cnt"));
                    pw.println("Total Revenue: Rs. " + String.format("%.2f", rs.getDouble("total")));
                    pw.println();
                }
                // Members
                rs = stmt.executeQuery("SELECT COUNT(*) as total, SUM(CASE WHEN status='Active' THEN 1 ELSE 0 END) as active FROM members");
                if (rs.next()) {
                    pw.println("=== MEMBER SUMMARY ===");
                    pw.println("Total Members: " + rs.getInt("total"));
                    pw.println("Active Members: " + rs.getInt("active"));
                    pw.println("Inactive Members: " + (rs.getInt("total") - rs.getInt("active")));
                    pw.println();
                }
                // Trainers
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM trainers WHERE status='Active'");
                if (rs.next()) {
                    pw.println("=== TRAINER SUMMARY ===");
                    pw.println("Active Trainers: " + rs.getInt("total"));
                    pw.println();
                }
                // Equipment
                rs = stmt.executeQuery("SELECT condition_status, COUNT(*) as cnt FROM equipment GROUP BY condition_status");
                pw.println("=== EQUIPMENT STATUS ===");
                while (rs.next()) {
                    pw.println(rs.getString("condition_status") + ": " + rs.getInt("cnt") + " items");
                }
            }
            pw.println("\n========================================");
            pw.println("           END OF REPORT               ");
            pw.println("========================================");

            JOptionPane.showMessageDialog(this, "Report exported successfully!\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createMiniCard(String title, String value, Color color) {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(25, 28, 48));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel lv = new JLabel(value); lv.setFont(new Font("Segoe UI", Font.BOLD, 22)); lv.setForeground(color);
        JLabel lt = new JLabel(title); lt.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lt.setForeground(new Color(160, 160, 180));
        card.add(lv); card.add(Box.createVerticalStrut(4)); card.add(lt);
        return card;
    }

    private JLabel colorLabel(String text, Color c) {
        JLabel l = new JLabel(text); l.setForeground(c); l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); return l;
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(25,28,48)); t.setForeground(new Color(210,210,230));
        t.setGridColor(new Color(40,44,68)); t.setRowHeight(28); t.setFont(new Font("Segoe UI",Font.PLAIN,13));
        t.setSelectionBackground(new Color(255,193,7,80)); t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(new Color(30,34,55)); t.getTableHeader().setForeground(new Color(255,193,7));
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13));
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setBorder(BorderFactory.createEmptyBorder(7,16,7,16));
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
}
