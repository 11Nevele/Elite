@echo off
REM Build script for Elite game - creates a runnable JAR
REM Run from project root: build.bat

echo === Building Elite Game JAR ===

REM Java path
set JAVA_HOME=C:\Program Files\Java\jdk-23
set JAVAC=%JAVA_HOME%\bin\javac.exe
set JAR=%JAVA_HOME%\bin\jar.exe

REM Step 1: Compile all Java files
echo Compiling Java sources...
%JAVAC% -d bin @sources.txt
if errorlevel 1 (
    echo Compilation failed!
    exit /b 1
)
echo Compilation successful!

REM Step 2: Create runnable JAR
echo Creating runnable JAR...
%JAR% cfe Elite.jar game.Game -C bin .
if errorlevel 1 (
    echo JAR creation failed!
    exit /b 1
)
echo JAR created: Elite.jar

REM Step 3: Verify it can be run
echo.
echo To run the game:
echo   java -jar Elite.jar
echo.
echo Remember: Keep Model/ and Sound/ folders next to Elite.jar
