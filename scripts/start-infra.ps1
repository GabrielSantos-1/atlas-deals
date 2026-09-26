Write-Host "=== Starting Atlas Deals Infrastructure ===" -ForegroundColor Cyan

$rootDir = Split-Path -Parent $PSScriptRoot
$composeFile = "$rootDir\infra\docker\docker-compose.yml"

if (-not (Test-Path -LiteralPath $composeFile)) {
    Write-Host "docker-compose.yml not found at $composeFile" -ForegroundColor Red
    exit 1
}

Set-Location -LiteralPath "$rootDir\infra\docker"
docker compose -f docker-compose.yml up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start infrastructure containers." -ForegroundColor Red
    exit 1
}

Write-Host "Infrastructure services started. Use 'docker compose logs -f' to monitor." -ForegroundColor Green
Write-Host "  Postgres: 5432" -ForegroundColor Gray
Write-Host "  Redis:    6379" -ForegroundColor Gray
Write-Host "  RabbitMQ: 5672 (management: 15672)" -ForegroundColor Gray
Write-Host "  OpenSearch: 9200" -ForegroundColor Gray
Write-Host "  Prometheus: 9090" -ForegroundColor Gray
Write-Host "  Grafana:  3001" -ForegroundColor Gray
Write-Host "  Loki:     3100" -ForegroundColor Gray
Write-Host "  Tempo:    4317 (gRPC) / 4318 (HTTP)" -ForegroundColor Gray
Write-Host "  MinIO:    9000 (console: 9001)" -ForegroundColor Gray

Set-Location -LiteralPath $rootDir
