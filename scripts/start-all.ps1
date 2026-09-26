Write-Host "=== Starting Atlas Deals - All Services ===" -ForegroundColor Cyan

$rootDir = Split-Path -Parent $PSScriptRoot
$composeFile = "$rootDir\infra\docker\docker-compose.full.yml"

Set-Location -LiteralPath "$rootDir\infra\docker"

Write-Host "[1/1] Starting all services with Docker Compose..." -ForegroundColor Yellow
docker compose -f docker-compose.full.yml up -d --build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker Compose failed to start." -ForegroundColor Red
    exit 1
}

Write-Host "All services started." -ForegroundColor Green
Write-Host ""
Write-Host "Monitor with: docker compose -f infra\docker\docker-compose.full.yml logs -f" -ForegroundColor Gray
Write-Host "Stop all with: docker compose -f infra\docker\docker-compose.full.yml down" -ForegroundColor Gray

Set-Location -LiteralPath $rootDir
