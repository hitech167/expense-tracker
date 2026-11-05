@echo off
rem Builds and starts Expense Tracker. Double-click this file.
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\expensetracker\*.java
if errorlevel 1 (
  echo.
  echo Build failed. Please install a JDK ^(Java 11 or newer^) and try again.
  pause
  exit /b 1
)
java -cp out expensetracker.Main
