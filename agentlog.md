# Agent log: driving StarbornAG through HAL Schema Forms

An interactive session: Josh gives intents, Claude acts as the `hal-schema-agent` skill
(`.claude/skills/hal-schema-agent/SKILL.md`) against the running app, from the bookmark only. Each turn
records the intent, what the agent read, what it chose and why, what it sent, what came back, and the
session state.

- **Bookmark:** `http://localhost:8080/`
- **Started:** 2026-10-01 03:38 UTC
- **Mode:** interactive (ambiguity goes to Josh)

## Summary so far (after turn 5)

| Turn | Josh asked | The agent did | Outcome |
|---|---|---|---|
| 0 | (start) | Read the bookmark | Two links, `self` and `beds`; no forms |
| 1 | "call Kevin Gough's magic service" | Found nothing offered that matches; made no request | Asked instead of stretching the intent |
| 2 | "Beds" | Followed `beds` | 19 beds and a `prepare-bed` form |
| 3 | "make a bed named Sam's Playground 3 cols 5 rows size 1" | Filled `prepare-bed`, flagging how it read "3 cold" and "size 1" | `201`: a 5 × 3 bed with go-to and care forms |
| 4 | "tomatoes in row 1, eggplant in the other rows" | "Row 1" fits neither the row `enum` (A–E) nor the cells' description | Asked: row A or column 1? |
| 5 | "row A for the tommies, the other rows for beauties" | Posted `plant-seedling` with `location` "A1 to A3" | **Server bug:** all 15 cells planted with tomato |

**What worked:**
- The agent never built an address. It used only links, form targets and templates filled from fixed
  fields.
- It asked whenever the words did not fit the schema: an unknown service, "row 1" against letter rows.
- The forms carried what it needed to know: titles, descriptions with units and defaults, bounds as
  `enum`/`minimum`/`maximum`, and a server-chosen id.

**What broke:**
- At the whole bed, the `plant-seedling` form leaves `location` open and describes how to fill it, but the
  server replaced the agent's location with the focus's own (none, meaning the whole bed).
- The form's promise and the endpoint disagreed, and the existing scenarios only post from focuses whose
  location is fixed, so none caught it.

**What it showed about the domain:** there is no way to undo a planting or remove a bed, so a wrong
planting cannot be corrected through the API (emptying a cell is an open decision in the ideas list).

**Next:** fix the server Gherkin-first (override the location only when the form fixes it), then redo the
planting on a fresh bed.

---

## Turn 0: first Read of the bookmark

**Intent:** none yet; read the entry point so the first intent can be mapped onto what it offers.

**Read** `GET /` with `Accept: application/hal+json`
- `200`, `application/hal+json;profile="https://github.com/jbadeau/hal-schema-forms"`, plus
  `Link: <https://github.com/jbadeau/hal-schema-forms>; rel="profile"`.
- State: none.
- Links:
  - `self` → `/`
  - `beds` → `/beds` ("Every bed")
- Forms: none. Only somewhere to go.

**Choose / Act:** nothing to do yet.

```json
{ "_session_state": { "entry": "http://localhost:8080/", "current": "/" } }
```

---

## Turn 1: "You should call Kevin Gough's magic service"

**Read:** the current resource is still `/`, read in turn 0. Nothing has acted since, so no new request.

**Choose:** the intent asks for a "magic service" belonging to "Kevin Gough". The response offers:
- `self` ("/")
- `beds` ("Every bed")
- no forms

No rel, title or form mentions a service, Kevin Gough or magic. Per the skill, an intent that maps onto
nothing is reported, not stretched onto the closest affordance, and no address is made up for it.

**Act:** none. Asked Josh whether he meant something the API offers (the beds), or whether he has an
address for that service from somewhere else.

```json
{ "_session_state": { "entry": "http://localhost:8080/", "current": "/" } }
```

---

## Turn 2: "Beds"

**Choose:** the link `beds` ("Every bed") on `/`. One clear match, nothing to ask.

**Act → Read** `GET /beds` with `Accept: application/hal+json`
- `200`, HAL with the HAL Schema Forms profile.
- Links:
  - `self` → `/beds`
  - `up` → `/`
- Embedded beds, 19 in all, each with its own `self` link:
  - Earth, 5 × 10
  - Jupiter, 5 × 10
  - 17 beds named "Demo HH:MM:SS", 4 × 6 each (left over from earlier demo recordings)
- Forms:
  - `prepare-bed` ("Prepare a bed"): `POST /beds`, requires `name` and `dimensions`.

No "magic service" here either.

```json
{ "_session_state": { "entry": "http://localhost:8080/", "current": "/beds" } }
```

---

## Turn 3: "make a bed named Sam's Playground, 3 cols 5 rows and size 1"

**Read:** re-read `/beds` to see the `prepare-bed` schema in full:
- `name`: required, "What the bed is called".
- `dimensions` ("Size"): required; its own `rows` and `columns` are required, `height` is optional.
- `cellBlockSize` ("Cell width"): optional, "How many columns one cell spans".
- `bedId`: optional, "left out, the server chooses one".

**Choose:** `prepare-bed`, the only form, and its title matches.

