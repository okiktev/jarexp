#!/bin/sh


BUILD_NUM=`cat "build_site.txt"`
BUILD_NUM=$((BUILD_NUM + 1))

printf $BUILD_NUM>build_site.txt
