#!/usr/bin/env bash
# Fills a demo bed with tomatoes, lettuce, eggplants and peppers, then records some care,
# through the same REST commands a client uses. Usage: scripts/populate-garden.sh [bedId] [baseUrl]
#
# The default bed is Earth (5 rows A-E, 10 columns). Rows are letters and columns numbers.
# Planting is allowed only into an empty cell, so running this twice answers 409 for the
# plantings already there and carries on.
set -u

bed="${1:-c0e75294-4b1e-4664-9037-3ca56f41ac5a}"
base="${2:-http://localhost:8080}"

send() { # action location extra-json-fields
  local now status
  now=$(date -u +%Y-%m-%dT%H:%M:%SZ)
  status=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$base/api/beds/$bed/$1" \
    -H 'Content-Type: application/json' \
    -d "{\"bedId\":\"$bed\",\"started\":\"$now\",\"location\":\"$2\"$3}")
  printf '%-9s %-12s %s\n' "$1" "$2" "$status"
}

plant() { send plant "$1" ",\"plantType\":\"$2\",\"plantCultivar\":\"$3\""; }

plant "A1 to A8"  "tomato"      "Dark Galaxy"
plant "B1 to B10" "lettuce"     "Paris Island"
plant "C1 to C5"  "eggplant"    "Black Beauty"
plant "C6 to C10" "bell pepper" "California Wonder"
plant "D1 to D6"  "hot pepper"  "Jalapeno"
plant "E1 to E3"  "chili pepper" "Thai"

send water     "A1 to B10" ',"volume":1.0'
send mulch     "A1 to A8"  ',"volume":2.0,"material":"straw"'
send fertilize "C1 to C10" ',"volume":0.5,"fertilizer":"fish emulsion"'
send water     "C1 to D6"  ',"volume":1.0'
