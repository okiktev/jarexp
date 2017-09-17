@ECHO OFF
REM BFCPEOPTIONSTART
REM Advanced BAT to EXE Converter www.BatToExeConverter.com
REM BFCPEICON=@ICOFILE@
REM BFCPEICONINDEX=-1
REM BFCPEEMBEDDISPLAY=0
REM BFCPEEMBEDDELETE=1
REM BFCPEADMINEXE=0
REM BFCPEINVISEXE=1
REM BFCPEVERINCLUDE=0
REM BFCPEVERVERSION=1.0.0.0
REM BFCPEVERPRODUCT=Product Name
REM BFCPEVERDESC=Product Description
REM BFCPEVERCOMPANY=Your Company
REM BFCPEVERCOPYRIGHT=Copyright Info
REM BFCPEOPTIONEND
@ECHO ON
rem HideSelf
@echo off
set LOGFILE=%TEMP%\jarexp-console.txt
set URL=file:///%LOGFILE:\=/%
echo %date% %time% Starting... >> %LOGFILE%
call java -jar "%~dp0\lib\@FILENAME@" "%~1" >> %LOGFILE% 2>&1
if %errorlevel% NEQ 0 (
echo Error code: %errorlevel% >> %LOGFILE%
mshta "javascript:var sh=new ActiveXObject('WScript.Shell');btn=sh.Popup('Jar Explorer unable to start\nError code: %errorlevel%\nDo you want to see details?',0,'Error',0x4+0x10);switch(btn){case 6:sh.Run('%URL%');break};close()"
)
echo %date% %time% Exit. >> %LOGFILE%
set URL=
set LOGFILE=