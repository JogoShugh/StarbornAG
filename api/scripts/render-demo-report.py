#!/usr/bin/env python3
"""Renders the YAML recorded by demo-two-views.sh as a single HTML report.

Usage: scripts/render-demo-report.py [--artifact] [in.yaml] [out.html]
       (defaults: build/demo/two-views.yaml and the same name with .html, under api/)
       --artifact writes the page without the outer html/head/body, for publishing as an Artifact.

The YAML is embedded as is; the page parses it with js-yaml and highlights bodies with highlight.js,
both from cdnjs, so nothing beyond the standard library is needed here (the highlighting colors are
inline). Screenshots recorded in the YAML are referenced by their relative path, so keep the
screenshots/ folder beside the report.
"""
import base64
import pathlib
import sys

here = pathlib.Path(__file__).resolve().parent
args = [a for a in sys.argv[1:] if not a.startswith("--")]
source = pathlib.Path(args[0]) if args else here.parent / "build/demo/two-views.yaml"
target = pathlib.Path(args[1]) if len(args) > 1 else source.with_suffix(".html")

data = base64.b64encode(source.read_bytes()).decode("ascii")

HEAD = r"""<title>One Address, Two Views</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,600;12..96,800&family=IBM+Plex+Sans:wght@400;500;600&family=IBM+Plex+Mono:wght@400;500&display=swap" rel="stylesheet">
<script src="https://cdnjs.cloudflare.com/ajax/libs/js-yaml/4.1.0/js-yaml.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
<style>
/* Layout: a step list on the left; each step shows the agent's and the browser's answers side by
   side, with a phone screenshot of what a person sees at that address. Garden palette from the app. */
:root {
  --bg: #f6f2e8; --panel: #fffdf8; --panel-2: #f1ecdf; --ink: #1f2a1d; --muted: #5d5e4e; --rule: #e1d9c6;
  --agent: #0b6f83; --agent-bg: #dcefef; --browser: #93307f; --browser-bg: #f4e3ee;
  --ok: #2c7a37; --bad: #a8291f; --chip: #ebe4d3; --code: #faf7f0; --leaf: #2f6b34;
  --hl-key: #0b6f83; --hl-str: #2c7a37; --hl-num: #9a5b00; --hl-tag: #93307f; --hl-attr: #6b4f1d; --hl-dim: #8a8a78;
  --display: "Bricolage Grotesque", "Avenir Next", system-ui, sans-serif;
  --body: "IBM Plex Sans", "Segoe UI", system-ui, sans-serif;
  --mono: "IBM Plex Mono", ui-monospace, "SF Mono", Menlo, monospace;
}
@media (prefers-color-scheme: dark) { :root:not([data-theme="light"]) {
  --bg: #0f1d14; --panel: #15281c; --panel-2: #1a3123; --ink: #eef3e9; --muted: #a2b59d; --rule: #2a4433;
  --agent: #5cc8dc; --agent-bg: #143a40; --browser: #e58ad9; --browser-bg: #3a1d37;
  --ok: #7fd48c; --bad: #ff8a7a; --chip: #223b2b; --code: #0c1811; --leaf: #7fd48c;
  --hl-key: #5cc8dc; --hl-str: #9fe0a8; --hl-num: #f2c14e; --hl-tag: #e58ad9; --hl-attr: #e8c58a; --hl-dim: #7c8f78;
  color-scheme: dark;
} }
:root[data-theme="dark"] {
  --bg: #0f1d14; --panel: #15281c; --panel-2: #1a3123; --ink: #eef3e9; --muted: #a2b59d; --rule: #2a4433;
  --agent: #5cc8dc; --agent-bg: #143a40; --browser: #e58ad9; --browser-bg: #3a1d37;
  --ok: #7fd48c; --bad: #ff8a7a; --chip: #223b2b; --code: #0c1811; --leaf: #7fd48c;
  --hl-key: #5cc8dc; --hl-str: #9fe0a8; --hl-num: #f2c14e; --hl-tag: #e58ad9; --hl-attr: #e8c58a; --hl-dim: #7c8f78;
  color-scheme: dark;
}
* { box-sizing: border-box; }
body { margin: 0; background: var(--bg); color: var(--ink); font: 15px/1.55 var(--body); padding-inline: 16px; }
code, pre, .mono { font-family: var(--mono); font-size: 12.5px; }
header.top { max-width: 1480px; margin: 0 auto; padding-block: 40px 28px; display: flex; justify-content: space-between; gap: 24px; flex-wrap: wrap; align-items: end; }
header.top > div { min-width: 0; max-width: 820px; }
.eyebrow { font: 600 12px var(--body); letter-spacing: .12em; text-transform: uppercase; color: var(--leaf); margin: 0 0 6px; }
header.top h1 { margin: 0; font: 800 clamp(30px, 5vw, 52px)/1.02 var(--display); letter-spacing: -.02em; text-wrap: balance; }
header.top p.lede { margin: 12px 0 0; color: var(--muted); max-width: 66ch; }
.meta { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 16px; }
.chip { background: var(--chip); border-radius: 999px; padding: 2px 10px; font-size: 12px; white-space: nowrap; max-width: 100%; overflow: hidden; text-overflow: ellipsis; }
.who { font: 600 11px var(--body); letter-spacing: .1em; text-transform: uppercase; padding: 3px 9px; border-radius: 6px; }
.who.agent { color: var(--agent); background: var(--agent-bg); }
.who.browser { color: var(--browser); background: var(--browser-bg); }
.same { color: var(--ok); font-weight: 600; white-space: nowrap; font-size: 12px; }
button.theme { border: 1px solid var(--rule); background: var(--panel); color: var(--ink); border-radius: 999px; padding: 7px 14px; cursor: pointer; font: inherit; }
button:focus-visible, a:focus-visible { outline: 2px solid var(--leaf); outline-offset: 2px; }
.layout { max-width: 1480px; margin: 0 auto; padding-block: 0 80px; display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 28px; }
nav.toc { position: sticky; top: calc(env(safe-area-inset-top, 0px) + 16px); align-self: start; display: grid; gap: 2px; }
nav.toc a { text-decoration: none; color: var(--muted); padding: 6px 10px; border-radius: 8px; font-size: 13px; display: flex; gap: 8px; }
nav.toc a span { font-variant-numeric: tabular-nums; color: var(--leaf); min-width: 1.4em; }
nav.toc a:hover { background: var(--panel); color: var(--ink); }
section.step { margin-bottom: 48px; scroll-margin-top: 16px; }
section.step h2 { margin: 0; font: 600 23px/1.2 var(--display); text-wrap: balance; }
section.step .note { color: var(--muted); margin: 4px 0 16px; max-width: 70ch; }
.step-body { display: grid; grid-template-columns: minmax(0, 1fr) 250px; gap: 18px; align-items: start; }
.step-body.no-shot { grid-template-columns: minmax(0, 1fr); }
.pair { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 400px), 1fr)); gap: 16px; min-width: 0; }
figure.shot { margin: 0; position: sticky; top: 16px; }
figure.shot .phone { border: 8px solid var(--ink); border-radius: 28px; overflow: hidden; background: var(--panel); }
figure.shot img { display: block; width: 100%; height: auto; max-width: 100%; }
figure.shot figcaption { font-size: 12px; color: var(--muted); margin-top: 8px; }
figure.shot figcaption code { word-break: break-all; }
.card { background: var(--panel); border: 1px solid var(--rule); border-radius: 12px; overflow: hidden; min-width: 0; }
.card.agent { box-shadow: inset 0 3px 0 var(--agent); }
.card.browser { box-shadow: inset 0 3px 0 var(--browser); }
.card-head { padding: 12px 14px 10px; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; border-bottom: 1px solid var(--rule); }
.method { font-weight: 500; }
.path { word-break: break-all; min-width: 0; }
.status { margin-left: auto; font: 600 13px var(--mono); padding: 2px 8px; border-radius: 6px; }
.status.ok { color: var(--ok); background: var(--chip); }
.status.bad { color: var(--bad); background: var(--chip); }
.subhead { padding: 6px 14px; display: flex; gap: 6px; flex-wrap: wrap; border-bottom: 1px solid var(--rule); background: var(--panel-2); }
.tabs { display: flex; gap: 4px; padding: 8px 10px 0; flex-wrap: wrap; }
.tabs button { border: 0; background: transparent; color: var(--muted); font: inherit; font-size: 13px; font-weight: 600; padding: 6px 10px; border-radius: 8px 8px 0 0; cursor: pointer; }
.tabs button.on { color: var(--ink); background: var(--panel-2); }
.pane { display: none; padding: 12px 14px 14px; background: var(--panel-2); min-width: 0; }
.pane.on { display: block; }
.pane pre, .request pre { margin: 0; max-height: 440px; overflow: auto; border-radius: 8px; background: var(--code); border: 1px solid var(--rule); }
.pane pre code, .request pre code { display: block; padding: 12px; color: var(--ink); white-space: pre; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
td, th { text-align: left; padding: 5px 6px; border-bottom: 1px solid var(--rule); vertical-align: top; }
th { color: var(--muted); font-weight: 600; font-size: 11px; text-transform: uppercase; letter-spacing: .08em; }
td.rel { font-weight: 600; white-space: nowrap; }
td.mono { word-break: break-all; }
.group { margin-bottom: 12px; }
.group h4 { margin: 0 0 6px; font-size: 11px; color: var(--muted); text-transform: uppercase; letter-spacing: .1em; font-weight: 600; }
.facts, .taps { display: flex; flex-wrap: wrap; gap: 5px; }
.taps .chip.match { box-shadow: inset 0 0 0 1px var(--ok); }
.problem { border-left: 3px solid var(--bad); padding: 8px 12px; background: var(--panel); border-radius: 6px; }
.request { padding: 10px 14px; border-bottom: 1px solid var(--rule); display: grid; gap: 6px; }
.hljs-attr { color: var(--hl-key); } .hljs-string { color: var(--hl-str); } .hljs-number, .hljs-literal { color: var(--hl-num); }
.hljs-tag, .hljs-name { color: var(--hl-tag); } .hljs-attribute { color: var(--hl-attr); } .hljs-comment, .hljs-meta, .hljs-punctuation { color: var(--hl-dim); }
.hljs-keyword, .hljs-section { color: var(--hl-tag); }
@media (max-width: 1100px) { .step-body { grid-template-columns: minmax(0, 1fr); } figure.shot { position: static; max-width: 300px; } }
@media (max-width: 860px) { .layout { grid-template-columns: minmax(0, 1fr); } nav.toc { display: none; } }
@media (prefers-reduced-motion: reduce) { * { scroll-behavior: auto !important; } }
</style>
"""

