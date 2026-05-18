package gym;

import javax.swing.*;

/**
 * Main.java - Entry Point for Gym Management System
 * 
 * MEHRAN UNIVERSITY OF ENGINEERING AND TECHNOLOGY
 * Course: SW121 - Object Oriented Programming
 * Project: Gym Management System
 * 
 * HOW TO RUN:
 * 1. Ensure sqlite-jdbc JAR is in classpath (lib/sqlite-jdbc.jar)
 * 2. Compile: javac -cp lib/sqlite-jdbc.jar -d out src/gym/*.java
 * 3. Run:     java -cp out:lib/sqlite-jdbc.jar gym.Main
 *    (Windows: java -cp "out;lib/sqlite-jdbc.jar" gym.Main)
 *
 * DEFAULT CREDENTIALS:
 *   Admin: admin / admin123
 *   User:  user  / user123
 */
public class Main {

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fallback to default
        }

        // Initialize database and create tables
        DatabaseHelper.initializeDatabase();

        // Launch login screen on EDT
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
