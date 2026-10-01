#!/usr/bin/env bash
# One address, two views: walks through preparing a bed, planting, moving around and watering,
# asking each address as an agent (HAL Schema Forms) and as a browser (HTML and htmx partials),
# and records every exchange in full (request, status, headers, body) to a YAML file.
#
# Usage: scripts/demo-two-views.sh [baseUrl] [out.yaml]
#        (defaults: http://localhost:8080 and build/demo/two-views.yaml under api/)
# Then:  scripts/render-demo-report.py      to turn the YAML into an HTML report.
#
# Needs curl and python3; with Google Chrome installed it also takes a phone-size screenshot
# of the HTML view at each step's address, as it looks at that moment, into screenshots/ beside the
# YAML. Each run prepares a new bed, so it can be run again and again.
set -u

here=$(cd "$(dirname "$0")" && pwd)
base="${1:-http://localhost:8080}"
out="${2:-$(cd "$here/.." && pwd)/build/demo/two-views.yaml}"
mkdir -p "$(dirname "$out")"
agent='application/hal+json'
browser='text/html'
bed=""
name="Demo $(date +%H:%M:%S)"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
: > "$work/exchanges.jsonl"

title=""; note=""; number=0
step() { title=$1; note=${2:-}; number=$((number + 1)); printf '%s\n' "$title" >&2; }

chrome="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
command -v google-chrome >/dev/null && chrome=$(command -v google-chrome)
shots="$(dirname "$out")/screenshots"
rm -rf "$shots"; mkdir -p "$shots"
: > "$work/shots.jsonl"

# shoot PATH: what a person sees at PATH right now, as the step's screenshot (skipped without Chrome).
shoot() {
  [ -x "$chrome" ] || return 0
  local file; file=$(printf 'step-%02d' "$number")
  # The page keeps an event stream open, so Chrome never finishes on its own: stop it once the
  # screenshot is written (or after 30 seconds).
  "$chrome" --headless=new --disable-gpu --hide-scrollbars --timeout=4000 --window-size=500,900 \
    --user-data-dir="$work/chrome" --screenshot="$work/$file.png" "$base$1" >/dev/null 2>&1 &
  local pid=$! waited=0
  while [ ! -s "$work/$file.png" ] && [ $waited -lt 300 ] && kill -0 $pid 2>/dev/null; do
    sleep 0.1; waited=$((waited + 1))
  done
  sleep 0.3; kill $pid 2>/dev/null; wait $pid 2>/dev/null
  [ -f "$work/$file.png" ] || return 0
  sips -s format jpeg -s formatOptions 82 "$work/$file.png" --out "$shots/$file.jpg" >/dev/null 2>&1 ||
    cp "$work/$file.png" "$shots/$file.png"
  local saved; saved=$(ls "$shots/$file".* | head -1)
  python3 -c 'import json,sys; print(json.dumps({"step": sys.argv[1], "screenshot": "screenshots/" + sys.argv[2], "of": sys.argv[3]}))' \
    "$title" "$(basename "$saved")" "$1" >> "$work/shots.jsonl"
}

# ask WHO METHOD PATH [-H header]... [--json BODY | --form name=value...]
# WHO is "agent" or "browser"; an htmx request is a browser with -H 'HX-Request: true'.
ask() {
  local who=$1 method=$2 path=$3; shift 3
  local accept=$agent; [ "$who" = browser ] && accept=$browser
  local -a args=(-H "Accept: $accept")
  local -a sent=("Accept: $accept")
  local body="" kind=""
  while [ $# -gt 0 ]; do
    case $1 in
      -H) args+=(-H "$2"); sent+=("$2"); shift 2 ;;
      --json) body=$2; kind=json; args+=(-H 'Content-Type: application/json' --data "$2")
              sent+=('Content-Type: application/json'); shift 2 ;;
      --form) kind=form; args+=(--data-urlencode "$2")
              body="${body:+$body&}$2"; shift 2 ;;
    esac
  done
  [ "$kind" = form ] && sent+=('Content-Type: application/x-www-form-urlencoded')
  curl -s -X "$method" -D "$work/headers" -o "$work/body" "${args[@]}" "$base$path"
  printf '%s\n' "${sent[@]}" > "$work/sent"
  printf '%s' "$body" > "$work/sent-body"
  python3 "$work/record.py" "$title" "$note" "$who" "$method" "$path" "$work/sent" "$work/sent-body" \
    "$work/headers" "$work/body" >> "$work/exchanges.jsonl"
}

