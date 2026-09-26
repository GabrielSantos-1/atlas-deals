Write-Host "=== Building Atlas Deals - All Services ===" -ForegroundColor Cyan

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $rootDir

Write-Host "[1/2] Building backend modules with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Backend build complete." -ForegroundColor Green

Write-Host "[2/2] Building frontend..." -ForegroundColor Yellow
Set-Location -LiteralPath "$rootDir\frontend"
if (-not (Test-Path -LiteralPath "node_modules")) {
    npm ci
    if ($LASTEXITCODE -ne 0) {
        Write-Host "npm ci failed!" -ForegroundColor Red
        exit 1
    }
}
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Frontend build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Frontend build complete." -ForegroundColor Green

Set-Location -LiteralPath $rootDir
Write-Host "=== All builds completed successfully ===" -ForegroundColor Cyan
