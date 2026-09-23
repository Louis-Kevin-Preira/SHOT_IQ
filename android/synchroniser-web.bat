@echo off
REM Recopie l'application web dans les ressources Android (Windows).
set DEST=app\src\main\assets\www
if exist "%DEST%" rmdir /s /q "%DEST%"
mkdir "%DEST%"
copy ..\index.html "%DEST%" >nul
copy ..\sw.js "%DEST%" >nul
copy ..\manifest.webmanifest "%DEST%" >nul
xcopy ..\icons "%DEST%\icons" /e /i /q >nul
xcopy ..\media "%DEST%\media" /e /i /q >nul
echo Application web copiee dans %DEST%
