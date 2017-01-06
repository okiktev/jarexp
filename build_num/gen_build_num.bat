@echo off

set /p BUILD_NUM=<build.txt
set /a BUILD_NUM=BUILD_NUM + 1

rem avoid new line
echo|set /p=%BUILD_NUM% 1>build.txt

exit 0