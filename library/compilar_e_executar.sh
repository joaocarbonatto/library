#!/bin/sh
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/biblioteca/*.java && java -cp out biblioteca.Principal
