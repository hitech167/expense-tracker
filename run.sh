#!/bin/sh
# Builds and starts Expense Tracker:  ./run.sh
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -encoding UTF-8 -d out src/expensetracker/*.java || { echo "Build failed. Install a JDK (Java 11 or newer)."; exit 1; }
java -cp out expensetracker.Main
