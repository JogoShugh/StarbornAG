#!/bin/sh

if [ "$1" = "-c" ] || [ "$1" = "--cloud" ]; then
    export API="https://starbornag-vevkduzweq-uc.a.run.app/api/beds"
else
    export API="http://localhost:8080/api/beds"
fi