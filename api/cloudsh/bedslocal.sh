#!/bin/sh

api="https://starbornag-vevkduzweq-uc.a.run.app/api/beds"

curl $api -X POST -H 'Content-Type: application/json' \
-d '{"bedId":"c0e75294-4b1e-4664-9037-3ca56f41ac5a","name":"Earth","dimensions":{"length":10,"width":5,"height":20}}' >/dev/null
curl $api -X POST -H 'Content-Type: application/json' \
-d '{"bedId":"2fbda883-d49d-4067-8e16-2b04cc523111","name":"Jupiter","dimensions":{"length":8,"width":4,"height":20}}' >/dev/null
