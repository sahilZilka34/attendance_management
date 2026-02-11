@echo off
echo Loading environment variables...

REM Load from .env file
for /f "tokens=1,2 delims==" %%a in (.env) do (
    set %%a=%%b
)

echo Starting backend...
mvn spring-boot:run
pause