# One JSON line per exchange; the YAML is written from all of them at the end.
cat > "$work/record.py" <<'PY'
import json, sys
title, note, who, method, path, sent, sent_body, headers, body = sys.argv[1:10]
def header_map(lines):
    found = {}
    for line in lines:
        if ":" in line:
            name, value = line.split(":", 1)
            found[name.strip()] = (found[name.strip()] + ", " if name.strip() in found else "") + value.strip()
    return found
raw = open(headers, encoding="utf-8").read().replace("\r", "").strip().split("\n")
status_line = raw[0].split(" ", 2)
response_headers = header_map(raw[1:])
text = open(body, encoding="utf-8", errors="replace").read().replace("\r\n", "\n")
if "json" in response_headers.get("Content-Type", ""):
    try: text = json.dumps(json.loads(text), indent=2, ensure_ascii=False)
    except ValueError: pass
request_body = open(sent_body, encoding="utf-8").read()
if request_body.startswith("{"):
    request_body = json.dumps(json.loads(request_body), indent=2, ensure_ascii=False)
print(json.dumps({
    "step": title, "note": note, "who": who,
    "request": {"method": method, "path": path,
                "headers": header_map(open(sent, encoding="utf-8").read().splitlines()),
                "body": request_body or None},
    "response": {"status": int(status_line[1]), "headers": response_headers, "body": text},
}, ensure_ascii=False))
PY

# Writes the YAML by hand (no PyYAML needed): bodies as literal blocks, everything else quoted.
cat > "$work/to_yaml.py" <<'PY'
import json, sys
meta = json.loads(sys.argv[1])
steps = []
for line in open(sys.argv[2], encoding="utf-8"):
    exchange = json.loads(line)
    title, note = exchange.pop("step"), exchange.pop("note")
    if not steps or steps[-1]["title"] != title:
        steps.append({"title": title, "note": note, "exchanges": []})
    steps[-1]["exchanges"].append(exchange)
for line in open(sys.argv[3], encoding="utf-8"):
    shot = json.loads(line)
    for step in steps:
        if step["title"] == shot["step"]:
            step["screenshot"] = {"file": shot["screenshot"], "of": shot["of"]}

def scalar(value, indent):
    if value is None: return "null"
    if isinstance(value, bool): return "true" if value else "false"
    if isinstance(value, (int, float)): return str(value)
    if "\n" in value or len(value) > 100:
        pad = " " * (indent + 2)
        # An explicit indentation indicator, so a body that starts indented (or blank) still parses.
        return "|2-\n" + "\n".join(pad + l if l else "" for l in value.split("\n"))
    return json.dumps(value, ensure_ascii=False)

def emit(value, indent=0):
    pad = " " * indent
    out = []
    if isinstance(value, dict):
        for key, item in value.items():
            k = json.dumps(key) if not key.replace("-", "").replace("_", "").isalnum() else key
            if isinstance(item, (dict, list)) and item:
                out.append(f"{pad}{k}:"); out.extend(emit(item, indent + 2))
            elif isinstance(item, (dict, list)):
                out.append(f"{pad}{k}: {'{}' if isinstance(item, dict) else '[]'}")
            else:
                out.append(f"{pad}{k}: {scalar(item, indent)}")
    else:
        for item in value:
            lines = emit(item, indent + 2)
            out.append(f"{pad}- {lines[0].lstrip()}"); out.extend(lines[1:])
    return out

print("# One address, two views: every exchange, as recorded by scripts/demo-two-views.sh")
print("\n".join(emit({**meta, "steps": steps})))
PY

# What the agent reads from the answer it just got: a value by Python expression over the JSON body.
last() { python3 -c 'import json,sys; d=json.load(open(sys.argv[1])); print(eval(sys.argv[2]))' "$work/body" "$1"; }

# Fills a templated form target (RFC 6570: {a}{b} and {?x}) with name=value pairs, as HAL Schema Forms says.
fill() {
  python3 - "$@" <<'PY'
import re, sys
template, values = sys.argv[1], dict(v.split("=", 1) for v in sys.argv[2:])
def expand(m):
    names = m.group(2).split(",")
    if m.group(1) == "?":
        pairs = [f"{n}={values[n]}" for n in names if n in values]
        return "?" + "&".join(pairs) if pairs else ""
    return "".join(values.get(n, "") for n in names)
print(re.sub(r"\{(\??)([\w,]+)\}", expand, template))
PY
}

step "1. The front door" "One address to start from. The agent gets a link to the beds; the browser gets the list."
ask agent GET /
ask browser GET /
shoot /

