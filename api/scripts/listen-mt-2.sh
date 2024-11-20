#!/bin/sh
clientId=2D93AF29-E0CC-4261-A3D7-BB2F6F22227A
curl -vvv -H "Accept: application/json" -N "$API/2fbda883-d49d-4067-8e16-2b04cc523111/events?clientId=$clientId"
