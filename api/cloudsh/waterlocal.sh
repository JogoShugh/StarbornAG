#!/bin/sh

api="https://starbornag-vevkduzweq-uc.a.run.app/api/beds/2fbda883-d49d-4067-8e16-2b04cc523111/water"
curl $api -X POST -H 'Content-Type: application/json' \
-d '{"bedId":"c0e75294-4b1e-4664-9037-3ca56f41ac5a","started":"2024-05-29T13:19:26.806+00:00","volume":2.0}' | jq .rows[].cells[].lastWatering.type

