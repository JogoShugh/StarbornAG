#!/bin/sh

url="$API/2fbda883-d49d-4067-8e16-2b04cc523111/fertilize"
bedId="2fbda883-d49d-4067-8e16-2b04cc523111"
curl $url -X POST -H 'Content-Type: application/json' \
-d '{"bedId":"'$bedId'","started":"2024-05-29T13:19:26.806+00:00","volume":1.0,"fertilizer":"Vegan Mix 3-2-1","row":1}' | jq .rows[].cells[].lastWatering.type
