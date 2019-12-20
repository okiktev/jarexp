#!/bin/sh
java -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED -jar "./lib/@JARNAME@" "$1"