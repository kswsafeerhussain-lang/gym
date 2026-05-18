package gym;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

/**
 * HomePanel - Dashboard home with summary stats
 */
public class HomePanel extends JPanel {

    private String username;
    private String role;

    public HomePanel(String username, String role) {
        this.username = username;
        this.role = role;
        initComponents();
    }

    private void initComponents() {
        setBackground(new Color(18, 20, 32));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Welcome header
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);

        JLabel lblGreet = new JLabel("Welcome back, " + username + "! 👋");
        lblGreet.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblGreet.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel("Logged in as: " + role + "  •  " + new java.util.Date().toString());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRole.setForeground(new Color(150, 150, 170));

        welcomePanel.add(lblGreet, BorderLayout.NORTH);
        welcomePanel.add(lblRole, BorderLayout.SOUTH);
        add(welcomePanel, BorderLayout.NORTH);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        statsPanel.add(createStatCard("👥 Total Members", getCount("members"), new Color(255, 87, 34)));
        statsPanel.add(createStatCard("🏋 Total Trainers", getCount("trainers"), new Color(33, 150, 243)));
        statsPanel.add(createStatCard("🔧 Equipment Items", getCount("equipment"), new Color(76, 175, 80)));
        statsPanel.add(createStatCard("💳 Total Payments", getCount("payments"), new Color(156, 39, 176)));

        add(statsPanel, BorderLayout.CENTER);

        // Footer note
        JLabel lblFooter = new JLabel("Gym Management System © 2025 | Mehran University Project", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(new Color(80, 85, 110));
        lblFooter.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        add(lblFooter, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, int count, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(25, 28, 48));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(accent);
                g2d.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());

        JLabel lblCount = new JLabel(String.valueOf(count));
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblCount.setForeground(accent);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(180, 180, 200));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        lblCount.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(lblCount);
        inner.add(Box.createVerticalStrut(5));
        inner.add(lblTitle);

        card.add(inner);
        return card;
    }

    private int getCount(String table) {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Count error: " + e.getMessage());
        }
        return 0;
    }
}
