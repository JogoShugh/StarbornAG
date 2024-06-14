#!/bin/sh

api="http://localhost:8080/api/beds/2fbda883-d49d-4067-8e16-2b04cc523111/water"
bedId="2fbda883-d49d-4067-8e16-2b04cc523111"
curl $api -X POST -H 'Content-Type: application/json' \
-d '{"bedId":"'$bedId'","started":"2024-05-29T13:19:26.806+00:00","volume":2.0}' | jq .rows[].cells[].lastWatering.type

