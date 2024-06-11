#!/bin/sh

api="http://localhost:8080/api/beds/2fbda883-d49d-4067-8e16-2b04cc523111"
curl $api | jq .rows[].cells[].lastWatering.type



