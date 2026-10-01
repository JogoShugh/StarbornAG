"""Mockups for the bed journal: the sheet's handle opens a summary of every event across the bed's
cells, by cell (the default tab), by row or by column. Uses direction C's dark palette and the
demo bed Earth as scripts/populate-garden.sh plants it. Portrait turns the bed (letters across)."""
import pathlib

OUT = pathlib.Path(__file__).parent
ROWS = "ABCDE"
COLS = range(1, 11)


def planted(cell):
    r, c = cell[0], int(cell[1:])
    if r == "A" and c <= 8: return ("🍅", "Tomato", "Dark Galaxy")
    if r == "B": return ("🥬", "Lettuce", "Paris Island")
    if r == "C" and c <= 5: return ("🍆", "Eggplant", "Black Beauty")
    if r == "C": return ("🫑", "Bell pepper", "California Wonder")
    if r == "D" and c <= 6: return ("🌶️", "Hot pepper", "Jalapeno")
    if r == "E" and c <= 3: return ("🌶", "Chili pepper", "Thai")
    return None


def care(cell):
    """Care per cell as (icon, word, detail, when), newest first."""
    r, c = cell[0], int(cell[1:])
    out = []
    if r in "AB": out.append(("💧", "Watered", "1 L", "2 h ago"))
    if r == "A" and c <= 8: out.append(("🪵", "Mulched", "straw", "2 h ago"))
    if r == "C": out.append(("🌿", "Fed", "fish emulsion", "2 h ago"))
    if (r == "C") or (r == "D" and c <= 6): out.append(("💧", "Watered", "1 L", "2 h ago"))
    if r == "A" and c in (3, 4): out.append(("💧", "Watered", "1 L", "yesterday"))
    p = planted(cell)
    if p: out.append((p[0], "Planted", f"{p[1].lower()} · {p[2]}", "3 h ago"))
    return out


