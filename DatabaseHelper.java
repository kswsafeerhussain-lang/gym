package gym;

import java.sql.*;

/**
 * DatabaseHelper - Handles all SQLite DB connections and table creation
 * Single Responsibility: DB setup and connection management
 */
public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:gym.db";
    private static Connection connection = null;

    // Returns singleton connection
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    // Creates all tables if they don't exist
    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Users table (Login/Auth)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('Admin','User')),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Members table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS members (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE,
                    phone TEXT NOT NULL,
                    address TEXT,
                    membership_type TEXT NOT NULL CHECK(membership_type IN ('Basic','Standard','Premium')),
                    join_date DATE NOT NULL,
                    expiry_date DATE NOT NULL,
                    status TEXT DEFAULT 'Active' CHECK(status IN ('Active','Inactive')),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Trainers table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS trainers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE,
                    phone TEXT NOT NULL,
                    specialization TEXT NOT NULL,
                    salary REAL NOT NULL,
                    hire_date DATE NOT NULL,
                    status TEXT DEFAULT 'Active' CHECK(status IN ('Active','Inactive')),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Equipment table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS equipment (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    quantity INTEGER NOT NULL DEFAULT 1,
                    condition_status TEXT DEFAULT 'Good' CHECK(condition_status IN ('Good','Fair','Poor','Under Repair')),
                    purchase_date DATE,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Payments table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    payment_date DATE NOT NULL,
                    payment_method TEXT NOT NULL CHECK(payment_method IN ('Cash','Card','Online')),
                    description TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(member_id) REFERENCES members(id)
                )
            """);

            // Insert default admin if not exists
            stmt.executeUpdate("""
                INSERT OR IGNORE INTO users (username, password, role)
                VALUES ('admin', 'admin123', 'Admin')
            """);

            stmt.executeUpdate("""
                INSERT OR IGNORE INTO users (username, password, role)
                VALUES ('user', 'user123', 'User')
            """);

            System.out.println("✅ Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("❌ DB Init Error: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
