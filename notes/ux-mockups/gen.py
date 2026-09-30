"""Portrait mockups for the StarbornAG zoom views. In portrait the bed is rotated to fit the tall
screen: row letters run ACROSS the top, column numbers run DOWN the side. So a row (A) is a vertical
strip on screen, a column (3) is a horizontal line, and a cell's on-screen neighbors are: A2 above,
A4 below, B3 to the right, and nothing to the left of row A (the bed's edge)."""
import pathlib

OUT = pathlib.Path(__file__).parent
LETTERS = "ABCDE"          # bed rows    -> screen columns (across the top)
NUMBERS = range(1, 11)     # bed columns -> screen rows    (down the side)
PLANTS = {
    "A1": ("🍅", "Tomato", "Dark Galaxy"), "A3": ("🍅", "Tomato", "Dark Galaxy"), "A5": ("🍅", "Tomato", "Dark Galaxy"),
    "A2": ("🥬", "Lettuce", "Romaine"), "A4": ("🥬", "Lettuce", "Romaine"), "A6": ("🥬", "Lettuce", "Romaine"),
    "C1": ("🥕", "Carrot", "Nantes"), "C2": ("🥕", "Carrot", "Nantes"), "C3": ("🥕", "Carrot", "Nantes"), "C4": ("🥕", "Carrot", "Nantes"),
}
CARE = {**{f"A{c}": "💧" for c in range(1, 7)}, **{f"B{c}": "🪵" for c in range(1, 4)}}
TINT = {"Tomato": "#f8d7d3", "Lettuce": "#dff2d8", "Carrot": "#fde3c8"}
HISTORY = [
    ("💧", "Watered", "1 L", "2 hours ago"),
    ("🌿", "Fertilized", "compost tea", "3 days ago"),
    ("💧", "Watered", "1 L", "4 days ago"),
    ("🪵", "Mulched", "straw", "2 weeks ago"),
    ("🌱", "Planted", "Tomato · Dark Galaxy", "5 weeks ago"),
]


def at(letter_index, number):
    """The cell at a screen position, or None past the bed's edge."""
    if 0 <= letter_index < len(LETTERS) and number in NUMBERS:
        return f"{LETTERS[letter_index]}{number}"
    return None


def neighborhood(letter_index, number):
    """3x3 on-screen neighborhood: rows are numbers (up = smaller), columns are letters (left = earlier)."""
    return [[at(letter_index + dl, number + dn) for dl in (-1, 0, 1)] for dn in (-1, 0, 1)]


def phone(title, body, css_class):
    return f'<figure><figcaption>{title}</figcaption><div class="phone {css_class}">{body}</div></figure>'


def page(name, subtitle, css, phones):
    return f"""<!doctype html><html><head><meta charset="utf-8"><title>{name}</title>
<style>
body {{ margin: 0; padding: 24px; background: #2b2b2b; font-family: -apple-system, 'Segoe UI', sans-serif; }}
h1 {{ color: #fff; margin: 0 0 4px; font-size: 26px; }} .sub {{ color: #bbb; margin: 0 0 18px; max-width: 1650px; }}
.row-of-phones {{ display: flex; gap: 28px; }}
figure {{ margin: 0; }} figcaption {{ color: #eee; font-size: 15px; margin-bottom: 8px; font-weight: 600; }}
.phone {{ width: 390px; height: 844px; border: 10px solid #111; border-radius: 40px; overflow: hidden;
  position: relative; display: flex; flex-direction: column; box-sizing: border-box; }}
{css}
</style></head><body><h1>{name}</h1><p class="sub">{subtitle}</p>
<div class="row-of-phones">{''.join(phones)}</div></body></html>"""


def portrait_bed(cell_fn, size, label_class, gap=3, label_w=22):
    """The whole bed rotated for portrait: letters across the top, numbers down the side."""
    html = [f'<div class="bedgrid" style="display:grid;gap:{gap}px;grid-template-columns:{label_w}px repeat(5,{size}px)"><div></div>']
    html += [f'<div class="{label_class}">{l}</div>' for l in LETTERS]
    for n in NUMBERS:
        html.append(f'<div class="{label_class}">{n}</div>')
        html += [cell_fn(f"{l}{n}", size) for l in LETTERS]
    html.append("</div>")
    return "".join(html)


