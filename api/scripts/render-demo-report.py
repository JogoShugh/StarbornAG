#!/usr/bin/env python3
"""Renders the YAML recorded by demo-two-views.sh as a single HTML report.

Usage: scripts/render-demo-report.py [in.yaml] [out.html]
       (defaults: build/demo/two-views.yaml and the same name with .html, under api/)

The YAML is embedded as is; the page parses it with js-yaml and highlights bodies with highlight.js,
both from a CDN, so nothing beyond the standard library is needed here. Opening the report needs a
network connection for those libraries; the Preview tabs also need the app running at the recorded
base address, for the page's stylesheet.
"""
import base64
import pathlib
import sys

here = pathlib.Path(__file__).resolve().parent
source = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else here.parent / "build/demo/two-views.yaml"
target = pathlib.Path(sys.argv[2]) if len(sys.argv) > 2 else source.with_suffix(".html")

data = base64.b64encode(source.read_bytes()).decode("ascii")

PAGE = r"""<!doctype html>
<html lang="en" data-theme="auto">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>One address, two views</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600&display=swap" rel="stylesheet">
<link id="hl-dark" rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github-dark.min.css">
<link id="hl-light" rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css" disabled>
<script src="https://cdnjs.cloudflare.com/ajax/libs/js-yaml/4.1.0/js-yaml.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
<style>
:root {
  --bg: #0f1d14; --panel: #15281c; --panel-2: #1b3324; --ink: #eef3e9; --muted: #9fb39a; --rule: #2a4433;
  --agent: #4fc3d9; --agent-bg: rgba(79,195,217,.12); --browser: #e07ad6; --browser-bg: rgba(224,122,214,.12);
  --ok: #6fcf7f; --bad: #ff7b6b; --warn: #f2c14e; --code: #0b1610; --chip: #223b2b;
}
:root[data-theme="light"] {
  --bg: #f6f2e8; --panel: #ffffff; --panel-2: #f4efe4; --ink: #1f2a1d; --muted: #5b5b4d; --rule: #e0d9c8;
  --agent: #0b7f95; --agent-bg: rgba(11,127,149,.08); --browser: #a3329a; --browser-bg: rgba(163,50,154,.08);
  --ok: #2f7d3a; --bad: #b3261e; --warn: #9a6b00; --code: #fbf9f4; --chip: #ece6d6;
}
@media (prefers-color-scheme: light) {
  :root[data-theme="auto"] {
    --bg: #f6f2e8; --panel: #ffffff; --panel-2: #f4efe4; --ink: #1f2a1d; --muted: #5b5b4d; --rule: #e0d9c8;
    --agent: #0b7f95; --agent-bg: rgba(11,127,149,.08); --browser: #a3329a; --browser-bg: rgba(163,50,154,.08);
    --ok: #2f7d3a; --bad: #b3261e; --warn: #9a6b00; --code: #fbf9f4; --chip: #ece6d6;
  }
}
* { box-sizing: border-box; }
body { margin: 0; background: var(--bg); color: var(--ink); font: 15px/1.5 Inter, system-ui, sans-serif; }
code, pre, .mono { font-family: "JetBrains Mono", ui-monospace, monospace; font-size: 12.5px; }
a { color: inherit; }
header.top { padding: 40px 24px 24px; max-width: 1500px; margin: 0 auto; display: flex; justify-content: space-between; gap: 24px; flex-wrap: wrap; align-items: end; }
header.top h1 { margin: 0; font-size: 34px; letter-spacing: -.02em; }
header.top p { margin: 6px 0 0; color: var(--muted); max-width: 760px; }
.meta { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 14px; }
.chip { background: var(--chip); border-radius: 999px; padding: 3px 10px; font-size: 12px; white-space: nowrap; }
.legend { display: flex; gap: 10px; align-items: center; }
.who { font-weight: 800; font-size: 11px; letter-spacing: .08em; text-transform: uppercase; padding: 3px 9px; border-radius: 6px; }
.who.agent { color: var(--agent); background: var(--agent-bg); }
.who.browser { color: var(--browser); background: var(--browser-bg); }
button.theme { border: 1px solid var(--rule); background: var(--panel); color: var(--ink); border-radius: 999px; padding: 6px 14px; cursor: pointer; font: inherit; }
.layout { max-width: 1500px; margin: 0 auto; padding: 0 24px 80px; display: grid; grid-template-columns: 240px minmax(0, 1fr); gap: 28px; }
nav.toc { position: sticky; top: 16px; align-self: start; display: grid; gap: 2px; }
nav.toc a { text-decoration: none; color: var(--muted); padding: 6px 10px; border-radius: 8px; font-size: 13px; }
nav.toc a:hover { background: var(--panel); color: var(--ink); }
section.step { margin-bottom: 40px; scroll-margin-top: 16px; }
section.step h2 { margin: 0; font-size: 21px; }
section.step .note { color: var(--muted); margin: 4px 0 14px; }
.pair { display: grid; grid-template-columns: repeat(auto-fit, minmax(440px, 1fr)); gap: 16px; }
.card { background: var(--panel); border: 1px solid var(--rule); border-radius: 14px; overflow: hidden; min-width: 0; }
.card.agent { border-top: 3px solid var(--agent); }
.card.browser { border-top: 3px solid var(--browser); }
.card-head { padding: 12px 14px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; border-bottom: 1px solid var(--rule); }
.method { font-weight: 700; }
.path { word-break: break-all; }
.status { margin-left: auto; font-weight: 800; padding: 2px 9px; border-radius: 6px; }
.status.ok { color: var(--ok); background: color-mix(in srgb, var(--ok) 14%, transparent); }
.status.bad { color: var(--bad); background: color-mix(in srgb, var(--bad) 14%, transparent); }
.subhead { padding: 6px 14px; display: flex; gap: 6px; flex-wrap: wrap; border-bottom: 1px solid var(--rule); background: var(--panel-2); }
.tabs { display: flex; gap: 4px; padding: 8px 10px 0; }
.tabs button { border: 0; background: transparent; color: var(--muted); font: inherit; font-size: 13px; font-weight: 600; padding: 6px 10px; border-radius: 8px 8px 0 0; cursor: pointer; }
.tabs button.on { color: var(--ink); background: var(--panel-2); }
.pane { display: none; padding: 12px 14px 14px; background: var(--panel-2); }
.pane.on { display: block; }
.pane pre { margin: 0; max-height: 460px; overflow: auto; border-radius: 10px; }
.pane pre code.hljs { background: var(--code); border-radius: 10px; padding: 12px; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
td, th { text-align: left; padding: 5px 6px; border-bottom: 1px solid var(--rule); vertical-align: top; }
th { color: var(--muted); font-weight: 600; font-size: 11px; text-transform: uppercase; letter-spacing: .06em; }
td.rel { font-weight: 600; white-space: nowrap; }
.same { color: var(--ok); font-weight: 800; white-space: nowrap; font-size: 12px; }
.group { margin-bottom: 12px; }
.group h4 { margin: 0 0 6px; font-size: 11px; color: var(--muted); text-transform: uppercase; letter-spacing: .08em; }
.facts { display: flex; flex-wrap: wrap; gap: 6px; }
.facts .chip b { font-weight: 700; }
.taps { display: flex; flex-wrap: wrap; gap: 5px; }
.taps .chip.match { outline: 1px solid var(--ok); }
.problem { border-left: 3px solid var(--bad); padding: 8px 12px; background: color-mix(in srgb, var(--bad) 10%, transparent); border-radius: 6px; }
.request { padding: 10px 14px; border-bottom: 1px solid var(--rule); }
.request pre { margin: 6px 0 0; }
iframe.preview { width: 100%; height: 520px; border: 1px solid var(--rule); border-radius: 10px; background: white; }
.hint { color: var(--muted); font-size: 12px; margin: 0 0 8px; }
@media (max-width: 900px) { .layout { grid-template-columns: 1fr; } nav.toc { display: none; } .pair { grid-template-columns: 1fr; } }
</style>
</head>
<body>
<header class="top">
  <div>
    <h1 id="title">One address, two views</h1>
    <p>Every address asked twice: once as an <span class="who agent">agent</span> getting HAL Schema Forms,
      once as a <span class="who browser">browser</span> getting the page or an htmx partial.
      A <span class="same">✓ same</span> marks a form or move the other view offers at the same address.</p>
    <div class="meta" id="meta"></div>
  </div>
  <button class="theme" id="theme">◐ Theme</button>
</header>
<div class="layout">
  <nav class="toc" id="toc"></nav>
  <main id="steps"></main>
</div>
<script id="data" type="application/octet-stream">__DATA__</script>
<script>
(function () {
  const root = document.documentElement;
  const syncHighlightTheme = () => {
    const light = root.dataset.theme === 'light' ||
      (root.dataset.theme === 'auto' && matchMedia('(prefers-color-scheme: light)').matches);
    document.getElementById('hl-light').disabled = !light;
    document.getElementById('hl-dark').disabled = light;
  };
  document.getElementById('theme').onclick = () => {
    const light = root.dataset.theme === 'light' ||
      (root.dataset.theme === 'auto' && matchMedia('(prefers-color-scheme: light)').matches);
    root.dataset.theme = light ? 'dark' : 'light';
    syncHighlightTheme();
  };
  syncHighlightTheme();

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
      const needs = (f.schema.required || []).filter(p => !['bedId', 'started', 'location'].includes(p));
      const choices = needs.filter(p => props[p] && props[p].enum).map(p => `${p} ∈ {${props[p].enum.join(', ')}}`);
      return { id, method: f.method, target: short(f._links.target.href), at: (props.location || {}).const, needs, choices };
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

  function halSummary(hal, other) {
    const otherTargets = new Set((other?.buttons || []).map(b => same(b.target)));
    const otherTaps = new Set((other?.taps || []).map(same));
    const parts = [];
    if (hal.facts.length) parts.push(`<div class="group"><h4>About</h4><div class="facts">${
      hal.facts.map(([k, v]) => `<span class="chip"><b>${esc(k)}</b> ${esc(v)}</span>`).join('')}</div></div>`);
    if (hal.links.length) parts.push(`<div class="group"><h4>Links</h4><table><tr><th>rel</th><th>href</th><th></th></tr>${
      hal.links.map(l => `<tr><td class="rel">${esc(l.rel)}</td><td class="mono">${esc(l.href)}</td><td>${
        other && otherTaps.has(same(l.href)) ? '<span class="same">✓ same</span>' : ''}</td></tr>`).join('')}</table></div>`);
    if (hal.forms.length) parts.push(`<div class="group"><h4>Forms</h4><table><tr><th>form</th><th>posts to</th><th>needs</th><th></th></tr>${
      hal.forms.map(f => `<tr><td class="rel">${esc(f.id)}</td><td class="mono">${esc(f.method)} ${esc(f.target)}${
        f.at ? ` <span class="chip">at ${esc(f.at)}</span>` : ''}</td><td>${esc(f.needs.join(', ') || '—')}${
        f.choices.length ? `<br><span class="mono">${esc(f.choices.join('  '))}</span>` : ''}</td><td>${
        other && otherTargets.has(same(f.target)) ? '<span class="same">✓ same</span>' : ''}</td></tr>`).join('')}</table></div>`);
    for (const [kind, items] of Object.entries(hal.embedded).filter(([, items]) => items.length)) {
      parts.push(`<div class="group"><h4>Embedded ${esc(kind)}</h4><table>${items.map(i => i.type
        ? `<tr><td class="rel">${esc(i.type)}</td><td class="mono">${esc(i.cells.join(' '))}</td><td>${esc(new Date(i.started).toLocaleTimeString())}</td></tr>`
        : `<tr><td class="rel">${esc(i.name)}</td><td>${i.rows} × ${i.columns}</td><td class="mono">${esc(short(i._links.self.href))}</td></tr>`).join('')}</table></div>`);
    }
    if (Array.isArray(hal.doc.lines) && hal.doc.lines.length) parts.push(`<div class="group"><h4>Lines</h4><table>${
      hal.doc.lines.map(l => `<tr><td class="rel">${esc(l.line)}</td><td>${esc(l.commands.map(c => `${c.type} ${c.cells.join(' ')}`).join(' · ') || '—')}</td></tr>`).join('')}</table></div>`);
    return parts.join('');
  }

  function htmlSummary(page, hal) {
    const forms = new Set((hal?.forms || []).map(f => same(f.target)));
    const links = new Set((hal?.links || []).map(l => same(l.href)));
    const parts = [`<div class="group"><div class="facts"><span class="chip"><b>view</b> ${page.whole ? 'the whole page' : 'only #view (htmx partial)'}</span>${
      page.crumbs.length ? `<span class="chip"><b>crumbs</b> ${esc(page.crumbs.join(' › '))}</span>` : ''}${
      page.title ? `<span class="chip"><b>title</b> ${esc(page.title)}</span>` : ''}${
      page.meta ? `<span class="chip"><b>meta</b> ${esc(page.meta)}</span>` : ''}</div></div>`];
    if (page.message) parts.push(`<div class="problem">${esc(page.message)}</div>`);
    if (page.beds.length) parts.push(`<div class="group"><h4>Beds to pick</h4><table>${
      page.beds.map(b => `<tr><td class="rel">${esc(b.name)}</td><td class="mono">${esc(b.href)}</td></tr>`).join('')}</table></div>`);
    if (page.taps.length) parts.push(`<div class="group"><h4>Places to tap (${page.taps.length})</h4><div class="taps">${
      page.taps.map(t => `<span class="chip mono${links.has(same(t)) ? ' match' : ''}">${esc(t.includes('/focus/') ? t.split('/focus/')[1] : t)}</span>`).join('')}</div></div>`);
    if (page.buttons.length) parts.push(`<div class="group"><h4>Care buttons</h4><table><tr><th>button</th><th>posts to</th><th>asks</th><th></th></tr>${
      page.buttons.map(b => `<tr><td class="rel">${esc(b.action)}</td><td class="mono">POST ${esc(b.target)}</td><td>${esc(b.asks.join(', ') || '—')}</td><td>${
        hal && forms.has(same(b.target)) ? '<span class="same">✓ same</span>' : ''}</td></tr>`).join('')}</table></div>`);
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
    if (type.includes('html')) {
      const wrap = el(`<div><p class="hint">Rendered with the app's stylesheet from ${esc(report.base)} (the app must be running).</p></div>`);
      const frame = el('<iframe class="preview" sandbox=""></iframe>');
      frame.srcdoc = (r.body.trim().startsWith('<html') ? r.body : `<link rel="stylesheet" href="/styles.css"><body>${r.body}</body>`)
        .replace(/<head>/i, `<head><base href="${report.base}/">`).replace(/^<link/, `<base href="${report.base}/"><link`);
      wrap.appendChild(frame); add('Preview', wrap);
    }
    return node;
  }

  const toc = document.getElementById('toc'), steps = document.getElementById('steps');
  report.steps.forEach((step, i) => {
    const id = 'step-' + (i + 1);
    toc.appendChild(el(`<a href="#${id}">${esc(step.title)}</a>`));
    const section = el(`<section class="step" id="${id}"><h2>${esc(step.title)}</h2><p class="note">${esc(step.note)}</p><div class="pair"></div></section>`);
    const read = step.exchanges.map(x => {
      const type = contentType(x.response);
      if (type === 'application/problem+json') return { problem: JSON.parse(x.response.body) };
      if (type.includes('json')) return { hal: readHal(JSON.parse(x.response.body)) };
      if (type.includes('html')) return { page: readHtml(x.response.body) };
      return {};
    });
    const agentRead = read.find((r, j) => r.hal && step.exchanges[j].who === 'agent');
    const browserRead = read.find((r, j) => r.page && step.exchanges[j].who === 'browser');
    step.exchanges.forEach((x, j) => {
      const r = read[j];
      const summary = r.problem
        ? `<div class="problem"><b>${esc(r.problem.title)}</b> (${r.problem.status}): ${esc(r.problem.detail)}</div>`
        : r.hal ? halSummary(r.hal, browserRead && browserRead.page)
        : r.page ? htmlSummary(r.page, agentRead && agentRead.hal) : '';
      section.querySelector('.pair').appendChild(card(x, summary));
    });
    steps.appendChild(section);
  });
})();
</script>
</body>
</html>
"""

target.write_text(PAGE.replace("__DATA__", data), encoding="utf-8")
print(f"Wrote {target}")