CSS = """
body { margin: 0; padding: 24px; background: #2b2b2b; font-family: -apple-system, 'Segoe UI', sans-serif; }
h1 { color: #fff; margin: 0 0 4px; font-size: 26px; } .sub { color: #bbb; margin: 0 0 18px; max-width: 1700px; }
.phones { display: flex; gap: 28px; flex-wrap: wrap; } figure { margin: 0; }
figcaption { color: #eee; font-size: 15px; margin-bottom: 8px; font-weight: 600; max-width: 390px; }
.phone { width: 390px; height: 844px; border: 10px solid #111; border-radius: 40px; overflow: hidden; position: relative;
  display: flex; flex-direction: column; box-sizing: border-box; background: #14261a; color: #eef3e9; }
.land { width: 844px; height: 390px; flex-direction: row; }
.top { display: flex; justify-content: space-between; align-items: center; padding: 12px 14px 4px; }
.crumbs { background: rgba(0,0,0,.45); border-radius: 14px; padding: 5px 12px; font-size: 14px; }
.crumbs u { text-underline-offset: 3px; } .toggle { width: 34px; height: 34px; border: 2px solid #eef3e9; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; font-size: 16px; }
.map { flex: 1; display: flex; align-items: center; justify-content: center; padding: 6px; min-height: 0; }
.grid { display: grid; gap: 3px; }
.t { border-radius: 8px; background: #3a2a1e; position: relative; display: flex; align-items: center; justify-content: center;
  box-shadow: inset 0 0 10px rgba(0,0,0,.6); }
.t .l { position: absolute; top: 2px; left: 4px; font-size: 9px; font-weight: 700; opacity: .7; }
.t .e { position: absolute; bottom: 1px; right: 3px; font-size: 8px; }
.mini { border-radius: 4px; background: #3a2a1e; text-align: center; overflow: hidden; }
.mini-lbl { color: #b8c7b0; font-weight: 800; font-size: 9px; display: flex; align-items: center; justify-content: center; }
.pill { background: rgba(0,0,0,.45); border: 2px solid #eef3e9; border-radius: 16px; padding: 5px 10px; font-size: 13px; font-weight: 700; }
.pill.dark { background: #fff; color: #22301f; border-color: #22301f; }
.lbl { color: #b8c7b0; font-weight: 800; font-size: 12px; display: flex; align-items: center; justify-content: center; }
.sheet { background: #f4efe4; color: #22301f; border-radius: 22px 22px 0 0; padding: 8px 14px 14px; display: flex; flex-direction: column; min-height: 0; }
.handle { margin: 0 auto 6px; display: flex; flex-direction: column; align-items: center; gap: 2px; color: #6d6a5e; font-size: 11px; font-weight: 700; }
.handle i { width: 44px; height: 5px; border-radius: 3px; background: #b9b09c; display: block; }
.h { font-size: 20px; font-weight: 800; } .m { font-size: 12px; color: #6d6a5e; }
.acts { display: grid; grid-template-columns: 1fr 1fr; gap: 7px; margin: 10px 0 4px; }
.acts div { background: #2f6b34; color: #fff; border-radius: 12px; padding: 10px 0; text-align: center; font-weight: 700; font-size: 14px; }
.tabs { display: flex; background: #e6dfcf; border-radius: 12px; padding: 3px; margin: 8px 0; }
.tabs div { flex: 1; text-align: center; padding: 7px 0; font-weight: 700; font-size: 13px; border-radius: 9px; color: #5a5848; }
.tabs .on { background: #fff; color: #22301f; box-shadow: 0 1px 2px rgba(0,0,0,.15); }
.chips { display: flex; gap: 5px; overflow: hidden; margin-bottom: 6px; } .chips span { white-space: nowrap; border: 1px solid #cfc6b2; border-radius: 12px; padding: 3px 9px; font-size: 12px; }
.chips .on { background: #22301f; color: #fff; border-color: #22301f; }
.stats { display: flex; gap: 6px; margin: 4px 0 2px; } .stats div { flex: 1; background: #fff; border-radius: 10px; padding: 6px 4px; text-align: center; }
.stats b { display: block; font-size: 17px; } .stats span { font-size: 10px; color: #6d6a5e; }
.list { overflow: hidden; flex: 1; }
.card { display: flex; align-items: center; gap: 10px; background: #fff; border-radius: 12px; padding: 7px 10px; margin-bottom: 6px; }
.chip { background: #22301f; color: #fff; border-radius: 7px; padding: 3px 6px; font-weight: 800; font-size: 12px; min-width: 26px; text-align: center; }
.ico { font-size: 24px; width: 30px; text-align: center; } .grow { flex: 1; min-width: 0; }
.name { font-weight: 700; font-size: 14px; } .when { font-size: 11px; color: #6d6a5e; white-space: nowrap; }
.counts { font-size: 12px; color: #4a4a3c; } .flag { font-size: 10px; font-weight: 800; color: #9b2c1f; background: #f8dcd6; border-radius: 6px; padding: 2px 5px; }
.sec { background: #fff; border-radius: 12px; padding: 8px 10px; margin-bottom: 7px; }
.sec .hd { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 4px; }
.sec .hd b { font-size: 15px; } .ev { display: flex; align-items: center; gap: 8px; font-size: 13px; padding: 3px 0; border-top: 1px solid #efe8d8; }
.strip { display: flex; gap: 2px; } .strip i { width: 9px; height: 9px; border-radius: 2px; background: #e2dbc9; display: block; }
.strip i.on { background: #2f6b34; }
.note { color: #ddd; font-size: 13px; max-width: 390px; margin-top: 8px; line-height: 1.35; }
"""


def tile(cell, size, dim=False):
    p = planted(cell)
    icons = "".join(dict.fromkeys(i for i, *_ in care(cell) if i in "💧🪵🌿"))
    return (f'<div class="t" style="width:{size}px;height:{size}px;font-size:{int(size * .5)}px{";opacity:.45" if dim else ""}">'
            f'<span class="l">{cell}</span>{p[0] if p else ""}<span class="e">{icons}</span></div>')


