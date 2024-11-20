#!/bin/sh

basepath="api/beds"
cloud="https://starbornag-vevkduzweq-ue.a.run.app"
local="http://localhost:8080"

if [ "$1" = "-c" ] || [ "$1" = "--cloud" ]; then
    export API="$cloud/$basepath"
else
    export API="$local/$basepath"
fi