# ---------------------------------------------------------------- A. Zoomed soil
A_CSS = """
.a { background: linear-gradient(160deg, #1e5e2e, #0f3d1c); color: #f5deb3; }
.a .crumbs { display: flex; gap: 6px; padding: 16px 14px 8px; font-size: 15px; align-items: center; }
.a .crumb { background: #8B4513; color: #fff4dc; padding: 6px 10px; border-radius: 16px; }
.a .crumb.now { background: #f5deb3; color: #4B2E1A; font-weight: 700; }
.a .main { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 0 10px; position: relative; }
.a .soil { background: radial-gradient(circle, #4E3629, #2a1d14); border: 3px solid #b0703a; border-radius: 6px;
  display: flex; align-items: center; justify-content: center; position: relative; box-sizing: border-box; }
.a .bedgrid { background: #c08a52; padding: 5px; border-radius: 8px; }
.a .lbl { color: #4B2E1A; font-weight: 800; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.a .bar { display: flex; justify-content: space-around; background: #3b2a1c; padding: 10px 6px 24px; border-top: 3px solid #8B4513; }
.a .bar div { background: #2E7D32; color: white; border-radius: 12px; padding: 10px 8px; font-size: 14px; min-width: 64px; text-align: center; }
.a .edge { background: rgba(245,222,179,.95); color: #4B2E1A; border-radius: 12px; font-size: 24px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 6px rgba(0,0,0,.4); }
.a .note { font-size: 13px; opacity: .85; margin-top: 8px; text-align: center; }
.a .hist { width: 100%; margin-top: 8px; } .a .hist div { display: flex; justify-content: space-between; padding: 6px 4px;
  border-bottom: 1px solid rgba(245,222,179,.2); font-size: 14px; }
.a .mini { position: absolute; top: 12px; right: 14px; display: grid; grid-template-columns: repeat(5, 7px); gap: 1px;
  background: #8B4513; padding: 3px; border-radius: 3px; } .a .mini span { width: 7px; height: 7px; background: #3b2a1c; }
.a .mini span.me { background: gold; }
"""


def a_cell(p, size):
    if p is None:
        return f'<div style="width:{size}px;height:{size}px"></div>'
    icon = PLANTS.get(p, ("", "", ""))[0]
    care = CARE.get(p, "")
    tag = f'<span style="position:absolute;top:2px;left:4px;font-size:{max(9, int(size * .14))}px;opacity:.7">{p}</span>' if size > 60 else ""
    return (f'<div class="soil" style="width:{size}px;height:{size}px"><span style="font-size:{int(size * .5)}px">{icon}</span>'
            f'<span style="font-size:{max(9, int(size * .18))}px;position:absolute;bottom:2px;left:3px">{care}</span>{tag}</div>')


def a_crumbs(*parts):
    items = [f'<span class="{"crumb now" if i == len(parts) - 1 else "crumb"}">{p}</span>' for i, p in enumerate(parts)]
    return '<div class="crumbs">' + '<span style="opacity:.6">›</span>'.join(items) + "</div>"


def a_bar(actions):
    return '<div class="bar">' + "".join(f"<div>{a}</div>" for a in actions) + "</div>"


def option_a():
    bed_view = (a_crumbs("Jupiter") + '<div class="main">' + portrait_bed(a_cell, 58, "lbl") +
                '<div class="note">Tap a letter for a row, a number for a column, a cell to stand on it</div></div>' +
                a_bar(["💧 Water", "🌿 Feed", "🪵 Mulch", "🌱 Plant"]))

    strip = "".join(a_cell(f"A{n}", 86) for n in range(1, 8))
    row_view = (a_crumbs("Jupiter", "Row A") +
                '<div class="main" style="flex-direction:row;gap:12px;justify-content:center;align-items:flex-start;padding-top:6px;overflow:hidden">'
                f'<div style="display:flex;flex-direction:column;gap:5px">{strip}</div>'
                '<div style="display:flex;flex-direction:column;justify-content:center;height:640px">'
                '<div class="edge" style="width:96px;height:96px;flex-direction:column;font-size:26px">→<span style="font-size:14px">Row B</span></div></div>'
                '</div><div class="note" style="margin:4px 0 6px">▼ scroll the row · A1–A10 · 6 planted</div>' +
                a_bar(["💧 Water", "🌿 Feed", "🪵 Mulch", "🧺 Harvest"]))

    line = "".join(a_cell(f"{l}3", 66) for l in LETTERS)
    col_view = (a_crumbs("Jupiter", "Column 3") + '<div class="main" style="gap:14px">'
                '<div class="edge" style="width:200px;height:56px;font-size:18px">↑ Column 2</div>'
                f'<div style="display:flex;gap:5px">{line}</div>'
                '<div class="edge" style="width:200px;height:56px;font-size:18px">↓ Column 4</div>'
                '<div class="note">A3–E3 · 2 of 5 planted</div></div>' +
                a_bar(["💧 Water", "🌿 Feed", "🪵 Mulch", "🧺 Harvest"]))

    mini = '<div class="mini">' + "".join(f'<span class="{"me" if f"{l}{n}" == "A3" else ""}"></span>' for n in NUMBERS for l in LETTERS) + "</div>"
    hood = neighborhood(0, 3)
    glyph = [["↖", "↑", "↗"], ["←", "", "→"], ["↙", "↓", "↘"]]
    place = {(0, 0): (0, 0), (0, 1): (104, -6), (0, 2): (220, 0), (1, 0): (-8, 104), (1, 2): (228, 104), (2, 0): (0, 218), (2, 1): (104, 226), (2, 2): (220, 218)}
    arrows = "".join(
        f'<div class="edge" style="position:absolute;left:{x}px;top:{y}px;width:52px;height:52px">{glyph[r][c]}</div>'
        for (r, c), (x, y) in place.items() if hood[r][c] is not None)
    big = f'<div style="position:relative;width:280px;height:280px;margin-top:4px"><div style="position:absolute;left:34px;top:34px">{a_cell("A3", 210)}</div>{arrows}</div>'
    hist = '<div class="hist">' + "".join(f"<div><span>{i} {w} · {d}</span><span style='opacity:.7'>{t}</span></div>" for i, w, d, t in HISTORY[:4]) + "</div>"
    cell_view = (a_crumbs("Jupiter", "Row A", "A3") + mini + '<div class="main" style="justify-content:flex-start">' + big +
                 '<div style="font-size:20px;font-weight:700">Tomato · Dark Galaxy</div>'
                 '<div class="note">↑ A2 · ↓ A4 · → B3 · the left edge is the bed\'s edge</div>' + hist + "</div>" +
                 a_bar(["💧 Water", "🌿 Feed", "🪵 Mulch", "🧺 Harvest"]))
    return page("A · Zoomed soil (portrait)",
                "The bed is rotated for a tall screen: row letters across the top, column numbers down the side. A row is a vertical strip (the next row is to the right), a column is a horizontal line (the next column is below). In the cell view the arrows point where the neighbors really are on screen.",
                A_CSS, [phone("Bed", bed_view, "a"), phone("Row A", row_view, "a"), phone("Column 3", col_view, "a"), phone("Cell A3", cell_view, "a")])


