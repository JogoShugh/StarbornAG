# A HAL Schema Forms agent skill, adapted from `heart-client`

Source: `heart-gh/heart-claude/.claude/skills/heart-client/SKILL.md` (ARTIE, the H.E.A.R.T. reference
client). Result: the skill **`hal-schema-agent`** (`.claude/skills/hal-schema-agent/SKILL.md`), which
drives StarbornAG's API as it stands: HAL links for navigation, HAL Schema Forms (`_forms`,
github.com/jbadeau/hal-schema-forms) for actions. Everything H.E.A.R.T.-specific is left out for now.

Status (2026-10-01): the API gaps this plan found are fixed, each Gherkin-first (section 6), and the skill
is written. Next: proving it with a person driving it interactively (section 7).

## 1. What the API offers an agent

| Aspect | What it is |
|---|---|
| Entry point | `GET /` → `_links.beds` → `GET /beds` → `_embedded.beds[]` (each with `_links.self`) and `_forms.prepare-bed` (`POST /beds`, name and size only; the server chooses the id) |
| Media type | Ask with `Accept: application/hal+json` (or `application/json`, `*/*`, no Accept). The answer is `application/hal+json; profile="https://github.com/jbadeau/hal-schema-forms"`, plus a `Link: <…>; rel="profile"` header. `text/html` at the same address gets the human page. |
| Navigation | `_links`: `rel → {href, title}`. On a focus: `self`, `bed`, `journal`, compass moves, `zoom-out`. On a journal: `self`, `focus`, `by-cell`, `by-time` (plus `by-row`, `by-column` at the whole bed). |
| Going straight there | GET forms with templated targets (RFC 6570): `go-to-row`, `go-to-column`, `go-to-cell` on the whole bed, `go-to-cell` on a row or column, `refresh` (`{?recent}`) on every focus. Their schemas bound the values: rows by `enum`, columns by `minimum`/`maximum`. |
| Care | POST forms (`plant-seedling`, `water-cells`, `fertilize-cells`, `mulch-cells`, `harvest-crop`) present only when the action is possible now; `harvest-crop` lists what can be harvested as `enum`s. |
| One template for every bed | Every focus form targets a template such as `/beds/{bedId}/focus/cell/{row}{column}`. The bed (and a care form's cells) are fields fixed the JSON Schema way: `const`, `default`, `readOnly`. |
| Schemas | JSON Schema 2019-09, as the profile names. Every form has a `title`; every field a `title` and `description`, with units (liters, kilograms) and what happens when it is left out. |
| Embedded state | `_embedded.recent` (the latest commands at the focus, 10 unless `refresh` asks otherwise), `_embedded.beds` on `/beds`. |
| Action results | A focus form → `200` with the refreshed focus. `prepare-bed` → `201`, `Location: /beds/{id}`, the new bed in the body. |
| Errors | `application/vnd.error+json`: `message`, `_embedded.errors[]` with a JSON pointer `path` per field, `_links.about` to the resource to re-read. `400` (fields), `404` (no such bed or focus, about the bed), `409` (the rules refuse, in the garden's words, for example "A1, A2 and A3 are already planted", about the focus). |

## 2. Keep, change, drop

| `heart-client` part | Decision | Why / how |
|---|---|---|
| Who you are: no domain knowledge | **Kept** | The core discipline. |
| Tools: `curl -s -i`, parse JSON yourself | **Kept** | Plus: always `Accept: application/hal+json`; `Content-Type` from the form. |
| Hash rules, `_registry_state`, agent directory | **Dropped / replaced** | No hashes. A small `_session_state` (`entry`, `current`) survives truncation. |
| Sync parameters (`Accept-Profile`, `param:*`) | **Dropped** | Read = `GET` with `Accept: application/hal+json`. |
| "State and `_forms` together or non-compliant" | **Changed** | A resource with only `_links` is normal. |
| Reason / Act | **Kept, widened** | Links and GET forms go somewhere; POST/PUT/PATCH/DELETE forms do something. Forms follow the spec's processing order: collect fields, resolve the target, build the body, send. |
| Templates | **New** | Fill templated targets from every field, fixed ones included; validate against the schema before expanding. |
| Error table | **Rewritten** | vnd.error; `409` means "the rules refuse", `400` names the fields. |
| Interactive vs autonomous | **Kept** | Client behaviour; default interactive. |
| Hard rules | **Kept and extended** | Never build an address except by filling a server-given template with schema-valid values; never change a `const`; never fall back to the HTML view. |

## 3. The cycle

**Read** the current href with `Accept: application/hal+json`; after an action, a `2xx` HAL body is the
next Read. **Choose** exactly one link or form for the user's intent, by rel, title and schema; ask when two
fit or none does. **Act**: GET a link; for a form, collect values (fixed fields from the schema, required
fields from the user or the state, optional ones only when given), fill the target template, send a body
only for methods that carry one, then Read again.

## 4. Status codes

| Code | What the agent does |
|---|---|
| 200 / 201 | The body is the resource now: the next Read. |
| 400 | Fix the fields the vnd.error names, from the user or the schema. Never guess. |
| 404 | Stop and say so; offer to continue from `about`. |
| 405 / 415 | Its own mistake: report it, do not switch methods. |
| 409 | Not possible now: Read `about`, show what *is* offered, never resubmit. |
| 412 / 428 | (Not used yet.) Read again before anything else. |
| 5xx | Report it; no blind retry. |

## 5. Hard rules

- Never build an address yourself: only hrefs, form targets, `Location`, `about`, and templates the server
  gave you filled with values that fit the schema.
- Never act on a link or form that is not in the latest response.
- Never change a fixed (`const`) field; never invent a required value.
- Never carry knowledge over from another server, session or memory.
- Never suppress an error; never use the HTML view.

## 6. The gaps, and how each was closed

| Gap | Fix | Commit |
|---|---|---|
| Errors were `problem+json`; the spec requires vnd.error | vnd.error with field paths and `about` links | `b3225fb` |
| A missing required field returned `500` | `400` naming each missing field, checked against the focus's own form | `b3225fb` |
| `409` named internal cell ids | "A1, A2 and A3 are already planted", with `about` back to the focus | `b3225fb` |
| No way down from the whole bed except the journal | `go-to-row`, `go-to-column`, `go-to-cell` GET forms; congruence checked both ways | `e940e57` |
| `started` required on some care forms, optional on others | Optional everywhere; left out, the moment the care arrives | `47e1c60` |
| `prepare-bed` needed a client-chosen id | The server chooses it; `dimensions` says rows and columns are required | `9b0a474` |
| Forms and fields lacked titles and descriptions | Every form titled; every field titled and described, with units | `25f0132` |
| `?recent=N` was not advertised | A `refresh` GET form with `{?recent}` on every focus | `a6ae236` |
| Profile only in a header; schemas declared 2020-12 | Profile in the media type too; JSON Schema 2019-09 | `f8e1065` |
| `prepare-bed` lived in the old `/api` tree | `POST /beds`, answering from `/beds/{id}` | `825b163` |
| Form targets embedded the bed id | One template for every bed; the bed is a fixed field (`const`, `default`, `readOnly`) | `0b8936a` |

Still open, in `notes/ideas-for-later.md`: retiring the old `/api/...` addresses, plain-HTML fallbacks
under the htmx controls, and ETag/If-Match.

## 7. Proving the skill

1. **Done:** the skill is written (`dcba3ae`).
2. **Next:** a person drives the skill interactively against the running app, from the bookmark
   `http://localhost:8080/` only, while every exchange and decision is logged to `agentlog.md`. Intents
   worth covering: listing the beds, preparing a bed, planting at a named cell, watering a row, a refused
   replant, a harvest with its offered choices, an incomplete form, and refreshing with fewer recent events.
3. **Then:** fold what the session shows back into the skill and the API.
