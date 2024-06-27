#!/bin/sh

bed=c0e75294-4b1e-4664-9037-3ca56f41ac5a
url="$API/$bed"
curl $url | jq .rows[].cells[].lastWatering.type