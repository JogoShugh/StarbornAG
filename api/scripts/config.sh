#!/bin/sh

path="api/beds"
cloud="https://starbornag-vevkduzweq-ue.a.run.app"
local="http://localhost:8080"

if [ "$1" = "-c" ] || [ "$1" = "--cloud" ]; then
    export API="$cloud/$path"
else
    export API="$local/$path"
fi

# /Users/josh/Downloads/google-cloud-sdk/bin:/Users/josh/Downloads/google-cloud-sdk/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/System/Cryptexes/App/usr/bin:/usr/bin:/bin:/usr/sbin:/sbin:/var/run/com.apple.security.cryptexd/codex.system/bootstrap/usr/local/bin:/var/run/com.apple.security.cryptexd/codex.system/bootstrap/usr/bin:/var/run/com.apple.security.cryptexd/codex.system/bootstrap/usr/appleinternal/bin
