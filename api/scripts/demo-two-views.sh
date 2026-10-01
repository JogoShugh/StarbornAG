#!/usr/bin/env bash
# One address, two views: walks through preparing a bed, planting, moving around and watering,
# asking each address as an agent (HAL Schema Forms) and as a browser (HTML and htmx partials),
# and records every exchange in full (request, status, headers, body) to a YAML file.
#
# Usage: scripts/demo-two-views.sh [baseUrl] [out.yaml]
#        (defaults: http://localhost:8080 and build/demo/two-views.yaml under api/)
# Then:  scripts/render-demo-report.py      to turn the YAML into an HTML report.
#
# Needs curl, python3 and uuidgen. Each run prepares a new bed, so it can be run again and again.
set -u

here=$(cd "$(dirname "$0")" && pwd)
base="${1:-http://localhost:8080}"
out="${2:-$(cd "$here/.." && pwd)/build/demo/two-views.yaml}"
mkdir -p "$(dirname "$out")"
agent='application/hal+json'
browser='text/html'
bed=$(uuidgen | tr 'A-Z' 'a-z')
name="Demo $(date +%H:%M:%S)"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
: > "$work/exchanges.jsonl"

title=""; note=""
step() { title=$1; note=${2:-}; printf '%s\n' "$title" >&2; }

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

step "1. The front door" "One address to start from. The agent gets a link to the beds; the browser gets the list."
ask agent GET /
ask browser GET /

step "2. The beds, and the form to prepare one" "Every bed links to its own address, and a form says how to prepare a new one."
ask agent GET /beds

step "3. The agent fills the prepare-bed form" "$name, 4 rows × 6 columns, posted as the form describes."
ask agent POST /api/beds \
  --json "{\"bedId\":\"$bed\",\"name\":\"$name\",\"dimensions\":{\"rows\":4,\"columns\":6},\"cellBlockSize\":1}"

step "4. The new bed: the same address, two views" "The agent's forms and the browser's care buttons post to the same addresses."
ask agent GET "/beds/$bed"
ask browser GET "/beds/$bed"

step "5. Stepping onto cell B2" "Eight moves and a zoom-out for the agent; the same places to tap for the browser."
ask agent GET "/beds/$bed/focus/cell/B2"
ask browser GET "/beds/$bed/focus/cell/B2" -H 'HX-Request: true'

step "6. The agent plants a tomato at B2" "JSON posted to B2's own plant address; the focus comes back, now offering harvest."
ask agent POST "/beds/$bed/focus/cell/B2/plant" --json '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'

step "7. The browser plants lettuce at B3" "Form fields posted from the sheet to the same kind of address; the view comes back."
ask browser POST "/beds/$bed/focus/cell/B3/plant" -H 'HX-Request: true' \
  --form 'plantType=lettuce' --form 'plantCultivar=Paris Island'

step "8. The agent follows B2's zoom-out link" "No address is built by hand: it comes from the zoom-out link."
zoom_out=$(curl -s -H "Accept: $agent" "$base/beds/$bed/focus/cell/B2" |
  python3 -c 'import json,sys; print(json.load(sys.stdin)["_links"]["zoom-out"]["href"])')
ask agent GET "$zoom_out"

step "9. Row B is watered, then seen by the browser" "The row's water form waters all six cells in one go."
ask agent POST "/beds/$bed/focus/row/B/water" --json '{"volume":1.0}'
ask browser GET "/beds/$bed/focus/row/B" -H 'HX-Request: true'

step "10. Planting B2 again" "The soil rules refuse: a 409 problem for the agent."
ask agent POST "/beds/$bed/focus/cell/B2/plant" --json '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'

step "11. The journal" "Links to each way of reading it for the agent; the open journal for the browser."
ask agent GET "/beds/$bed/journal?by=row"
ask browser GET "/beds/$bed/journal?by=row&size=full"

meta=$(python3 -c 'import json,sys; print(json.dumps({"title": "One address, two views", "base": sys.argv[1], "bed": sys.argv[2], "bedName": sys.argv[3], "captured": sys.argv[4]}))' \
  "$base" "$bed" "$name" "$(date -u +%Y-%m-%dT%H:%M:%SZ)")
python3 "$work/to_yaml.py" "$meta" "$work/exchanges.jsonl" > "$out"
echo "Recorded $(wc -l < "$work/exchanges.jsonl" | tr -d ' ') exchanges to $out" >&2
echo "Render it with: $here/render-demo-report.py \"$out\"" >&2
