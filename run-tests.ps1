# Script para ejecutar pruebas unitarias - GamerZone App
# PowerShell Script

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  PRUEBAS UNITARIAS GAMERZONE  " -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Menú de opciones
Write-Host "Seleccione una opción:" -ForegroundColor Yellow
Write-Host "1. Ejecutar TODAS las pruebas"
Write-Host "2. Ejecutar pruebas de ViewModels"
Write-Host "3. Ejecutar pruebas de Repositories"
Write-Host "4. Ejecutar pruebas de Models"
Write-Host "5. Ejecutar con reporte de cobertura"
Write-Host "6. Limpiar y ejecutar todas las pruebas"
Write-Host "7. Salir"
Write-Host ""

$option = Read-Host "Ingrese el número de opción"

switch ($option) {
    "1" {
        Write-Host "`nEjecutando TODAS las pruebas..." -ForegroundColor Green
        .\gradlew test
    }
    "2" {
        Write-Host "`nEjecutando pruebas de ViewModels..." -ForegroundColor Green
        .\gradlew test --tests "*.viewmodel.*"
    }
    "3" {
        Write-Host "`nEjecutando pruebas de Repositories..." -ForegroundColor Green
        .\gradlew test --tests "*.repository.*"
    }
    "4" {
        Write-Host "`nEjecutando pruebas de Models..." -ForegroundColor Green
        .\gradlew test --tests "*.model.*"
    }
    "5" {
        Write-Host "`nEjecutando pruebas con reporte de cobertura..." -ForegroundColor Green
        .\gradlew test jacocoTestReport
        Write-Host "`nReporte generado en: app/build/reports/jacoco/test/html/index.html" -ForegroundColor Cyan
    }
    "6" {
        Write-Host "`nLimpiando proyecto..." -ForegroundColor Green
        .\gradlew clean
        Write-Host "`nEjecutando todas las pruebas..." -ForegroundColor Green
        .\gradlew test
    }
    "7" {
        Write-Host "`nSaliendo..." -ForegroundColor Yellow
        exit
    }
    default {
        Write-Host "`nOpción inválida" -ForegroundColor Red
    }
}

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "  PROCESO COMPLETADO           " -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

