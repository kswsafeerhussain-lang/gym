package gym;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * DashboardFrame - Main navigation hub
 * Role-based access: Admin sees all, User sees limited panels
 */
public class DashboardFrame extends JFrame {

    private String currentUser;
    private String currentRole;
    private JPanel contentPanel;
    private JLabel lblWelcome;

    public DashboardFrame(String username, String role) {
        this.currentUser = username;
        this.currentRole = role;
        initComponents();
    }

    private void initComponents() {
        setTitle("Gym Management System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        // Root layout
        setLayout(new BorderLayout());

        // Top Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(18, 20, 32));
        add(contentPanel, BorderLayout.CENTER);

        // Show home/dashboard by default
        showHome();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 22, 38),
                        getWidth(), 0, new Color(35, 38, 60));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 65));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 87, 34)));

        JLabel lblTitle = new JLabel("  💪 GYM MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(255, 87, 34));

        lblWelcome = new JLabel("👤 " + currentUser + "  |  " + currentRole + "   ");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblWelcome.setForeground(new Color(180, 180, 200));

        JButton btnLogout = createHeaderButton("🚪 Logout");
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(lblWelcome);
        rightPanel.add(btnLogout);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 87, 34));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 22, 38),
                        0, getHeight(), new Color(15, 17, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(40, 44, 68)));

        sidebar.add(Box.createVerticalStrut(20));
        addSidebarButton(sidebar, "🏠  Dashboard", () -> showHome());
        addSidebarButton(sidebar, "👥  Members", () -> showPanel(new MembersPanel(currentRole)));
        addSidebarButton(sidebar, "🏋  Trainers", () -> showPanel(new TrainersPanel(currentRole)));
        addSidebarButton(sidebar, "🔧  Equipment", () -> showPanel(new EquipmentPanel(currentRole)));
        addSidebarButton(sidebar, "💳  Payments", () -> showPanel(new PaymentsPanel(currentRole)));
        addSidebarButton(sidebar, "📊  Reports", () -> showPanel(new ReportsPanel()));

        // Admin only
        if ("Admin".equals(currentRole)) {
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(50, 54, 80));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(sep);
            sidebar.add(Box.createVerticalStrut(10));
            addSidebarButton(sidebar, "⚙  Manage Users", () -> showPanel(new UsersPanel()));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addSidebarButton(JPanel sidebar, String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2d.setColor(new Color(255, 87, 34, 40));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(new Color(255, 87, 34));
                    g2d.fillRect(0, 0, 3, getHeight());
                } else {
                    g2d.setColor(new Color(0, 0, 0, 0));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                g2d.setColor(getModel().isRollover() ? new Color(255, 87, 34) : new Color(190, 190, 210));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawString(getText(), 20, getHeight() / 2 + 5);
            }
        };
        btn.setPreferredSize(new Dimension(210, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        sidebar.add(btn);
    }

    private void showHome() {
        contentPanel.removeAll();
        contentPanel.add(new HomePanel(currentUser, currentRole), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
