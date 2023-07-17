@echo off

set JAREXP_FILE_NAME=Jar Explorer.exe
set CUR_DIR=%CD%
set EXE_JAREXP_DIR=%JAREXP_DIR%\..\


cd %JAREXP_DIR%


echo Running '%JAREXP_FILE_NAME%' under Java '%JAVA_HOME%' from '%EXE_JAREXP_DIR%'

"%EXE_JAREXP_DIR%%JAREXP_FILE_NAME%"


cd %CUR_DIR%