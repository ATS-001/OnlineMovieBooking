# compile.ps1  — Run from the project root
# Usage:  .\compile.ps1

$JAR = "lib\mysql-connector-j-26.7.0.jar"

if (!(Test-Path $JAR)) {
    Write-Error "MySQL connector JAR not found at $JAR. Please check lib\ directory."
    exit 1
}

if (Test-Path "bin") { Remove-Item -Recurse -Force "bin" }
New-Item -ItemType Directory -Path "bin" | Out-Null

$files = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | `
         Select-Object -ExpandProperty FullName

Write-Host "Compiling $($files.Count) source files..."
javac -encoding UTF-8 -cp $JAR -sourcepath "src" -d "bin" $files

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "BUILD SUCCESSFUL  ->  bin/"
    Write-Host "To run: .\run.ps1"
} else {
    Write-Host "BUILD FAILED. Fix the errors above and try again."
}
