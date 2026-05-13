# Build script for Elite game - creates a runnable JAR
# Run from project root: .\build.ps1

Write-Host "=== Building Elite Game JAR ===" -ForegroundColor Green

# Java path
$javaHome = "C:\Program Files\Java\jdk-23"
$javac = "$javaHome\bin\javac.exe"
$jar = "$javaHome\bin\jar.exe"

# Step 1: Compile all Java files
Write-Host "Compiling Java sources..." -ForegroundColor Cyan
& $javac -d bin "@sources.txt"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Compilation successful!" -ForegroundColor Green

# Step 2: Create runnable JAR
Write-Host "Creating runnable JAR..." -ForegroundColor Cyan
& $jar cfe Elite.jar game.Game -C bin .
if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR creation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "JAR created: Elite.jar" -ForegroundColor Green

# Step 3: Verify it can be run
Write-Host ""
Write-Host "To run the game:" -ForegroundColor Yellow
Write-Host "  java -jar Elite.jar"
Write-Host ""
Write-Host "Remember: Keep Model/ and Sound/ folders next to Elite.jar" -ForegroundColor Yellow
