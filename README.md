# Gym Management System
## Mehran University of Engineering and Technology
### Course: SW121 – Object Oriented Programming | Batch K25SW

---

## 📋 Project Overview
A full-featured Gym Management System built with Java Swing and SQLite.

---

## ✅ Features Implemented

### Functional Requirements
| # | Feature | Status |
|---|---------|--------|
| 1 | Login/Authentication (Admin + User roles) | ✅ Done |
| 2 | CRUD on Members, Trainers, Equipment, Payments | ✅ Done |
| 3 | Search & Filter functionality | ✅ Done |
| 4 | Data Validation (UI + DB level) | ✅ Done |
| 5 | Reports Generation + TXT Export | ✅ Done |
| 6 | Attractive GUI with dark theme | ✅ Done |
| 7 | JDBC with SQLite database | ✅ Done |
| 8 | Modular Code + Comments | ✅ Done |

### Non-Functional Requirements
- Easy setup (SQLite = no server needed)
- Dark, responsive, attractive UI
- Proper error handling on all inputs

---

## 📁 Project Structure
```
GymManagementSystem/
├── src/
│   └── gym/
│       ├── Main.java           ← Entry point
│       ├── DatabaseHelper.java ← DB connection + table creation
│       ├── LoginFrame.java     ← Login screen
│       ├── DashboardFrame.java ← Main dashboard with sidebar
│       ├── HomePanel.java      ← Stats overview
│       ├── MembersPanel.java   ← Members CRUD
│       ├── TrainersPanel.java  ← Trainers CRUD
│       ├── EquipmentPanel.java ← Equipment CRUD
│       ├── PaymentsPanel.java  ← Payments CRUD
│       ├── ReportsPanel.java   ← Reports + Export
│       └── UsersPanel.java     ← User management (Admin only)
├── lib/
│   └── sqlite-jdbc.jar         ← Download from step 1 below
├── README.md
└── run.bat / run.sh
```

---

## 🚀 Setup & Run Instructions

### Step 1: Download SQLite JDBC Driver
Download `sqlite-jdbc-3.43.0.0.jar` from:
https://github.com/xerial/sqlite-jdbc/releases

Place it in the `lib/` folder and rename to `sqlite-jdbc.jar`

### Step 2: Compile
**Windows (Command Prompt):**
```
javac -cp "lib/sqlite-jdbc.jar" -d out src/gym/*.java
```

**Linux/Mac (Terminal):**
```
javac -cp lib/sqlite-jdbc.jar -d out src/gym/*.java
```

### Step 3: Run
**Windows:**
```
java -cp "out;lib/sqlite-jdbc.jar" gym.Main
```

**Linux/Mac:**
```
java -cp out:lib/sqlite-jdbc.jar gym.Main
```

---

## 🔐 Default Login Credentials
| Role  | Username | Password  |
|-------|----------|-----------|
| Admin | admin    | admin123  |
| User  | user     | user123   |

---

## 📌 Role-Based Access
| Module     | Admin | User |
|-----------|-------|------|
| Members   | CRUD  | View |
| Trainers  | CRUD  | View |
| Equipment | CRUD  | View |
| Payments  | CRUD  | View |
| Reports   | ✅    | ✅   |
| Users     | ✅    | ❌   |

---

## 💡 Using NetBeans IDE
1. Open NetBeans → File → New Project → Java Application
2. Copy all files from `src/gym/` into your project's source package
3. Right-click project → Properties → Libraries → Add JAR → select `sqlite-jdbc.jar`
4. Set Main Class to `gym.Main`
5. Run the project

---

## 🗄️ Database
- Database: SQLite (`gym.db` auto-created on first run)
- Tables: `users`, `members`, `trainers`, `equipment`, `payments`
- No installation needed — SQLite is embedded!
