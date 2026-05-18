@echo off
echo ========================================
echo    Gym Management System - Launcher
echo ========================================
echo.

if not exist "out" mkdir out

echo Compiling...
javac -cp "lib/sqlite-jdbc.jar" -d out src/gym/*.java

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Compilation failed!
    echo Make sure Java JDK is installed and lib/sqlite-jdbc.jar exists.
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Starting Gym Management System...
java -cp "out;lib/sqlite-jdbc.jar" gym.Main

pause
