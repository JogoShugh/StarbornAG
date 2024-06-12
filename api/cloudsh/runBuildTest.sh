#!/bin/sh

./gradlew clean build
docker build -t starborn .
docker run -p 8080:8080 starborn &
./alllocal.sh