BODY = r"""<header class="top">
  <div>
    <p class="eyebrow">StarbornAG · content negotiation</p>
    <h1>One address, two views</h1>
    <p class="lede">Each address in the garden was asked twice: once as an <span class="who agent">agent</span> that
      reads HAL Schema Forms, and once as a <span class="who browser">browser</span> that gets the page or an htmx
      partial. <span class="same">✓ same</span> marks a move or a form that the other view offers at the same address.
      Each step also shows a phone screenshot of the page at that moment.</p>
    <div class="meta" id="meta"></div>
  </div>
  <button class="theme" id="theme" type="button">Switch light / dark</button>
</header>
<div class="layout">
  <nav class="toc" id="toc" aria-label="Steps"></nav>
  <main id="steps"></main>
</div>
<script id="data" type="application/octet-stream">__DATA__</script>
<script>
(function () {
  const root = document.documentElement;
  document.getElementById('theme').onclick = () => {
    const dark = root.dataset.theme === 'dark' ||
      (!root.dataset.theme && matchMedia('(prefers-color-scheme: dark)').matches);
    root.dataset.theme = dark ? 'light' : 'dark';
  };

  const bytes = Uint8Array.from(atob(document.getElementById('data').textContent.trim()), c => c.charCodeAt(0));
  const report = jsyaml.load(new TextDecoder().decode(bytes));
  const bed = report.bed;
  const short = s => (s || '').split(bed).join('{bed}');
  const esc = s => String(s ?? '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);
  const el = (html) => { const t = document.createElement('template'); t.innerHTML = html.trim(); return t.content.firstElementChild; };

  document.getElementById('meta').innerHTML = [
    ['base', report.base], ['bed', report.bedName], ['id', report.bed], ['captured', report.captured],
    ['steps', report.steps.length], ['exchanges', report.steps.reduce((n, s) => n + s.exchanges.length, 0)]
  ].map(([k, v]) => `<span class="chip"><b>${k}</b> ${esc(v)}</span>`).join('');

  const contentType = r => (r.headers['Content-Type'] || '').split(';')[0];

  /** The same resource under both spellings: the bed is its whole-bed focus, and the page-only
      journal size (and the default fold, by cell) do not change what is addressed. */
  const same = href => (href || '')
    .replace(/\/focus\/bed$/, '')
    .replace(/[?&]size=[^&]*/g, '').replace(/[?&]by=cell(?=&|$)/g, '')
    .replace(/journal&/, 'journal?');

  /** What the agent is offered: links, forms, embedded items and plain facts. */
  function readHal(doc) {
    const links = Object.entries(doc._links || {}).map(([rel, l]) => ({ rel, href: short(l.href), title: l.title }));
    const forms = Object.entries(doc._forms || {}).map(([id, f]) => {
      const props = f.schema.properties || {};
      const needs = (f.schema.required || []).filter(p => !(props[p] && props[p].readOnly));
      const choices = needs.filter(p => props[p] && props[p].enum).map(p => `${p} ∈ {${props[p].enum.join(', ')}}`);
      const places = f.method === 'GET' && f._links.target.templated ? expansions(f).map(short) : [];
      // The address the form posts to once its fixed fields (const) fill the template.
      const resolved = short(f._links.target.href.replace(/\{(\w+)\}/g, (m, n) =>
        props[n] && props[n].const !== undefined ? String(props[n].const) : m));
      return { id, method: f.method, target: short(f._links.target.href), resolved, at: (props.location || {}).const, needs,
               choices, title: f.schema.title, places };
    });
    const facts = Object.entries(doc).filter(([k, v]) => !k.startsWith('_') && v !== null && typeof v !== 'object' && !['bedId', 'id'].includes(k));
    return { links, forms, facts, embedded: doc._embedded || {}, doc };
  }

  /** What the person can do on the page: places to tap and care buttons, read from the htmx attributes. */
  function readHtml(text) {
    const page = new DOMParser().parseFromString(text, 'text/html');
    const whole = /^\s*<html/i.test(text);
    const view = page.querySelector('#view') || page.body;
    const taps = [...new Set([...view.querySelectorAll('[hx-get]')].map(e => short(e.getAttribute('hx-get'))))];
    const buttons = [...view.querySelectorAll('form[hx-post]')].map(f => ({
      action: f.dataset.action, target: short(f.getAttribute('hx-post')),
      asks: [...f.querySelectorAll('input[name], select[name]')].map(i => i.name)
    }));
    const text_ = sel => [...view.querySelectorAll(sel)].map(e => e.textContent.trim()).filter(Boolean);
    const beds = [...page.querySelectorAll('a.bed-link')].map(a => ({ name: a.textContent.trim(), href: short(a.getAttribute('href')) }));
    return { whole, taps, buttons, crumbs: text_('.crumbs .crumb'), title: text_('.sheet-title')[0],
             meta: text_('.sheet-meta')[0], message: text_('.sheet-message')[0], beds };
  }

  /** A vnd.error (or an older problem+json): the message, what it is about, and each field to fix. */
  function problemSummary(p) {
    const about = p._links && p._links.about ? short(p._links.about.href) : null;
    const fields = (p._embedded && p._embedded.errors) || [];
    return `<div class="problem"><b>${esc(p.message || p.title)}</b>${p.detail ? ': ' + esc(p.detail) : ''}</div>` +
      (about ? `<div class="group"><h4>About</h4><span class="chip mono">${esc(about)}</span></div>` : '') +
      (fields.length ? `<div class="group"><h4>Fields to fix</h4><div class="table-wrap"><table>${
        fields.map(f => `<tr><td class="mono rel">${esc(f.path)}</td><td>${esc(f.message)}</td></tr>`).join('')}</table></div></div>` : '');
  }

  /** Every address a bounded GET form leads to, filling its template from each field's enum or range. */
  function expansions(form) {
    const props = form.schema.properties || {};
    const choices = Object.entries(props).map(([name, p]) => [name,
      p.const !== undefined ? [String(p.const)] : p.enum ? p.enum.map(String) : (p.minimum !== undefined && p.maximum !== undefined)
        ? Array.from({ length: p.maximum - p.minimum + 1 }, (_, i) => String(p.minimum + i)) : null]);
    if (choices.some(([, v]) => !v)) return [];
    let sets = [{}];
    for (const [name, values] of choices) sets = sets.flatMap(set => values.map(v => ({ ...set, [name]: v })));
    return sets.map(set => form._links.target.href.replace(/\{(\??)([\w,]+)\}/g, (_, q, names) =>
      q ? '' : names.split(',').map(n => set[n] || '').join('')));
  }

  function halSummary(hal, other) {
    const otherTargets = new Set((other?.buttons || []).map(b => same(b.target)));
    const otherTaps = new Set((other?.taps || []).map(same));
    const parts = [];
    if (hal.facts.length) parts.push(`<div class="group"><h4>About</h4><div class="facts">${
      hal.facts.map(([k, v]) => `<span class="chip"><b>${esc(k)}</b> ${esc(v)}</span>`).join('')}</div></div>`);
    if (hal.links.length) parts.push(`<div class="group"><h4>Links</h4><div class="table-wrap"><table><tr><th>rel</th><th>href</th><th></th></tr>${
      hal.links.map(l => `<tr><td class="rel">${esc(l.rel)}</td><td class="mono">${esc(l.href)}</td><td>${
        other && otherTaps.has(same(l.href)) ? '<span class="same">✓ same</span>' : ''}</td></tr>`).join('')}</table></div></div>`);
    if (hal.forms.length) parts.push(`<div class="group"><h4>Forms</h4><div class="table-wrap"><table><tr><th>form</th><th>posts to</th><th>needs</th><th></th></tr>${
      hal.forms.map(f => `<tr><td class="rel">${esc(f.id)}</td><td class="mono">${esc(f.method)} ${esc(f.target)}${
        f.at ? ` <span class="chip">at ${esc(f.at)}</span>` : ''}</td><td>${esc(f.needs.join(', ') || '—')}${
        f.choices.length ? `<br><span class="mono">${esc(f.choices.join('  '))}</span>` : ''}</td><td>${
        other && (otherTargets.has(same(f.resolved)) || (f.places.length && f.places.every(p => otherTaps.has(same(p)))))
          ? `<span class="same">✓ same${f.places.length ? ` (${f.places.length} places)` : ''}</span>` : ''}</td></tr>`).join('')}</table></div></div>`);
    for (const [kind, items] of Object.entries(hal.embedded).filter(([, items]) => items.length)) {
      parts.push(`<div class="group"><h4>Embedded ${esc(kind)}</h4><div class="table-wrap"><table>${items.map(i => i.type
        ? `<tr><td class="rel">${esc(i.type)}</td><td class="mono">${esc(i.cells.join(' '))}</td><td>${esc(new Date(i.started).toLocaleTimeString())}</td></tr>`
        : `<tr><td class="rel">${esc(i.name)}</td><td>${i.rows} × ${i.columns}</td><td class="mono">${esc(short(i._links.self.href))}</td></tr>`).join('')}</table></div></div>`);
    }
    if (Array.isArray(hal.doc.lines) && hal.doc.lines.length) parts.push(`<div class="group"><h4>Lines</h4><div class="table-wrap"><table>${
      hal.doc.lines.map(l => `<tr><td class="rel">${esc(l.line)}</td><td>${esc(l.commands.map(c => `${c.type} ${c.cells.join(' ')}`).join(' · ') || '—')}</td></tr>`).join('')}</table></div></div>`);
    return parts.join('');
  }

  function htmlSummary(page, hal) {
    const forms = new Set((hal?.forms || []).map(f => same(f.resolved)));
    const links = new Set((hal?.links || []).map(l => same(l.href)));
    const parts = [`<div class="group"><div class="facts"><span class="chip"><b>view</b> ${page.whole ? 'the whole page' : 'only #view (htmx partial)'}</span>${
      page.crumbs.length ? `<span class="chip"><b>crumbs</b> ${esc(page.crumbs.join(' › '))}</span>` : ''}${
      page.title ? `<span class="chip"><b>title</b> ${esc(page.title)}</span>` : ''}${
      page.meta ? `<span class="chip"><b>meta</b> ${esc(page.meta)}</span>` : ''}</div></div>`];
    if (page.message) parts.push(`<div class="problem">${esc(page.message)}</div>`);
    if (page.beds.length) parts.push(`<div class="group"><h4>Beds to pick</h4><div class="table-wrap"><table>${
      page.beds.map(b => `<tr><td class="rel">${esc(b.name)}</td><td class="mono">${esc(b.href)}</td></tr>`).join('')}</table></div></div>`);
    if (page.taps.length) parts.push(`<div class="group"><h4>Places to tap (${page.taps.length})</h4><div class="taps">${
      page.taps.map(t => `<span class="chip mono${links.has(same(t)) ? ' match' : ''}">${esc(t.includes('/focus/') ? t.split('/focus/')[1] : t)}</span>`).join('')}</div></div>`);
    if (page.buttons.length) parts.push(`<div class="group"><h4>Care buttons</h4><div class="table-wrap"><table><tr><th>button</th><th>posts to</th><th>asks</th><th></th></tr>${
      page.buttons.map(b => `<tr><td class="rel">${esc(b.action)}</td><td class="mono">POST ${esc(b.target)}</td><td>${esc(b.asks.join(', ') || '—')}</td><td>${
        hal && forms.has(same(b.target)) ? '<span class="same">✓ same</span>' : ''}</td></tr>`).join('')}</table></div></div>`);
    return parts.join('');
  }

  function code(text, language) {
    const pre = el('<pre><code></code></pre>');
    const c = pre.firstChild; c.textContent = text; if (language) c.className = 'language-' + language;
    hljs.highlightElement(c);
    return pre;
  }

  function card(exchange, summaryHtml) {
    const r = exchange.response, q = exchange.request, type = contentType(r);
    const node = el(`<article class="card ${exchange.who}">
      <div class="card-head"><span class="who ${exchange.who}">${exchange.who}</span>
        <span class="method mono">${esc(q.method)}</span><span class="path mono">${esc(short(q.path))}</span>
        <span class="status ${r.status < 400 ? 'ok' : 'bad'}">${r.status}</span></div>
      <div class="subhead"><span class="chip mono">${esc(type)}</span>${
        r.headers.Vary ? `<span class="chip mono">Vary: ${esc(r.headers.Vary)}</span>` : ''}${
        q.headers['HX-Request'] ? '<span class="chip mono">HX-Request: true</span>' : ''}</div>
    </article>`);
    if (q.body) {
      const req = el(`<div class="request"><span class="chip mono">sent ${esc(q.headers['Content-Type'] || '')}</span></div>`);
      req.appendChild(code(q.body, q.body.trim().startsWith('{') ? 'json' : 'plaintext'));
      node.appendChild(req);
    }
    const tabs = el('<div class="tabs"></div>'); node.appendChild(tabs);
    const panes = [];
    const add = (name, content) => {
      const b = el(`<button>${name}</button>`), p = el('<div class="pane"></div>');
      if (typeof content === 'string') p.innerHTML = content; else p.appendChild(content);
      b.onclick = () => { tabs.querySelectorAll('button').forEach(x => x.classList.remove('on')); panes.forEach(x => x.classList.remove('on')); b.classList.add('on'); p.classList.add('on'); };
      tabs.appendChild(b); node.appendChild(p); panes.push(p);
      if (panes.length === 1) b.onclick();
    };
    if (summaryHtml) add('Summary', summaryHtml);
    add('Body', code(r.body, type.includes('json') ? 'json' : type.includes('html') ? 'xml' : 'plaintext'));
    add('Headers', code(Object.entries(r.headers).map(([k, v]) => `${k}: ${v}`).join('\n'), 'http'));
    return node;
  }

  const toc = document.getElementById('toc'), steps = document.getElementById('steps');
  report.steps.forEach((step, i) => {
    const id = 'step-' + (i + 1);
    const [number, ...words] = step.title.split(' ');
    toc.appendChild(el(`<a href="#${id}"><span>${esc(number.replace('.', ''))}</span>${esc(words.join(' '))}</a>`));
    const shot = step.screenshot;
    const section = el(`<section class="step" id="${id}"><h2>${esc(step.title)}</h2><p class="note">${esc(step.note)}</p>
      <div class="step-body${shot ? '' : ' no-shot'}"><div class="pair"></div>${shot ? `<figure class="shot">
        <div class="phone"><img src="${esc(shot.file)}" alt="The bed page at ${esc(short(shot.of))}, as a phone shows it" loading="lazy" width="500" height="900"></div>
        <figcaption>What a person sees at <code>${esc(short(shot.of))}</code> at this point.</figcaption></figure>` : ''}</div></section>`);
    const read = step.exchanges.map(x => {
      const type = contentType(x.response);
      if (type === 'application/problem+json' || type === 'application/vnd.error+json') return { problem: JSON.parse(x.response.body) };
      if (type.includes('json')) return { hal: readHal(JSON.parse(x.response.body)) };
      if (type.includes('html')) return { page: readHtml(x.response.body) };
      return {};
    });
    const agentRead = read.find((r, j) => r.hal && step.exchanges[j].who === 'agent');
    const browserRead = read.find((r, j) => r.page && step.exchanges[j].who === 'browser');
    step.exchanges.forEach((x, j) => {
      const r = read[j];
      const summary = r.problem ? problemSummary(r.problem)
        : r.hal ? halSummary(r.hal, browserRead && browserRead.page)
        : r.page ? htmlSummary(r.page, agentRead && agentRead.hal) : '';
      section.querySelector('.pair').appendChild(card(x, summary));
    });
    steps.appendChild(section);
  });
})();
</script>
"""

artifact = "--artifact" in sys.argv
page = HEAD + BODY.replace("__DATA__", data)
if not artifact:
    page = "<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n" \
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, viewport-fit=cover\">\n" \
        + HEAD + "</head>\n<body>\n" + BODY.replace("__DATA__", data) + "</body>\n</html>\n"
target.write_text(page, encoding="utf-8")
print(f"Wrote {target}")
