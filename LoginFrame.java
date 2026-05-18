package gym;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

/**
 * LoginFrame - Authentication Module
 * Supports Admin and User roles
 * Validates credentials against DB
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Gym Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with dark background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 15, 25),
                        0, getHeight(), new Color(30, 30, 50));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // Card panel
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(25, 28, 45));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(380, 460));

        // Logo / Icon label
        JLabel lblIcon = new JLabel("💪", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel lblTitle = new JLabel("GYM MANAGER", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(255, 87, 34));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Sign in to your account", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(150, 150, 170));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(200, 200, 220));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = createStyledTextField();
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(200, 200, 220));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        stylePasswordField(txtPassword);
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        btnLogin = new JButton("LOGIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(200, 60, 20));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(255, 100, 50));
                } else {
                    g2d.setColor(new Color(255, 87, 34));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        btnLogin.setPreferredSize(new Dimension(280, 45));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Status label
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(255, 80, 80));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hint label
        JLabel lblHint = new JLabel("Default: admin/admin123 | user/user123", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblHint.setForeground(new Color(100, 100, 120));
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to card
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(30));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(16));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(25));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(lblStatus);
        card.add(Box.createVerticalStrut(15));
        card.add(lblHint);

        mainPanel.add(card);
        setContentPane(mainPanel);

        // Action listeners
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(280, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBackground(new Color(38, 42, 65));
        field.setForeground(new Color(220, 220, 240));
        field.setCaretColor(new Color(255, 87, 34));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 65, 90), 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        return field;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setPreferredSize(new Dimension(280, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBackground(new Color(38, 42, 65));
        field.setForeground(new Color(220, 220, 240));
        field.setCaretColor(new Color(255, 87, 34));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 65, 90), 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
    }

    // Login logic with DB validation
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // UI-level validation
        if (username.isEmpty()) {
            lblStatus.setText("⚠ Username cannot be empty!");
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            lblStatus.setText("⚠ Password cannot be empty!");
            txtPassword.requestFocus();
            return;
        }

        // DB validation
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, username, role FROM users WHERE username=? AND password=?")) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("id");
                String uname = rs.getString("username");
                lblStatus.setForeground(new Color(80, 200, 120));
                lblStatus.setText("✓ Login successful! Welcome " + uname);

                // Open dashboard after brief delay
                Timer timer = new Timer(600, ev -> {
                    new DashboardFrame(uname, role).setVisible(true);
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();

            } else {
                lblStatus.setForeground(new Color(255, 80, 80));
                lblStatus.setText("✗ Invalid username or password!");
                txtPassword.setText("");
                txtPassword.requestFocus();
            }

        } catch (SQLException e) {
            lblStatus.setText("✗ DB Error: " + e.getMessage());
        }
    }
}
