#!/usr/bin/env bash
# One address, two views: walks through preparing a bed, planting, moving around and watering,
# asking each address as an agent (HAL Schema Forms) and as a browser (HTML and htmx partials).
#
# By default each answer is summarized side by side in the same terms: for the agent its links,
# forms and embedded events; for the browser what a person can tap and which care buttons post
# where. --full prints the raw answers instead (headers and bodies, JSON pretty-printed).
#
# Usage: scripts/demo-two-views.sh [--full] [baseUrl]      (default http://localhost:8080)
# Needs curl, python3 and uuidgen. Each run prepares a new bed, so it can be run again and again.
set -u

full=false
if [ "${1:-}" = "--full" ]; then full=true; shift; fi
base="${1:-http://localhost:8080}"
agent='application/hal+json'
browser='text/html'
bed=$(uuidgen | tr 'A-Z' 'a-z')
name="Demo $(date +%H:%M:%S)"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

bold=$'\e[1m'; dim=$'\e[2m'; reset=$'\e[0m'; cyan=$'\e[36m'; magenta=$'\e[35m'; yellow=$'\e[33m'

step() { printf '\n%s━━ %s%s\n' "$bold$yellow" "$*" "$reset"; }

# ask WHO METHOD PATH [curl args...]: WHO is "agent" or "browser" (htmx adds HX-Request).
ask() {
  local who=$1 method=$2 path=$3; shift 3
  local accept=$agent color=$cyan
  [ "$who" != agent ] && { accept=$browser; color=$magenta; }
  printf '%s%-8s%s %s %s\n' "$color$bold" "$who" "$reset" "$method" "${path//$bed/\{bed\}}"
  curl -s -X "$method" -H "Accept: $accept" -D "$work/headers" -o "$work/body" "$@" "$base$path"
  if $full; then
    cat "$work/headers"
    if grep -qi '^content-type: application/.*json' "$work/headers"; then python3 -m json.tool "$work/body"
    else cat "$work/body"; fi
    echo
  else
    python3 "$work/summarize.py" "$work/headers" "$work/body" "$bed"
  fi
}

cat > "$work/summarize.py" <<'PY'
import json, re, sys
from html.parser import HTMLParser

headers, body, bed = open(sys.argv[1]).read(), open(sys.argv[2], encoding="utf-8").read(), sys.argv[3]
B, D, R, G, RED, C = "\033[1m", "\033[2m", "\033[0m", "\033[32m", "\033[31m", "\033[36m"
short = lambda s: s.replace(bed, "{bed}") if isinstance(s, str) else s

status = headers.split("\n", 1)[0].split(" ", 2)[1]
ctype = re.search(r"(?im)^content-type:\s*([^;\r\n]+)", headers)
ctype = ctype.group(1) if ctype else "-"
vary = re.search(r"(?im)^vary:\s*(.+?)\r?$", headers)
print(f"  {(G if status.startswith('2') else RED)}{B}{status}{R}  {ctype}"
      + (f"  {D}vary: {vary.group(1)}{R}" if vary and vary.group(1).strip() else ""))

def line(label, text): print(f"  {D}{label:<9}{R}{text}")

if "json" in ctype:
    doc = json.loads(body)
    if ctype == "application/problem+json":
        line("problem", f"{RED}{doc.get('title')}{R}: {doc.get('detail')}")
        sys.exit()
    facts = {k: v for k, v in doc.items() if not k.startswith("_") and v is not None and not isinstance(v, (list, dict)) and k not in ("bedId", "id")}
    if facts: line("about", "  ".join(f"{k}={v}" for k, v in facts.items()))
    for rel, link in doc.get("_links", {}).items():
        line("link", f"{C}{rel:<11}{R}→ {short(link['href'])}")
    for form_id, form in doc.get("_forms", {}).items():
        needs = [f for f in form["schema"].get("required", []) if f not in ("bedId", "started", "location")]
        props = form["schema"].get("properties", {})
        fixed = props.get("location", {}).get("const")
        extra = (f" at {fixed}" if fixed else "") + (f"  needs {', '.join(needs)}" if needs else "")
        choices = {p: props[p]["enum"] for p in needs if "enum" in props.get(p, {})}
        extra += "".join(f"  {p}∈{v}" for p, v in choices.items())
        line("form", f"{C}{form_id:<16}{R}{form['method']} {short(form['_links']['target']['href'])}{D}{extra}{R}")
    for kind, items in doc.get("_embedded", {}).items():
        for item in items:
            if "type" in item: line(kind, f"{item['type']} {' '.join(item['cells'])}")
            else: line(kind, f"{item['name']} ({item['rows']}×{item['columns']}) → {short(item['_links']['self']['href'])}")
    for line_ in doc.get("lines", [])[:4]:
        cmds = "; ".join(f"{c['type']} {' '.join(c['cells'])}" for c in line_["commands"][:3])
        line("line", f"{line_['line']}: {cmds}")
    if "planting" in str(doc.get("cells", "")):
        planted = [f"{c['position']} {c['planting']['plantType']}" for c in doc["cells"] if c["planting"]["plantType"]]
        line("growing", ", ".join(planted) or "nothing")
    sys.exit()