def mini_bed(size, focus=None, portrait=True):
    """A small whole-bed map for while the journal is open: plants only, no labels or care icons in
    the tiles, so nothing crowds. Portrait turns it (letters across); [focus] ("row C") is outlined,
    the rest dimmed."""
    lines, across = (COLS, ROWS) if portrait else (ROWS, COLS)
    head = '<div></div>' + "".join(f'<div class="mini-lbl">{a}</div>' for a in across)
    body = []
    for line in lines:
        body.append(f'<div class="mini-lbl">{line}</div>')
        for a in across:
            r, c = (a, line) if portrait else (line, a)
            p = planted(f"{r}{c}")
            inside = focus is None or focus == f"row {r}" or focus == f"column {c}"
            style = f"width:{size}px;height:{size}px;font-size:{int(size * .62)}px;line-height:{size}px"
            if not inside: style += ";opacity:.3"
            if focus and inside: style += ";outline:2px solid #f2c14e;outline-offset:-1px"
            body.append(f'<div class="mini" style="{style}">{p[0] if p else ""}</div>')
    columns = f"14px repeat({len(across)},{size}px)"
    return f'<div class="grid" style="gap:3px;grid-template-columns:{columns}">{head}{"".join(body)}</div>'


def portrait_bed(size):
    h = [f'<div class="grid" style="grid-template-columns:18px repeat(5,{size}px)"><div></div>']
    h += [f'<div class="lbl">{r}</div>' for r in ROWS]
    for c in COLS:
        h.append(f'<div class="lbl">{c}</div>')
        h += [tile(f"{r}{c}", size) for r in ROWS]
    return "".join(h) + "</div>"


def land_bed(size):
    h = [f'<div class="grid" style="grid-template-columns:16px repeat(10,{size}px)"><div></div>']
    h += [f'<div class="lbl">{c}</div>' for c in COLS]
    for r in ROWS:
        h.append(f'<div class="lbl">{r}</div>')
        h += [tile(f"{r}{c}", size) for c in COLS]
    return "".join(h) + "</div>"


def top(crumbs):
    return f'<div class="top"><span class="crumbs">{crumbs}</span><span class="toggle">◐</span></div>'


def handle(text):
    return f'<div class="handle"><i></i>{text}</div>'


def tabs(on):
    return '<div class="tabs">' + "".join(f'<div class="{"on" if t == on else ""}">{t}</div>' for t in ("Cells", "Rows", "Columns")) + "</div>"


CHIPS = '<div class="chips"><span class="on">All</span><span>💧</span><span>🌿</span><span>🪵</span><span>🧺</span><span>⚠ Needs care</span></div>'
STATS = ('<div class="stats"><div><b>37</b><span>planted</span></div><div><b>13</b><span>empty</span></div>'
         '<div><b>94</b><span>events</span></div><div><b>2 h</b><span>last water</span></div></div>')


def cell_card(cell, flag=""):
    p = planted(cell)
    events = care(cell)
    counts = {}
    for i, *_ in events:
        if i in "💧🪵🌿🧺": counts[i] = counts.get(i, 0) + 1
    count_text = " ".join(f"{i}{n}" for i, n in counts.items()) or "no care yet"
    last = events[0] if events else None
    name = f"{p[1]} · {p[2]}" if p else "Empty"
    return (f'<div class="card"><span class="chip">{cell}</span><span class="ico">{p[0] if p else "·"}</span>'
            f'<div class="grow"><div class="name">{name}</div><div class="counts">{count_text} · {len(events)} events {flag}</div></div>'
            f'<div class="when">{last[1].lower() + "<br>" + last[3] if last else ""}</div></div>')


def strip(cells_on, total):
    return '<span class="strip">' + "".join(f'<i class="{"on" if i in cells_on else ""}"></i>' for i in range(total)) + "</span>"


def row_section(r):
    plants = {}
    for c in COLS:
        p = planted(f"{r}{c}")
        if p: plants[p[0] + " " + p[1]] = plants.get(p[0] + " " + p[1], 0) + 1
    empty = sum(1 for c in COLS if not planted(f"{r}{c}"))
    summary = " · ".join(f"{n} {k}" for k, n in plants.items()) + (f" · {empty} empty" if empty else "")
    # Events of one command share a moment: one line per command, with which cells it reached.
    groups = {}
    for idx, c in enumerate(COLS):
        for i, w, d, t in care(f"{r}{c}"):
            key = (i if w != "Planted" else "🌱", w, t)
            groups.setdefault(key, []).append(idx)
    lines = []
    for (i, w, t), idxs in list(groups.items())[:3]:
        first, last = f"{r}{idxs[0] + 1}", f"{r}{idxs[-1] + 1}"
        span = first if first == last else f"{first}–{last}"
        lines.append(f'<div class="ev"><span>{i}</span><span class="grow">{w} {span}</span>{strip(idxs, 10)}<span class="when">{t}</span></div>')
    return f'<div class="sec"><div class="hd"><b>Row {r}</b><span class="m">{summary}</span></div>{"".join(lines)}</div>'


