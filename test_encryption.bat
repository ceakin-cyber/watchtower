@echo off
echo Testing Database Encryption Implementation
echo ========================================

echo.
echo Building project...
gradlew.bat clean assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Running unit tests...
gradlew.bat test

if %ERRORLEVEL% NEQ 0 (
    echo Unit tests failed!
    pause
    exit /b 1
)

echo.
echo Running integration tests...
gradlew.bat connectedAndroidTest

if %ERRORLEVEL% NEQ 0 (
    echo Integration tests failed!
    pause
    exit /b 1
)

echo.
echo ======================================
echo All tests passed! Encryption is working correctly.
echo ======================================
pause