if "html" in ctype:
    class Page(HTMLParser):
        keep = {"crumb", "sheet-title", "sheet-meta", "bed-link", "sheet-message"}
        def __init__(self):
            super().__init__(); self.stack = []; self.texts = []; self.taps = []; self.posts = []; self.full = False
        def handle_starttag(self, tag, attrs):
            a = dict(attrs); classes = set((a.get("class") or "").split())
            if tag == "html": self.full = True
            if "hx-get" in a: self.taps.append(short(a["hx-get"]))
            if tag == "form" and "hx-post" in a: self.posts.append((a.get("data-action"), short(a["hx-post"]), []))
            if tag in ("input", "select") and a.get("name") and self.posts: self.posts[-1][2].append(a["name"])
            if tag == "a" and "bed-link" in classes: self.texts.append(["bed", "", short(a.get("href"))])
            hit = classes & self.keep
            self.stack.append((tag, next(iter(hit)) if hit else None))
            if hit and "bed-link" not in hit: self.texts.append([next(iter(hit)), "", None])
        def handle_endtag(self, tag):
            while self.stack:
                t, _ = self.stack.pop()
                if t == tag: break
        def handle_data(self, data):
            if any(k for _, k in self.stack) and self.texts: self.texts[-1][1] += data.strip()
    page = Page(); page.feed(body)
    line("view", "the whole page" if page.full else "only #view (htmx partial)")
    crumbs = [t for k, t, _ in page.texts if k == "crumb"]
    if crumbs: line("crumbs", " › ".join(crumbs))
    for k, t, href in page.texts:
        if k in ("sheet-title", "sheet-meta", "sheet-message") and t: line(k.replace("sheet-", ""), t)
        if k == "bed": line("bed", f"{t} → {href}")
    taps = list(dict.fromkeys(page.taps))
    if taps:
        focuses = [t.split("/focus/")[1] for t in taps if "/focus/" in t]
        others = [t for t in taps if "/focus/" not in t]
        line("taps", f"{len(taps)} places: " + " ".join(focuses[:14]) + (" …" if len(focuses) > 14 else ""))
        for t in others[:4]: line("", f"{D}{t}{R}")
    for action, target, fields in page.posts:
        line("button", f"{C}{action:<16}{R}POST {target}{D}" + (f"  asks {', '.join(fields)}" if fields else "") + R)
    sys.exit()

print("  " + body[:300])
PY

printf '%sOne address, two views%s  %s(%s, bed %s)%s\n' "$bold" "$reset" "$dim" "$base" "$bed" "$reset"

step "1. The front door"
ask agent GET /
ask browser GET /

step "2. The beds, and the form to prepare one"
ask agent GET /beds

step "3. The agent fills the prepare-bed form: $name, 4 rows × 6 columns"
ask agent POST /api/beds -H 'Content-Type: application/json' \
  -d "{\"bedId\":\"$bed\",\"name\":\"$name\",\"dimensions\":{\"rows\":4,\"columns\":6},\"cellBlockSize\":1}"

step "4. The new bed: the same address, two views"
ask agent GET "/beds/$bed"
ask browser GET "/beds/$bed"

step "5. Stepping onto cell B2: eight moves and a zoom-out, and plant is possible"
ask agent GET "/beds/$bed/focus/cell/B2"
ask browser GET "/beds/$bed/focus/cell/B2" -H 'HX-Request: true'

step "6. The agent plants a tomato at B2, posting JSON to B2's own plant address"
ask agent POST "/beds/$bed/focus/cell/B2/plant" -H 'Content-Type: application/json' \
  -d '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'

step "7. The browser plants lettuce at B3 from the sheet: the same kind of address, form fields in"
ask browser POST "/beds/$bed/focus/cell/B3/plant" -H 'HX-Request: true' \
  --data-urlencode 'plantType=lettuce' --data-urlencode 'plantCultivar=Paris Island'

step "8. The agent follows B2's zoom-out link to Row B"
zoom_out=$(curl -s -H "Accept: $agent" "$base/beds/$bed/focus/cell/B2" |
  python3 -c 'import json,sys; print(json.load(sys.stdin)["_links"]["zoom-out"]["href"])')
ask agent GET "$zoom_out"

step "9. Row B gets watered through the row's water form, then seen by the browser"
ask agent POST "/beds/$bed/focus/row/B/water" -H 'Content-Type: application/json' -d '{"volume":1.0}'
ask browser GET "/beds/$bed/focus/row/B" -H 'HX-Request: true'

step "10. Planting B2 again: the soil rules refuse"
ask agent POST "/beds/$bed/focus/cell/B2/plant" -H 'Content-Type: application/json' \
  -d '{"plantType":"tomato","plantCultivar":"Dark Galaxy"}'

step "11. The journal: links to each way of reading it for the agent, the open journal for the browser"
ask agent GET "/beds/$bed/journal?by=row"
ask browser GET "/beds/$bed/journal?by=row&size=full"
echo