def column_section(c):
    plants = "".join(planted(f"{r}{c}")[0] if planted(f"{r}{c}") else "·" for r in ROWS)
    groups = {}
    for idx, r in enumerate(ROWS):
        for i, w, d, t in care(f"{r}{c}"):
            key = (i if w != "Planted" else "🌱", w, t)
            groups.setdefault(key, []).append(idx)
    lines = []
    for (i, w, t), idxs in list(groups.items())[:3]:
        cells = " ".join(f"{ROWS[x]}{c}" for x in idxs)
        lines.append(f'<div class="ev"><span>{i}</span><span class="grow">{w} {cells}</span>{strip(idxs, 5)}<span class="when">{t}</span></div>')
    return f'<div class="sec"><div class="hd"><b>Column {c}</b><span class="m" style="font-size:15px;letter-spacing:2px">{plants}</span></div>{"".join(lines)}</div>'


def phone(caption, body, cls="", note=""):
    return f'<figure><figcaption>{caption}</figcaption><div class="phone {cls}">{body}</div><div class="note">{note}</div></figure>'


def collapsed():
    sheet = ('<div class="sheet">' + handle("Journal ▴") + '<div class="h">Earth</div><div class="m">37 of 50 planted</div>'
             '<div class="acts"><div>🌱 Plant</div><div>💧 Water</div><div>🌿 Feed</div><div>🪵 Mulch</div></div></div>')
    return top("<b>Earth</b>") + f'<div class="map">{portrait_bed(54)}</div>' + sheet


def journal(tab, body, full=False):
    """Half: the mini map stays on top. Full: the journal takes the whole screen; a Map pill brings the map back."""
    if full:
        bar = ('<div class="top"><span class="crumbs"><b>Earth</b> · journal</span>'
               '<span style="display:flex;gap:8px"><span class="pill">🗺 Map</span><span class="toggle">◐</span></span></div>')
        return (bar + '<div class="sheet" style="flex:1;border-radius:16px 16px 0 0">' + handle("▾ Half · show the map") +
                '<div style="display:flex;justify-content:space-between;align-items:baseline"><div class="h">Earth journal</div>'
                '<div class="m">all 50 cells</div></div>' + tabs(tab) + body + "</div>")
    mini_map = f'<div class="map" style="flex:0 0 236px">{mini_bed(17)}</div>'
    return (top("<b>Earth</b> · journal") + mini_map +
            '<div class="sheet" style="flex:1">' + handle("▴ Full &nbsp;·&nbsp; ▾ Close") +
            '<div style="display:flex;justify-content:space-between;align-items:baseline"><div class="h">Earth journal</div><div class="m">all 50 cells</div></div>'
            + tabs(tab) + body + "</div>")


def cells_tab(more=False):
    order = ["A3", "A4", "C5", "C6", "D2", "B1", "E1", "A9"]
    if more: order = ["A3", "A4", "A1", "A2", "C5", "C6", "C1", "D2", "D3", "B1", "B2", "E1", "A9"]
    flags = {"E1": '<span class="flag">dry 3 days</span>', "A9": ""}
    return STATS + CHIPS + '<div class="list">' + "".join(cell_card(c, flags.get(c, "")) for c in order) + "</div>"


def rows_tab(rows="ABCD"):
    return CHIPS + '<div class="list">' + "".join(row_section(r) for r in rows) + "</div>"


def columns_tab(columns=(1, 6, 9)):
    return CHIPS + '<div class="list">' + "".join(column_section(c) for c in columns) + "</div>"