step "2. The beds, and the form to prepare one" "Every bed links to its own address, and a form says how to prepare a new one."
ask agent GET /beds
prepare=$(last 'd["_forms"]["prepare-bed"]["_links"]["target"]["href"]')
shoot /beds

step "3. The agent fills the prepare-bed form" "$name, 4 rows × 6 columns: only a name and a size; the server chooses the id and says where the bed lives."
ask agent POST "$prepare" --json "{\"name\":\"$name\",\"dimensions\":{\"rows\":4,\"columns\":6}}"
home=$(grep -i '^location:' "$work/headers" | tr -d '\r' | cut -d' ' -f2)
bed=${home##*/}
shoot "$home"

step "4. The new bed: the same address, two views" "The agent's care forms and the browser's buttons post to the same addresses; GET forms go straight to any row, column or cell."
ask agent GET "$home"
go_to_cell=$(last 'd["_forms"]["go-to-cell"]["_links"]["target"]["href"]')
ask browser GET "$home"
shoot "$home"

step "5. Straight to cell B2" "The agent fills the bed's go-to-cell form (row B, column 2) instead of building an address; the browser taps B2."
b2=$(fill "$go_to_cell" row=B column=2)
ask agent GET "$b2"
plant_b2=$(last 'd["_forms"]["plant-seedling"]["_links"]["target"]["href"]')
ask browser GET "$b2" -H 'HX-Request: true'
shoot "$b2"

step "6. The agent plants a tomato at B2" "JSON posted to the target of B2's plant-seedling form; the focus comes back, now offering harvest instead."
ask agent POST "$plant_b2" --json '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'
shoot "$b2"

step "7. The browser plants lettuce at B3" "Form fields posted from the sheet to the same kind of address; the view comes back."
ask browser POST "$home/focus/cell/B3/plant" -H 'HX-Request: true' \
  --form 'plantType=lettuce' --form 'plantCultivar=Paris Island'
shoot "$home/focus/cell/B3"

step "8. The agent follows B2's zoom-out link" "No address is built by hand: it comes from the zoom-out link."
row_b=$(curl -s -H "Accept: $agent" "$base$b2" |
  python3 -c 'import json,sys; print(json.load(sys.stdin)["_links"]["zoom-out"]["href"])')
ask agent GET "$row_b"
water_row=$(last 'd["_forms"]["water-cells"]["_links"]["target"]["href"]')
shoot "$row_b"

step "9. Row B is watered, then seen by the browser" "The row's water form waters all six cells in one go; no time sent, so the server records the moment it arrives."
ask agent POST "$water_row" --json '{"volume":1.0}'
cell_in_row=$(last 'd["_forms"]["go-to-cell"]["_links"]["target"]["href"]')
ask browser GET "$row_b" -H 'HX-Request: true'
shoot "$row_b"

step "10. A form sent incomplete" "The agent goes to B4 with Row B's go-to-cell form and sends plant-seedling without a cultivar: a 400 vnd.error names the missing field."
b4=$(fill "$cell_in_row" row=B column=4)
ask agent GET "$b4"
ask agent POST "$(last 'd["_forms"]["plant-seedling"]["_links"]["target"]["href"]')" --json '{"plantType":"tomato"}'

step "11. Planting B2 again, from a stale form" "B2 no longer offers plant-seedling; posting the old form anyway gets a 409 vnd.error in the garden's words, linking back to B2."
ask agent POST "$plant_b2" --json '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'

step "12. Refresh with fewer recent events" "B2's refresh form says how to ask for a different number of recent events: here 1."
ask agent GET "$b2"
ask agent GET "$(fill "$(last 'd["_forms"]["refresh"]["_links"]["target"]["href"]')" recent=1)"

step "13. The journal" "Links to each way of reading it for the agent; the open journal for the browser."
ask agent GET "$home/journal?by=row"
ask browser GET "$home/journal?by=row&size=full"
shoot "$home/journal?by=row&size=full"

meta=$(python3 -c 'import json,sys; print(json.dumps({"title": "One address, two views", "base": sys.argv[1], "bed": sys.argv[2], "bedName": sys.argv[3], "captured": sys.argv[4]}))' \
  "$base" "$bed" "$name" "$(date -u +%Y-%m-%dT%H:%M:%SZ)")
python3 "$work/to_yaml.py" "$meta" "$work/exchanges.jsonl" "$work/shots.jsonl" > "$out"
echo "Recorded $(wc -l < "$work/exchanges.jsonl" | tr -d ' ') exchanges to $out" >&2
echo "Render it with: $here/render-demo-report.py \"$out\"" >&2