# ---------------------------------------------------------------- B. Field notebook
B_CSS = """
.b { background: #fbf7ee; color: #1f2a1d; }
.b .top { padding: 16px 16px 6px; } .b .crumbs { font-size: 14px; color: #6b6b5b; } .b .crumbs b { color: #1f2a1d; }
.b .title { font-size: 24px; font-weight: 800; margin-top: 2px; }
.b .main { flex: 1; padding: 4px 14px; overflow: hidden; position: relative; }
.b .c { border-radius: 8px; background: #e9e3d3; display: flex; align-items: center; justify-content: center; }
.b .lbl { display: flex; align-items: center; justify-content: center; font-weight: 800; color: #6b6b5b; font-size: 14px; }
.b .card { display: flex; align-items: center; gap: 12px; background: white; border-radius: 14px; padding: 8px 12px;
  margin-bottom: 7px; box-shadow: 0 1px 3px rgba(0,0,0,.12); }
.b .chip { background: #1f2a1d; color: white; border-radius: 8px; padding: 4px 7px; font-weight: 800; font-size: 13px; min-width: 26px; text-align: center; }
.b .ico { font-size: 28px; width: 38px; text-align: center; } .b .meta { font-size: 13px; color: #6b6b5b; }
.b .bar { display: flex; gap: 8px; padding: 10px 12px 24px; background: white; border-top: 1px solid #e0d9c8; }
.b .bar div { flex: 1; text-align: center; background: #2f6b34; color: white; border-radius: 12px; padding: 12px 0; font-weight: 700; font-size: 14px; }
.b .navbtn { background: white; border: 2px solid #1f2a1d; border-radius: 12px; padding: 8px 12px; font-weight: 700; font-size: 15px; text-align: center; }
.b .off { opacity: .2; }
.b .tile { border-radius: 14px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.b .ring { position: relative; width: 290px; height: 290px; margin: 2px auto 0; }
.b .ring .mid { position: absolute; left: 65px; top: 65px; width: 160px; height: 160px; border-radius: 50%; background: #f8d7d3;
  display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 64px; }
.b .ring .dir { position: absolute; width: 50px; height: 50px; border-radius: 50%; background: white; border: 2px solid #1f2a1d;
  display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: 800; }
.b .tl div { display: flex; gap: 10px; font-size: 14px; padding: 5px 0; border-bottom: 1px solid #e9e3d3; }
"""


def b_cell(p, size):
    icon, name, _ = PLANTS.get(p, ("", "", ""))
    return f'<div class="c" style="width:{size}px;height:{size}px;background:{TINT.get(name, "#e9e3d3")};font-size:{int(size * .55)}px">{icon}</div>'


def b_card(p):
    icon, name, cultivar = PLANTS.get(p, ("·", "Empty", ""))
    care = {"💧": "watered 2 h ago", "🪵": "mulched · straw"}.get(CARE.get(p, ""), "no care yet")
    return (f'<div class="card"><span class="chip">{p}</span><span class="ico">{icon}</span>'
            f'<div><div style="font-weight:700">{name}{" · " + cultivar if cultivar else ""}</div><div class="meta">{care}</div></div></div>')


