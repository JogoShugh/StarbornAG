#!/usr/bin/env bash
# Records the two-views demo, renders the HTML report and opens it, in one go.
#
# Usage: scripts/demo-report.sh [baseUrl]      (default http://localhost:8080)
# The app must be running at baseUrl. Output: build/demo/two-views.yaml and two-views.html under api/.
set -euo pipefail

here=$(cd "$(dirname "$0")" && pwd)
base="${1:-http://localhost:8080}"
yaml="$(cd "$here/.." && pwd)/build/demo/two-views.yaml"

if ! curl -sf -o /dev/null -H 'Accept: application/hal+json' "$base/"; then
  echo "The app is not answering at $base. Start it first, then run this again." >&2
  exit 1
fi

"$here/demo-two-views.sh" "$base" "$yaml"
"$here/render-demo-report.py" "$yaml" "${yaml%.yaml}.html"

if command -v open >/dev/null; then open "${yaml%.yaml}.html"
elif command -v xdg-open >/dev/null; then xdg-open "${yaml%.yaml}.html"
fi
