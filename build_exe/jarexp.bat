@echo off
set LOGFILE=%TEMP%\jarexp-console.txt
set URL=file:///%LOGFILE:\=/%
echo %date% %time% Starting... >> %LOGFILE%
call java -jar ".\lib\@FILENAME@" "%~1" >> %LOGFILE% 2>&1
if %errorlevel% NEQ 0 (
echo Error code: %errorlevel% >> %LOGFILE%
mshta "javascript:var sh=new ActiveXObject('WScript.Shell');btn=sh.Popup('Jar Explorer unable to start\nError code: %errorlevel%\nDo you want to see details?',0,'Error',0x4+0x10);switch(btn){case 6:sh.Run('%URL%');break};close()"
)
echo %date% %time% Exit. >> %LOGFILE%
set URL=
set LOGFILE=