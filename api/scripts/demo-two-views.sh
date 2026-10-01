#!/usr/bin/env bash
# One address, two views: walks through preparing a bed, planting, moving around and watering,
# asking every address twice, once as an agent (HAL Schema Forms) and once as a browser (HTML and
# htmx partials). Prints each curl's full output, headers included, one after another.
#
# Usage: scripts/demo-two-views.sh [baseUrl]      (default http://localhost:8080)
#
# Each run prepares a new bed, so it can be run again and again.
set -u

base="${1:-http://localhost:8080}"
agent='Accept: application/hal+json'
browser='Accept: text/html'
bed=$(uuidgen | tr 'A-Z' 'a-z')
name="Demo $(date +%H:%M:%S)"

step() { printf '\n\n==================== %s\n\n' "$*"; }
show() { printf -- '$ curl %s\n\n' "$*"; curl -s -i "$@"; echo; }

step "1. The front door, as an agent: a link to the beds"
show -H "$agent" "$base/"

step "1. The front door, as a browser: the list of beds"
show -H "$browser" "$base/"

step "2. The beds, as an agent: every bed and the form to prepare a new one"
show -H "$agent" "$base/beds"

step "3. The agent fills the prepare-bed form: $name, 4 rows by 6 columns"
show -X POST -H 'Content-Type: application/json' -H "$agent" "$base/api/beds" \
  -d "{\"bedId\":\"$bed\",\"name\":\"$name\",\"dimensions\":{\"rows\":4,\"columns\":6},\"cellBlockSize\":1}"

step "4. The new bed, as an agent: no moves yet, a link to the journal, a form per possible care"
show -H "$agent" "$base/beds/$bed"

step "4. The same address, as a browser: the whole bed page"
show -H "$browser" "$base/beds/$bed"

step "5. The agent steps onto cell B2: eight moves, zoom-out to Row B, and plant is possible"
show -H "$agent" "$base/beds/$bed/focus/cell/B2"

step "6. The agent plants a tomato at B2 by posting JSON to B2's own plant address"
show -X POST -H 'Content-Type: application/json' -H "$agent" "$base/beds/$bed/focus/cell/B2/plant" \
  -d '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'

step "7. The browser taps B3 (htmx): only the view comes back"
show -H "$browser" -H 'HX-Request: true' "$base/beds/$bed/focus/cell/B3"

step "8. The browser plants lettuce at B3 from the sheet: the same kind of address, form fields in"
show -X POST -H 'HX-Request: true' "$base/beds/$bed/focus/cell/B3/plant" \
  --data-urlencode 'plantType=lettuce' --data-urlencode 'plantCultivar=Paris Island'

step "9. The agent follows B2's zoom-out link to Row B"
zoom_out=$(curl -s -H "$agent" "$base/beds/$bed/focus/cell/B2" | jq -r '._links["zoom-out"].href')
show -H "$agent" "$base$zoom_out"

step "10. The agent waters all of Row B through the row's water form"
show -X POST -H 'Content-Type: application/json' -H "$agent" "$base/beds/$bed/focus/row/B/water" \
  -d '{"volume":1.0}'

step "11. Row B as a browser sees it (htmx): the sheet's buttons post where the agent's forms do"
show -H "$browser" -H 'HX-Request: true' "$base/beds/$bed/focus/row/B"

step "12. The agent tries to plant at B2 again: the soil rules refuse, 409"
show -X POST -H 'Content-Type: application/json' -H "$agent" "$base/beds/$bed/focus/cell/B2/plant" \
  -d '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'

step "13. The journal by row, as an agent: links to each way of reading it, no page-only state"
show -H "$agent" "$base/beds/$bed/journal?by=row"

step "13. The same journal, as a browser: the page with the journal open full"
show -H "$browser" "$base/beds/$bed/journal?by=row&size=full"
