#!/bin/sh

url="$API/2fbda883-d49d-4067-8e16-2b04cc523111/plant"
bedId="2fbda883-d49d-4067-8e16-2b04cc523111"
curl $url -X POST -H 'Content-Type: application/json' \
-d '{"bedId":"'$bedId'","rowPosition":"2","cellPositionInRow":1,"started":"2024-05-29T13:19:26.806+00:00","plantType":"lettuce","plantCultivar":"Paris Island"}' | jq .rows[].cells[].lastWatering.type

🥬