def landscape_full():
    cards = "".join(cell_card(c) for c in ["A3", "A4", "C5", "C6", "D2", "B1", "E1", "A9"])
    return ('<div class="sheet" style="flex:1;border-radius:0;padding:10px 16px">'
            '<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:2px">'
            '<span class="h">Earth journal <span class="m">· all 50 cells</span></span>'
            '<span style="display:flex;gap:8px"><span class="pill dark">🗺 Map</span><span class="pill dark">✕ Close</span></span></div>'
            + tabs("Cells") + STATS + CHIPS +
            f'<div class="list" style="display:grid;grid-template-columns:1fr 1fr;column-gap:8px;align-content:start">{cards}</div></div>')


def landscape():
    side = ('<div class="sheet" style="width:420px;border-radius:22px 0 0 22px;padding:12px 14px">'
            '<div style="display:flex;justify-content:space-between;align-items:baseline"><div class="h">Earth journal</div><div class="m">⤢ Full · ✕ Close</div></div>'
            + tabs("Cells") + CHIPS + '<div class="list">' + "".join(cell_card(c) for c in ["A3", "C5", "D2", "B1"]) + "</div></div>")
    left = ('<div style="flex:1;display:flex;flex-direction:column">' + top("<b>Earth</b> · journal") +
            f'<div class="map">{mini_bed(30, portrait=False)}</div></div>')
    return left + side


def scoped():
    body = (tabs("Cells").replace("Rows", "—").replace("Columns", "—") +
            '<div class="list">' + "".join(cell_card(f"C{c}") for c in range(1, 8)) + "</div>")
    strip_map = f'<div class="map" style="flex:0 0 236px">{mini_bed(17, focus="row C")}</div>'
    return (top("<u>Earth</u> › <b>Row C</b> · journal") + strip_map + '<div class="sheet" style="flex:1">' + handle("▾ Back to Row C") +
            '<div style="display:flex;justify-content:space-between;align-items:baseline"><div class="h">Row C journal</div><div class="m">10 cells</div></div>'
            + body + "</div>")


html = f"""<!doctype html><html><head><meta charset="utf-8"><title>Bed journal</title><style>{CSS}</style></head><body>
<h1>Bed journal: every cell's history, summarized</h1>
<p class="sub">The sheet's handle has three stops: collapsed, half (journal with a mini map above it) and full (journal only). Tap or drag it. At the bed it covers all 50 cells.
The Cells tab is the default: one card per cell, most recent activity first, with care counts and flags such as "dry 3 days".
Rows and Columns fold the cells' events back into the commands that made them ("Watered A1–A8") with a strip showing which cells each reached.
Every card and section is a way in: tap it to zoom the map there.</p>
<div class="phones">
{phone("1 · Bed, sheet collapsed", collapsed(), note="The handle gains a label: Journal ▴. Tap or drag up: half. Drag on up: full.")}
{phone("2 · Half: Cells tab (default)", journal("Cells", cells_tab()), note="Mini map stays on top. Bed totals, care filter chips, one card per cell by latest activity. Tap a card: cell view.")}
{phone("3 · Full: Cells tab", journal("Cells", cells_tab(more=True), full=True), note="Drag the handle to the top or tap ▴ Full: the journal fills the screen. 🗺 Map or dragging down goes back to half.")}
{phone("4 · Full: Rows tab", journal("Rows", rows_tab("ABCDE"), full=True), note="Per row: what grows, then each command once with the cells it reached. Tap Row C: row view.")}
{phone("5 · Full: Columns tab", journal("Columns", columns_tab((1, 2, 6, 9)), full=True), note="Same per column; the plant icons read top to bottom as A–E.")}
{phone("6 · Half, from Row C: scoped to the row", scoped(), note="Opened below the bed, the journal covers only the focus; Row C is outlined on the mini map.")}
</div>
<h1 style="margin-top:28px">Landscape</h1><p class="sub">Half: the side sheet widens into the journal and the whole bed stays visible. Full: the journal takes the whole screen with cards in two columns.</p>
<div class="phones">{phone("7 · Landscape, half", landscape(), "land")}{phone("8 · Landscape, full", landscape_full(), "land")}</div>
</body></html>"""

(OUT / "journal.html").write_text(html, encoding="utf-8")
print("written")
