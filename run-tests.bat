@echo off
REM Script para ejecutar pruebas unitarias - GamerZone App
REM Windows Batch Script

echo ========================================
echo   PRUEBAS UNITARIAS GAMERZONE
echo ========================================
echo.

echo [INFO] Limpiando proyecto...
call gradlew.bat clean
echo.

echo [INFO] Sincronizando dependencias...
call gradlew.bat --refresh-dependencies
echo.

echo [INFO] Compilando proyecto...
call gradlew.bat compileDebugKotlin
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Falló la compilación
    pause
    exit /b %ERRORLEVEL%
)
echo.

echo [INFO] Ejecutando pruebas unitarias...
call gradlew.bat test --info
echo.

echo ========================================
echo   PROCESO COMPLETADO
echo ========================================
echo.

echo [INFO] Para ver el reporte de pruebas, abre:
echo app\build\reports\tests\testDebugUnitTest\index.html
echo.

pause

