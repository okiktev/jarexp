#!/bin/sh


BUILD_NUM=`cat "build.txt"`
BUILD_NUM=$((BUILD_NUM + 1))

printf $BUILD_NUM>build.txt
