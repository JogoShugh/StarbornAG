#!/bin/sh

./gradlew clean build -x test
docker build -t starborn .
docker run --network starbornag-network -p 8080:8080 starborn