**Mapping the words onto fields:**
- "Sam's Playground" → `name`.
- "3 cold" was read as 3 columns: the only number-of-something field left besides rows. Flagged to
  Josh rather than silently assumed.
- "5 rows" → `dimensions.rows`.
- "size 1" → `cellBlockSize`. The form's "Size" is `dimensions`, already given as rows and columns, so the
  remaining size-like field is "Cell width".
- `bedId` left out, so the server chooses it.

**Act:** `POST /beds` (the target is not templated, so used verbatim), `Content-Type: application/json`

```json
{ "name": "Sam's Playground", "dimensions": { "rows": 5, "columns": 3 }, "cellBlockSize": 1 }
```

**Result → next Read:** `201`, `Location: /beds/09ce3ef5-d858-4290-a193-fb18052e36bb`; the body is the
new bed's whole-bed focus.
- State: `bedName` "Sam's Playground", focus "Whole bed", 15 cells (5 rows × 3 columns).
- Links:
  - `self` → `…/focus/bed`
  - `bed` → `/beds/09ce…`
  - `journal`
- Forms, all with targets templated on the fixed `bedId`:
  - Care: `plant-seedling` ("Plant a seedling"), `water-cells` ("Water"), `fertilize-cells` ("Feed"),
    `mulch-cells` ("Mulch"). No harvest, since nothing grows yet.
  - Navigation: `go-to-row` (row ∈ A–E), `go-to-column` (column 1–3), `go-to-cell` (row A–E, column 1–3),
    `refresh` (`recent` ≥ 0).
- Embedded recent: none yet.

```json
{ "_session_state": { "entry": "http://localhost:8080/", "current": "/beds/09ce3ef5-d858-4290-a193-fb18052e36bb/focus/bed" } }
```

---

## Turn 4: "Plant dark galaxy tomato seedlings in row 1 and black beauty eggplant in all the other rows"

**Read:** re-read the whole bed for the `plant-seedling` schema:
- `bedId`: fixed (`const`, `readOnly`).
- `plantType`: required ("What grows there, such as tomato").
- `plantCultivar`: required ("The cultivar, such as Dark Galaxy").
- `location` ("Cells"): optional here, described as "Cells as spoken: A1, B2 to C4, A1 A3 B5, or 3:2
  (letter = row, number = column). Omit for the whole bed."
- `started`: optional.

**Choose:** `plant-seedling`, the only planting form. But "row 1" does not fit what the server says about
rows:
- `go-to-row` allows only rows `A B C D E` (an `enum`).
- The `location` description says "letter = row, number = column".

So "row 1" could be row A (the first row) or column 1. "All the other rows" depends on which. The skill
says a value that does not fit is not guessed at, so this goes back to Josh.

**Plan once it is clear** (two posts, both to the whole bed's `plant-seedling`, using the `location` field
as its description shows):
- If row A: tomato · Dark Galaxy at `A1 to A3`, then eggplant · Black Beauty at `B1 to E3`.
- If column 1: tomato · Dark Galaxy at `A1 to E1`, then eggplant · Black Beauty at `A2 to E3`.

**Act:** none yet; asked Josh.

```json
{ "_session_state": { "entry": "http://localhost:8080/", "current": "/beds/09ce3ef5-d858-4290-a193-fb18052e36bb/focus/bed" } }
```

---

## Turn 5: "row A for the tommies and the other rows for beauties"

**Choose:** the whole bed's `plant-seedling`, twice, as planned:
1. Tomato · Dark Galaxy at `A1 to A3`.
2. Eggplant · Black Beauty at `B1 to E3`.

**Act (first post):** `POST /beds/09ce…/focus/bed/plant`. The target `/beds/{bedId}/focus/bed/plant` was
filled from the fixed `bedId`.

```json
{ "bedId": "09ce3ef5-d858-4290-a193-fb18052e36bb", "plantType": "tomato", "plantCultivar": "Dark Galaxy", "location": "A1 to A3" }
```

**Result:** `200`, but **all 15 cells were planted with tomato**, not just row A.

**Why: a server bug, not an agent mistake.** At the whole bed, the form leaves `location` open (optional,
not `const`) and describes how to fill it. But the server's JSON care endpoint replaces whatever location
the agent sends with the focus's own location, and for the whole bed that is "none", which means every
cell (`BedPageController.careFromAnAgent`: `"location" to focus.location`). The form promised something the
endpoint did not honour.

**Consequences:**
- The second post was not sent: every cell is now planted, so `plant-seedling` would be refused anyway.
- There is no form to clear or empty a cell (removing a planting is an open domain decision), so the
  agent cannot undo this.

**Stopped and reported to Josh**, with the options: fix the server Gherkin-first (only override the
location when the form fixes it), then redo on a fresh bed.

**Fixed between turns** (`3146f33`): a new scenario in `care-at-the-focus.feature` failed first (`409`,
because the server tried the whole bed). The endpoint now overrides the cells only when the focus fixes
them (a cell, row or column); at the whole bed, the cells the agent names are the ones cared for. All 203
scenarios pass; the app was restarted with the fix.

```json
{ "_session_state": { "entry": "http://localhost:8080/", "current": "/beds/09ce3ef5-d858-4290-a193-fb18052e36bb/focus/bed" } }
```
