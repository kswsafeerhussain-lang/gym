#!/bin/bash
echo "========================================"
echo "   Gym Management System - Launcher"
echo "========================================"
echo ""

mkdir -p out

echo "Compiling..."
javac -cp "lib/sqlite-jdbc.jar" -d out src/gym/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed!"
    echo "Make sure Java JDK is installed and lib/sqlite-jdbc.jar exists."
    exit 1
fi

echo "Compilation successful!"
echo ""
echo "Starting Gym Management System..."
java -cp "out:lib/sqlite-jdbc.jar" gym.Main