def b_bar(actions):
    return '<div class="bar">' + "".join(f"<div>{a}</div>" for a in actions) + "</div>"


def option_b():
    legend = '<div class="meta" style="margin-top:10px">🍅 Tomato 3 · 🥬 Lettuce 3 · 🥕 Carrot 4 · 40 empty</div>'
    bed_view = ('<div class="top"><div class="crumbs"><b>Jupiter</b></div><div class="title">Jupiter</div></div>'
                '<div class="main">' + portrait_bed(b_cell, 57, "lbl", gap=4) + legend + "</div>" +
                b_bar(["💧 Water", "🌿 Feed", "🪵 Mulch"]))
    row_view = ('<div class="top"><div class="crumbs">Jupiter › <b>Row A</b></div>'
                '<div style="display:flex;justify-content:space-between;align-items:center"><div class="title">Row A</div>'
                '<span class="navbtn">Row B →</span></div></div>'
                '<div class="main">' + "".join(b_card(f"A{n}") for n in range(1, 10)) + '<div class="meta">A10 ↓</div></div>' +
                b_bar(["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    tiles = "".join(
        f'<div class="tile" style="width:62px;height:88px;background:{TINT.get(PLANTS.get(f"{l}3", ("", "", ""))[1], "#e9e3d3")}">'
        f'<span style="font-size:30px">{PLANTS.get(f"{l}3", ("", "", ""))[0] or "·"}</span><span class="chip" style="margin-top:4px">{l}3</span></div>'
        for l in LETTERS)
    col_view = ('<div class="top"><div class="crumbs">Jupiter › <b>Column 3</b></div><div class="title">Column 3</div></div>'
                '<div class="main"><div class="navbtn" style="margin-bottom:10px">↑ Column 2</div>'
                f'<div style="display:flex;justify-content:space-between">{tiles}</div>'
                '<div class="navbtn" style="margin:10px 0">↓ Column 4</div>' +
                b_card("A3") + b_card("C3") + "</div>" + b_bar(["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    hood = neighborhood(0, 3)
    glyph = [["↖", "↑", "↗"], ["←", "", "→"], ["↙", "↓", "↘"]]
    spots = {(0, 0): (10, 10), (0, 1): (120, 0), (0, 2): (230, 10), (1, 0): (0, 120), (1, 2): (240, 120), (2, 0): (10, 230), (2, 1): (120, 240), (2, 2): (230, 230)}
    dirs = "".join(
        f'<div class="dir{"" if hood[r][c] else " off"}" style="left:{x}px;top:{y}px">{glyph[r][c]}</div>' for (r, c), (x, y) in spots.items())
    ring = f'<div class="ring"><div class="mid">🍅<span style="font-size:14px;font-weight:800">A3</span></div>{dirs}</div>'
    tl = '<div class="tl">' + "".join(f"<div><span>{i}</span><span style='flex:1'>{w} · {d}</span><span class='meta'>{t}</span></div>" for i, w, d, t in HISTORY) + "</div>"
    cell_view = ('<div class="top"><div class="crumbs">Jupiter › Row A › <b>A3</b></div><div class="title">Tomato · Dark Galaxy</div></div>'
                 '<div class="main">' + ring + '<div class="meta" style="text-align:center">↑ A2 · ↓ A4 · → B3 · faded = the bed\'s edge</div>' + tl + "</div>" +
                 b_bar(["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    return page("B · Field notebook (portrait)",
                "Light and high-contrast for bright sun, with the bed rotated for a tall screen. A row reads down as a list of cell cards (Row B is to the right); a column is a line of tiles across (Column 2 above, Column 4 below).",
                B_CSS, [phone("Bed", bed_view, "b"), phone("Row A", row_view, "b"), phone("Column 3", col_view, "b"), phone("Cell A3", cell_view, "b")])


# ---------------------------------------------------------------- C. Neighborhood
C_CSS = """
.c-ui { background: #14261a; color: #eef3e9; }
.c-ui .map { flex: 0 0 auto; display: flex; align-items: center; justify-content: center; padding: 42px 10px 10px; position: relative; }
.c-ui .t { border-radius: 10px; background: #3a2a1e; display: flex; align-items: center; justify-content: center; position: relative;
  box-shadow: inset 0 0 12px rgba(0,0,0,.6); }
.c-ui .t .p { position: absolute; top: 3px; left: 5px; font-size: 11px; opacity: .7; }
.c-ui .lbl { display: flex; align-items: center; justify-content: center; font-weight: 800; color: #b8c7b0; font-size: 13px; }
.c-ui .sheet { flex: 1; background: #f4efe4; color: #22301f; border-radius: 22px 22px 0 0; padding: 10px 16px; overflow: hidden; }
.c-ui .grab { width: 44px; height: 5px; border-radius: 3px; background: #c9c0ad; margin: 0 auto 8px; }
.c-ui .h { font-size: 21px; font-weight: 800; } .c-ui .m { font-size: 13px; color: #6d6a5e; }
.c-ui .acts { display: flex; gap: 8px; margin: 10px 0; } .c-ui .acts div { flex: 1; background: #2f6b34; color: white; border-radius: 12px;
  padding: 11px 0; text-align: center; font-weight: 700; font-size: 14px; }
.c-ui .li { display: flex; justify-content: space-between; padding: 5px 0; border-bottom: 1px solid #e2dbc9; font-size: 14px; }
.c-ui .crumb { position: absolute; top: 10px; left: 14px; font-size: 13px; background: rgba(0,0,0,.45); padding: 4px 10px; border-radius: 12px; }
.c-ui .edge { background: repeating-linear-gradient(45deg, #1b3322, #1b3322 6px, #20402a 6px, #20402a 12px); box-shadow: none; }
"""


def c_tile(p, w, h, extra=""):
    if p is None:
        return f'<div class="t edge" style="width:{w}px;height:{h}px{extra}"></div>'
    icon = PLANTS.get(p, ("", "", ""))[0]
    return f'<div class="t" style="width:{w}px;height:{h}px;font-size:{int(min(w, h) * .5)}px{extra}"><span class="p">{p}</span>{icon}</div>'


def c_sheet(title, meta, actions, rows=()):
    return ('<div class="sheet"><div class="grab"></div>' f'<div class="h">{title}</div><div class="m">{meta}</div>'
            '<div class="acts">' + "".join(f"<div>{a}</div>" for a in actions) + "</div>" +
            "".join(f'<div class="li"><span>{a}</span><span class="m">{b}</span></div>' for a, b in rows) + "</div>")


def option_c():
    bed = portrait_bed(lambda p, s: c_tile(p, s, s), 46, "lbl", gap=3, label_w=18)
    bed_view = (f'<div class="map"><span class="crumb">Jupiter</span>{bed}</div>' +
                c_sheet("Jupiter", "5 rows × 10 · 10 planted · watered 2 h ago", ["💧 Water", "🌿 Feed", "🪵 Mulch"]))
    # Row A: a vertical strip on screen; Row B peeks in from the right.
    strip = "".join(c_tile(f"A{n}", 118, 62) for n in range(1, 8))
    peek = "".join(c_tile(f"B{n}", 40, 62, ";opacity:.35") for n in range(1, 8))
    row_view = ('<div class="map" style="gap:5px;padding-top:40px"><span class="crumb">Jupiter › Row A</span>'
                '<div class="t edge" style="width:40px;height:470px"></div>'
                f'<div style="display:flex;flex-direction:column;gap:5px">{strip}</div>'
                f'<div style="display:flex;flex-direction:column;gap:5px">{peek}</div></div>' +
                c_sheet("Row A", "6 of 10 planted · ▼ scroll for A8–A10 · tap Row B at the right to move",
                        ["💧 Water", "🌿 Feed", "🧺 Harvest"], [("💧 Watered A1–A6", "2 h ago")]))
    # Column 3: a horizontal line on screen; Columns 2 and 4 peek in above and below.
    above = "".join(c_tile(f"{l}2", 64, 34, ";opacity:.35") for l in LETTERS)
    line = "".join(c_tile(f"{l}3", 64, 96) for l in LETTERS)
    below = "".join(c_tile(f"{l}4", 64, 34, ";opacity:.35") for l in LETTERS)
    col_view = ('<div class="map" style="flex-direction:column;gap:5px;padding-top:44px"><span class="crumb">Jupiter › Column 3</span>'
                f'<div style="display:flex;gap:5px">{above}</div><div style="display:flex;gap:5px">{line}</div>'
                f'<div style="display:flex;gap:5px">{below}</div></div>' +
                c_sheet("Column 3", "2 of 5 planted · tap Column 2 above or Column 4 below to move", ["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    # Cell A3: its on-screen neighborhood. Row A is the first letter, so the left side is past the bed's edge.
    hood = neighborhood(0, 3)
    tiles = []
    for r, line_ in enumerate(hood):
        for c, p in enumerate(line_):
            w = 180 if c == 1 else 76
            h = 180 if r == 1 else 76
            tiles.append(c_tile(p, w, h, "" if (r, c) == (1, 1) else ";opacity:.55"))
    cell_view = ('<div class="map" style="padding-top:44px"><span class="crumb">Jupiter › Row A › A3</span>'
                 f'<div style="display:grid;grid-template-columns:76px 180px 76px;grid-template-rows:76px 180px 76px;gap:6px">{"".join(tiles)}</div></div>' +
                 c_sheet("🍅 Tomato · Dark Galaxy", "A3 · tap a neighbor to step there: A2 above, A4 below, B3 right; hatched = bed's edge",
                         ["💧 Water", "🌿 Feed", "🧺 Harvest"], [(f"{i} {w} · {d}", t) for i, w, d, t in HISTORY[:3]]))
    return page("C · Neighborhood (portrait)",
                "The map on top zooms with the focus, with the bed rotated for a tall screen; details live in a bottom sheet. Neighboring rows and columns peek in where they really are, and in the cell view the 8 neighbors are the moves.",
                C_CSS, [phone("Bed", bed_view, "c-ui"), phone("Row A", row_view, "c-ui"), phone("Column 3", col_view, "c-ui"), phone("Cell A3", cell_view, "c-ui")])



# ================================================================ LANDSCAPE
# The bed keeps its natural orientation: row letters DOWN the side, column numbers ACROSS the top.
# A row is a horizontal strip (Row B below), a column a vertical line (Column 4 to the right).
# A3's on-screen neighbors: A2 left, A4 right, B3 below, nothing above row A (the bed's edge).
L_CSS = """
.land { width: 844px; height: 390px; flex-direction: row; }
.side { width: 280px; padding: 12px 14px; box-sizing: border-box; display: flex; flex-direction: column; gap: 6px; overflow: hidden; }
.view { flex: 1; display: flex; align-items: center; justify-content: center; position: relative; }
.grid2 { display: grid; grid-template-columns: repeat(2, 844px); gap: 26px 32px; }
"""


def land_bed(cell_fn, size, label_class, gap=3, label_w=18):
    html = [f'<div class="bedgrid" style="display:grid;gap:{gap}px;grid-template-columns:{label_w}px repeat(10,{size}px)"><div></div>']
    html += [f'<div class="{label_class}">{n}</div>' for n in NUMBERS]
    for l in LETTERS:
        html.append(f'<div class="{label_class}">{l}</div>')
        html += [cell_fn(f"{l}{n}", size) for n in NUMBERS]
    html.append("</div>")
    return "".join(html)


def land_hood(letter_index, number):
    """3x3 on-screen neighborhood in landscape: rows are letters (up = earlier), columns are numbers (left = smaller)."""
    return [[at(letter_index + dl, number + dn) for dn in (-1, 0, 1)] for dl in (-1, 0, 1)]


def land_page(name, subtitle, css, phones):
    return page(name, subtitle, css + L_CSS, ['<div class="grid2">' + "".join(phones) + "</div>"])


def a_side(crumbs, title, note, actions, extra=""):
    return (f'<div class="side" style="background:#3b2a1c">{crumbs}<div style="font-size:18px;font-weight:700">{title}</div>'
            f'<div class="note" style="text-align:left;margin:0">{note}</div>{extra}'
            '<div style="margin-top:auto;display:grid;grid-template-columns:1fr 1fr;gap:6px">' +
            "".join(f'<div style="background:#2E7D32;color:#fff;border-radius:10px;padding:9px 0;text-align:center;font-size:14px">{a}</div>' for a in actions) +
            "</div></div>")


def option_a_land():
    crumbs = lambda *p: a_crumbs(*p).replace("padding: 16px 14px 8px", "padding:0")
    bed = (f'<div class="view">{land_bed(a_cell, 50, "lbl")}</div>' +
           a_side(crumbs("Jupiter"), "Jupiter", "10 of 50 planted · tap a letter, number or cell", ["💧 Water", "🌿 Feed", "🪵 Mulch", "🌱 Plant"]))
    strip = "".join(a_cell(f"A{n}", 68) for n in range(1, 8))
    row = ('<div class="view" style="flex-direction:column;gap:12px">'
           f'<div style="display:flex;gap:4px">{strip}</div><div class="note" style="margin:0">◀ A1–A10 · swipe ▶</div>'
           '<div class="edge" style="width:160px;height:48px;font-size:17px">↓ Row B</div></div>' +
           a_side(crumbs("Jupiter", "Row A"), "Row A", "6 of 10 planted · 🍅 3 · 🥬 3", ["💧 Water", "🌿 Feed", "🪵 Mulch", "🧺 Harvest"]))
    line = "".join(a_cell(f"{l}3", 62) for l in LETTERS)
    col = ('<div class="view" style="gap:14px"><div class="edge" style="width:110px;height:48px;font-size:16px">← Column 2</div>'
           f'<div style="display:flex;flex-direction:column;gap:4px">{line}</div>'
           '<div class="edge" style="width:110px;height:48px;font-size:16px">Column 4 →</div></div>' +
           a_side(crumbs("Jupiter", "Column 3"), "Column 3", "A3–E3 · 2 of 5 planted", ["💧 Water", "🌿 Feed", "🪵 Mulch", "🧺 Harvest"]))
    hood = land_hood(0, 3)
    glyph = [["↖", "↑", "↗"], ["←", "", "→"], ["↙", "↓", "↘"]]
    place = {(0, 0): (0, 0), (0, 1): (104, -4), (0, 2): (206, 0), (1, 0): (-6, 104), (1, 2): (214, 104), (2, 0): (0, 206), (2, 1): (104, 212), (2, 2): (206, 206)}
    arrows = "".join(f'<div class="edge" style="position:absolute;left:{x}px;top:{y}px;width:50px;height:50px">{glyph[r][c]}</div>'
                     for (r, c), (x, y) in place.items() if hood[r][c] is not None)
    hist = '<div class="hist" style="margin:0">' + "".join(f"<div><span>{i} {w}</span><span style='opacity:.7'>{t}</span></div>" for i, w, d, t in HISTORY[:3]) + "</div>"
    cell = (f'<div class="view"><div style="position:relative;width:264px;height:264px"><div style="position:absolute;left:32px;top:32px">{a_cell("A3", 200)}</div>{arrows}</div></div>' +
            a_side(crumbs("Jupiter", "Row A", "A3"), "Tomato · Dark Galaxy", "← A2 · → A4 · ↓ B3 · top edge = bed's edge", ["💧 Water", "🌿 Feed", "🪵 Mulch", "🧺 Harvest"], hist))
    return land_page("A · Zoomed soil (landscape)",
                     "Landscape keeps the bed's natural orientation: letters down the side, numbers across the top. A row is a horizontal strip (Row B below), a column a vertical line (Column 2 left, Column 4 right). Details and care move to a side panel.",
                     A_CSS, [phone("Bed", bed, "a land"), phone("Row A", row, "a land"), phone("Column 3", col, "a land"), phone("Cell A3", cell, "a land")])


def b_side(crumbs, title, body, actions):
    return (f'<div class="side" style="background:white;border-left:1px solid #e0d9c8"><div class="crumbs">{crumbs}</div>'
            f'<div class="title" style="font-size:20px">{title}</div>{body}'
            '<div style="margin-top:auto;display:flex;gap:6px">' +
            "".join(f'<div style="flex:1;background:#2f6b34;color:#fff;border-radius:10px;padding:10px 0;text-align:center;font-weight:700;font-size:13px">{a}</div>' for a in actions) +
            "</div></div>")


def option_b_land():
    legend = '<div class="meta">🍅 3 · 🥬 3 · 🥕 4 · 40 empty</div>'
    bed = (f'<div class="view">{land_bed(b_cell, 48, "lbl", gap=4)}</div>' +
           b_side("<b>Jupiter</b>", "Jupiter", legend, ["💧 Water", "🌿 Feed", "🪵 Mulch"]))
    tiles = "".join(
        f'<div class="tile" style="width:52px;height:74px;background:{TINT.get(PLANTS.get(f"A{n}", ("", "", ""))[1], "#e9e3d3")}">'
        f'<span style="font-size:26px">{PLANTS.get(f"A{n}", ("", "", ""))[0] or "·"}</span><span class="chip" style="margin-top:3px;font-size:11px">A{n}</span></div>'
        for n in NUMBERS)
    row = (f'<div class="view" style="flex-direction:column;gap:10px"><div style="display:flex;gap:4px">{tiles}</div>'
           '<span class="navbtn">↓ Row B</span></div>' +
           b_side("Jupiter › <b>Row A</b>", "Row A", b_card("A1") + b_card("A2"), ["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    ctiles = "".join(
        f'<div class="tile" style="width:120px;height:58px;flex-direction:row;gap:8px;background:{TINT.get(PLANTS.get(f"{l}3", ("", "", ""))[1], "#e9e3d3")}">'
        f'<span class="chip">{l}3</span><span style="font-size:26px">{PLANTS.get(f"{l}3", ("", "", ""))[0] or "·"}</span></div>' for l in LETTERS)
    col = ('<div class="view" style="gap:14px"><span class="navbtn">← Column 2</span>'
           f'<div style="display:flex;flex-direction:column;gap:5px">{ctiles}</div><span class="navbtn">Column 4 →</span></div>' +
           b_side("Jupiter › <b>Column 3</b>", "Column 3", b_card("A3") + b_card("C3"), ["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    hood = land_hood(0, 3)
    glyph = [["↖", "↑", "↗"], ["←", "", "→"], ["↙", "↓", "↘"]]
    spots = {(0, 0): (10, 10), (0, 1): (110, 0), (0, 2): (210, 10), (1, 0): (0, 110), (1, 2): (220, 110), (2, 0): (10, 210), (2, 1): (110, 220), (2, 2): (210, 210)}
    dirs = "".join(f'<div class="dir{"" if hood[r][c] else " off"}" style="left:{x}px;top:{y}px;width:46px;height:46px">{glyph[r][c]}</div>' for (r, c), (x, y) in spots.items())
    ring = f'<div class="ring" style="width:270px;height:270px"><div class="mid" style="left:60px;top:60px;width:150px;height:150px">🍅<span style="font-size:13px;font-weight:800">A3</span></div>{dirs}</div>'
    tl = '<div class="tl">' + "".join(f"<div><span>{i}</span><span style='flex:1'>{w}</span><span class='meta'>{t}</span></div>" for i, w, d, t in HISTORY[:4]) + "</div>"
    cell = (f'<div class="view">{ring}</div>' + b_side("Jupiter › Row A › <b>A3</b>", "Tomato · Dark Galaxy", tl, ["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    return land_page("B · Field notebook (landscape)",
                     "Landscape keeps the bed's natural orientation. A row is a line of tiles across (Row B below); a column is a stack (Columns 2 and 4 to the sides). Cards, history and care sit in a light side panel.",
                     B_CSS, [phone("Bed", bed, "b land"), phone("Row A", row, "b land"), phone("Column 3", col, "b land"), phone("Cell A3", cell, "b land")])


def c_side(title, meta, actions, rows=()):
    return ('<div class="side" style="background:#f4efe4;color:#22301f;border-radius:22px 0 0 22px">'
            f'<div class="h" style="font-size:19px">{title}</div><div class="m">{meta}</div>'
            '<div class="acts" style="margin:6px 0">' + "".join(f"<div>{a}</div>" for a in actions) + "</div>" +
            "".join(f'<div class="li"><span>{a}</span><span class="m">{b}</span></div>' for a, b in rows) + "</div>")


def option_c_land():
    bed = (f'<div class="view"><span class="crumb">Jupiter</span>{land_bed(lambda p, s: c_tile(p, s, s), 46, "lbl")}</div>' +
           c_side("Jupiter", "5 rows × 10 · 10 planted", ["💧 Water", "🌿 Feed", "🪵 Mulch"]))
    edge_top = '<div class="t edge" style="width:560px;height:30px"></div>'
    strip = "".join(c_tile(f"A{n}", 62, 104) for n in range(1, 9))
    peek = "".join(c_tile(f"B{n}", 62, 44, ";opacity:.35") for n in range(1, 9))
    row = ('<div class="view" style="flex-direction:column;gap:5px;padding-top:26px"><span class="crumb">Jupiter › Row A</span>'
           f'{edge_top}<div style="display:flex;gap:5px">{strip}</div><div style="display:flex;gap:5px">{peek}</div></div>' +
           c_side("Row A", "6 of 10 planted · swipe for A9–A10 · Row B below", ["💧 Water", "🌿 Feed", "🧺 Harvest"], [("💧 Watered A1–A6", "2 h ago")]))
    left = "".join(c_tile(f"{l}2", 44, 60, ";opacity:.35") for l in LETTERS)
    mid = "".join(c_tile(f"{l}3", 120, 60) for l in LETTERS)
    right = "".join(c_tile(f"{l}4", 44, 60, ";opacity:.35") for l in LETTERS)
    col = ('<div class="view" style="gap:5px;padding-top:24px"><span class="crumb">Jupiter › Column 3</span>'
           f'<div style="display:flex;flex-direction:column;gap:5px">{left}</div><div style="display:flex;flex-direction:column;gap:5px">{mid}</div>'
           f'<div style="display:flex;flex-direction:column;gap:5px">{right}</div></div>' +
           c_side("Column 3", "2 of 5 planted · Column 2 left, Column 4 right", ["💧 Water", "🌿 Feed", "🧺 Harvest"]))
    hood = land_hood(0, 3)
    tiles = []
    for r, line_ in enumerate(hood):
        for c, p in enumerate(line_):
            w = 170 if c == 1 else 90
            h = 170 if r == 1 else 70
            tiles.append(c_tile(p, w, h, "" if (r, c) == (1, 1) else ";opacity:.55"))
    cell = ('<div class="view" style="padding-top:22px"><span class="crumb">Jupiter › Row A › A3</span>'
            f'<div style="display:grid;grid-template-columns:90px 170px 90px;grid-template-rows:70px 170px 70px;gap:6px">{"".join(tiles)}</div></div>' +
            c_side("🍅 Tomato · Dark Galaxy", "A3 · A2 left, A4 right, B3 below; hatched = bed's edge", ["💧 Water", "🌿 Feed", "🧺 Harvest"],
                   [(f"{i} {w}", t) for i, w, d, t in HISTORY[:3]]))
    return land_page("C · Neighborhood (landscape)",
                     "Landscape keeps the bed's natural orientation; the map fills the left and the details become a side sheet. Row B peeks in below Row A, Columns 2 and 4 at the sides of Column 3, and A3's neighbors are A2 left, A4 right and B3 below.",
                     C_CSS, [phone("Bed", bed, "c-ui land"), phone("Row A", row, "c-ui land"), phone("Column 3", col, "c-ui land"), phone("Cell A3", cell, "c-ui land")])


for name, html in [("option-a-landscape", option_a_land()), ("option-b-landscape", option_b_land()), ("option-c-landscape", option_c_land())]:
    (OUT / f"{name}.html").write_text(html, encoding="utf-8")

for name, html in [("option-a", option_a()), ("option-b", option_b()), ("option-c", option_c())]:
    (OUT / f"{name}.html").write_text(html, encoding="utf-8")
print("written")
