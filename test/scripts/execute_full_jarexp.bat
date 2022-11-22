@echo off

set CUR_DIR=%CD%

cd %JAREXP_DIR%

for /F "tokens=*" %%g in ('dir jar-explorer-*.jar /b') do (set JAREXP_FILE_NAME=%%g)

set FULL_JAREXP_DIR=%JAREXP_DIR%\..\fullpack\lib

echo Running '%JAREXP_FILE_NAME%' under Java '%JAVA_HOME%' from '%FULL_JAREXP_DIR%'

rem "%JAVA_HOME%\bin\java.exe" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=666 -jar "%JAREXP_DIR%\%JAREXP_FILE_NAME%"

"%JAVA_HOME%\bin\java.exe" -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED ^
	--add-exports=java.desktop/sun.swing.table=ALL-UNNAMED -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=666 ^
	-jar "%FULL_JAREXP_DIR%\%JAREXP_FILE_NAME%"

rem "%JAVA_HOME%\bin\java.exe" -jar "%JAREXP_DIR%\%JAREXP_FILE_NAME%"

cd %CUR_DIR%