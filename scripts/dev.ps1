param(
    [switch]$NoInfra,
    [switch]$NoFrontend,
    [string[]]$Services = @()
)

$rootDir = Split-Path -Parent $PSScriptRoot
$backendDir = "$rootDir\backend"

if (-not $NoInfra) {
    Write-Host "=== Starting Infrastructure ===" -ForegroundColor Cyan
    & "$PSScriptRoot\start-infra.ps1"
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

if (-not $NoFrontend) {
    Write-Host "=== Starting Frontend (dev mode) ===" -ForegroundColor Cyan
    $frontendJob = Start-Job -ScriptBlock {
        param($dir)
        Set-Location -LiteralPath $dir
        npm run dev
    } -ArgumentList "$rootDir\frontend"
    Write-Host "Frontend dev server starting on http://localhost:3000" -ForegroundColor Green
}

$allServices = @(
    "atlas-deals-common",
    "atlas-deals-gateway",
    "atlas-deals-collector",
    "atlas-deals-price-history",
    "atlas-deals-promotion-engine",
    "atlas-deals-affiliate-engine",
    "atlas-deals-ai-engine",
    "atlas-deals-publisher",
    "atlas-deals-analytics",
    "atlas-deals-notification",
    "atlas-deals-scheduler",
    "atlas-deals-auth",
    "atlas-deals-search",
    "atlas-deals-user",
    "atlas-deals-recommendation"
)

if ($Services.Count -gt 0) {
    $modulesToStart = $Services
}
else {
    $modulesToStart = $allServices | Where-Object { $_ -ne "atlas-deals-common" }
}

Write-Host "=== Starting Backend Services (dev mode) ===" -ForegroundColor Cyan

$javaJobs = @()
foreach ($module in $modulesToStart) {
    $modulePath = "$backendDir\$module"
    if (-not (Test-Path -LiteralPath $modulePath)) {
        Write-Host "Module not found: $module" -ForegroundColor Yellow
        continue
    }

    Write-Host "  Starting $module..." -ForegroundColor Yellow
    $job = Start-Job -ScriptBlock {
        param($dir, $mod)
        Set-Location -LiteralPath $dir
        mvn spring-boot:run -pl "backend/$mod" -am
    } -ArgumentList $rootDir, $module
    $javaJobs += $job
}

Write-Host ""
Write-Host "=== Development environment started ===" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop monitoring. Run stop-all.ps1 to shut everything down." -ForegroundColor Gray
Write-Host ""
Write-Host "Active jobs:" -ForegroundColor Gray
Get-Job | Where-Object { $_.State -eq "Running" } | ForEach-Object {
    Write-Host "  - $($_.Id): $($_.Command)" -ForegroundColor Gray
}

try {
    while ($true) {
        $running = Get-Job | Where-Object { $_.State -eq "Running" }
        if ($running.Count -eq 0) {
            Write-Host "All jobs have completed." -ForegroundColor Yellow
            break
        }
        Start-Sleep -Seconds 5
    }
}
finally {
    Write-Host "Cleaning up background jobs..." -ForegroundColor Yellow
    Get-Job | Stop-Job -PassThru | Remove-Job
}
