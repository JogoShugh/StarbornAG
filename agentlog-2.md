# Agent log 2: a self-directed run

Claude made up the intents and played both parts: the user giving them, and the `hal-schema-agent` skill
carrying them out against the running app, from the bookmark only. Where the skill says "ask the user", the
made-up user's answer is noted. A small helper read answers and filled templates; it never built an address.

- **Bookmark:** `http://localhost:8080/`
- **When:** 2026-10-01, after the location fix (`3146f33`)
- **Bed:** Sam's Playground 2 (`/beds/a6551fb5-…`), 5 rows × 3 columns

## Summary

| # | Intent | Path taken (only links, form targets, filled templates) | Result |
|---|---|---|---|
| 1 | What is there? | `/` → `beds` | 20 beds, `prepare-bed` form |
| 2 | Make "Sam's Playground 2", 5 rows × 3 columns | `prepare-bed`, no id | `201`, Location `/beds/a6551fb5-…` |
| 3 | Tomatoes (Dark Galaxy) in row A, eggplant (Black Beauty) in rows B–E | Whole bed `plant-seedling` twice, `location` "A1 to A3" then "B1 to E3" | Exactly those cells; the location fix holds |
| 4 | Water row B | `go-to-row` (row=B) → `water-cells`, no fields | `watered B1 B2 B3` |
| 5 | Feed column 2, 0.5 L of fish emulsion | `zoom-out` → `go-to-column` (column=2) → `fertilize-cells` | `fertilized A2 B2 C2 D2 E2` |
| 6 | Mulch C3 with straw | Column 2's `go-to-cell` allows only column 2, so C3 was refused before sending; `zoom-out` → bed `go-to-cell` (C, 3) → `mulch-cells` | `volume` is required, so the agent asked ("2 liters"), then `mulched C3` |
| 7 | Harvest A2: 3 tomatoes, 0.6 kg | `bed` link → `go-to-cell` (A, 2) → `harvest-crop`, choices from its enums (tomato, Dark Galaxy) | Recorded; harvest stays offered |
| 8 | Plant basil in A2 | Harvest's answer offers no `plant-seedling` at A2 | Not offered, so not attempted: A2 still grows tomatoes |
| 9 | Go to F9 | Bed `go-to-cell`: F is not in the row `enum` (A–E), 9 is past the maximum (3) | Refused before sending: no such place |
| 10 | Just the last 2 things in row A | `go-to-row` (A) → `refresh` (recent=2) | `bedHarvested A2`, `fertilized A2` |
| 11 | Row A's story, in time order | Row A `journal` → `by-time` link | harvested A2 · fertilized A2 · planted A1–A3 |

Every request was `200`/`201`. No address was built by hand, no `const` was changed, and no required
value was invented (the mulch volume was asked for).

## What the run showed

**Worked as designed**
- The location fix: at the whole bed, the cells the agent names are the ones cared for (turn 3).
- Bounds in the schemas stopped bad places before any request: C3 from column 2, F9 from the bed (turns 6, 9).
- A missing form meant "not possible now" (turn 8), with nothing posted.
- Enums gave the harvest choices (turn 7); the descriptions gave units and what happens when a field is
  left out (turns 4, 5, 6).
- The helper (not the API) crashed mid-run. The skill's rule, "Read again from the last known href",
  brought it back to row A without guessing.

**Worth fixing in the API** (candidates for the ideas list)
1. **Inconsistent event names:** harvests show as `bedHarvested`, while everything else is `planted`,
   `watered`, `fertilized`, `mulched`. They should read `harvested`.
2. **Water's volume default is not stated:** `volume` is optional on `water-cells`, and the server uses
   1 liter when it is left out, but the description does not say so (the others do say what "left out" means).
3. **The fixed `location` field still says "Omit for the whole bed":** on a cell, row or column it is fixed,
   so that hint is misleading there.
4. **Reaching another cell from inside a row or column takes a hop out first**, since their `go-to-cell`
   only covers their own cells. This is consistent with the page, but worth a thought.

## Turn details (concise)

1. `GET /` → links `self`, `beds`. `GET /beds` → 20 beds, `prepare-bed` (`POST /beds`, needs `name`,
   `dimensions`).
2. `POST /beds` `{"name":"Sam's Playground 2","dimensions":{"rows":5,"columns":3}}` → `201`; the body is the
   whole bed with care forms (plant, water, feed, mulch) and GET forms (`go-to-row` A–E, `go-to-column` 1–3,
   `go-to-cell`, `refresh`).
3. `POST /beds/{bedId}/focus/bed/plant`, filled from the fixed `bedId`:
   - `{"plantType":"tomato","plantCultivar":"Dark Galaxy","location":"A1 to A3"}` → planted A1 A2 A3.
   - Then `{"plantType":"eggplant","plantCultivar":"Black Beauty","location":"B1 to E3"}` → planted B1–E3.
   - `plant-seedling` disappeared once every cell was planted.
4. `go-to-row` row=B → `GET …/focus/row/B` (location fixed "B1 to B3"). `water-cells` with no fields → `200`,
   recent `watered B1 B2 B3`.
5. `zoom-out` → bed; `go-to-column` column=2 → column 2 (A2 to E2); `fertilize-cells`
   `{"volume":0.5,"fertilizer":"fish emulsion"}` → `fertilized A2 B2 C2 D2 E2`.
6. From column 2, `go-to-cell` allows `column` 2 only, so C3 was refused without a request.
   `zoom-out` → bed; `go-to-cell` row=C, column=3 → C3. `mulch-cells` needs `volume`; the intent had none,
   so the agent asked (answer: 2 liters) → `{"material":"straw","volume":2}` → `mulched C3`.
7. `bed` link → whole bed; `go-to-cell` A, 2 → A2, which offers `harvest-crop` with `plantType` ∈ {tomato},
   `plantCultivar` ∈ {Dark Galaxy}. Sent `{"plantType":"tomato","plantCultivar":"Dark Galaxy","quantity":3,"weight":0.6}`
   → recent `bedHarvested A2`.
8. The answer at A2 offers water, feed, mulch, harvest and refresh, but no `plant-seedling`: A2 is still
   planted. Reported; nothing sent.
9. `bed` link → whole bed; `go-to-cell` with row=F, column=9 fails the schema (row ∉ A–E; column > 3).
   Reported; nothing sent.
10. `go-to-row` A → row A; `refresh` recent=2 → `GET …/focus/row/A?recent=2` → two recent events.
11. (After the helper's crash, re-read row A from the session state.) `journal` → row A's journal by cell
    (3 planted, 5 events), with `by-cell` and `by-time` links; `by-time` → harvested A2, fertilized A2,
    planted A1–A3.

```json
{ "_session_state": { "entry": "http://localhost:8080/", "current": "/beds/a6551fb5-b71d-4af2-81a1-9c70bdbb67d3/journal?focus=row/A&by=time" } }
```
