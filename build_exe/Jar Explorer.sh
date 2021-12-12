#!/bin/sh
java -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED --add-exports=java.desktop/sun.swing.table=ALL-UNNAMED -jar "./lib/@JARNAME@" "$1"