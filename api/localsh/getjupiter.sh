#!/bin/sh

bed=2fbda883-d49d-4067-8e16-2b04cc523111
url="$API/$bed"
curl $url | jq .rows[].cells[].lastWatering.type

