#!/bin/sh

bed=2fbda883-d49d-4067-8e16-2b04cc523111
url="http://localhost:8080/api/beds/$bed/action"
curl -vvv -X POST $url -H "Content-Type: application/x-www-form-urlencoded" -d "prompt=$1"

