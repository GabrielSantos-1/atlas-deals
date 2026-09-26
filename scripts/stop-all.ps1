Write-Host "=== Stopping Atlas Deals - All Services ===" -ForegroundColor Cyan

$rootDir = Split-Path -Parent $PSScriptRoot

Write-Host "[1/3] Stopping Docker Compose services..." -ForegroundColor Yellow
$fullCompose = "$rootDir\infra\docker\docker-compose.full.yml"
$infraCompose = "$rootDir\infra\docker\docker-compose.yml"

if (Test-Path -LiteralPath $fullCompose) {
    Set-Location -LiteralPath "$rootDir\infra\docker"
    docker compose -f docker-compose.full.yml down
}
elseif (Test-Path -LiteralPath $infraCompose) {
    Set-Location -LiteralPath "$rootDir\infra\docker"
    docker compose -f docker-compose.yml down
}

Write-Host "[2/3] Stopping any remaining Docker containers..." -ForegroundColor Yellow
$containers = docker ps -q --filter "name=atlas-"
if ($containers) {
    docker stop $containers
    docker rm $containers
}

Write-Host "[3/3] Stopping Java processes..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "=== All services stopped ===" -ForegroundColor Cyan
