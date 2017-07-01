@echo off

set /p BUILD_NUM=<build_site.txt
set /a BUILD_NUM=BUILD_NUM + 1

rem avoid new line
echo|set /p=%BUILD_NUM% 1>build_site.txt

exit 0