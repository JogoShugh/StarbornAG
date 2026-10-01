# Ideas for later

Ideas that came up while rebuilding StarbornAG and were parked on purpose.
Newest first. Move an item out when it becomes a slice.

## Voice navigation (UX option D, planned as navigation slice N4)

- **Fixed phrase grammar, parsed in Kotlin on the server.** The browser sends recognized speech
  for the current focus; phrases such as "north", "southeast", "next", "back", "go to B4",
  "row two", "column five", "whole bed", "zoom out", "watered", "mulched with straw" and
  "planted tomato Dark Galaxy here" move the focus or give care there, instantly and without a
  model. Anything that does not match falls back to the AI path (`ai/plant`).
- Tested with Gherkin tables of phrases, like the focus moves.
- Spoken confirmation ("B4, tomato, watered two days ago") and no need to say "starborn" before
  every phrase while tending.
- Builds on the focus resource: every phrase maps to a move link or a form of the current focus.

## Hypermedia and HEART + RISE

- **Retire the old `/api/beds/...` addresses.** Every bed, focus and journal now answers at
  `/beds/...` in both views (HTML or HAL by `Accept`), and focus forms post to
  `/beds/{id}/focus/{path}/{action}`, and beds are prepared with `POST /beds`. The `/api/...`
  reads, the bed-level command endpoints (`/api/beds/{id}/plant` and so on), `POST /api/beds`, the
  bed-level `_forms` with a free-form location, and the old `/api/beds/{id}/negotiable` experiment
  remain as before; fold them into the one tree (or keep them as documented aliases) and point the
  bed resource's forms at the focus addresses.
- **HTML that works without JavaScript.** Put a real `<form action method>` or `<a href>` under
  every htmx control (progressive enhancement), so the page works with JavaScript off and a
  browsing agent that only understands plain HTML can still navigate and tend the bed.
- **ETag / If-Match (HEART L4).** Map a representation's `ETag` to stream versions and require
  `If-Match` on commands. `Repository.handle(id, expectedVersion)` already rejects stale versions
  with `WrongExpectedVersion`; map that to `412`, keep `409` for "not possible in this state"
  (already used for the soil rules), and `428` when `If-Match` is missing.
- **Content-addressed layers (HEART L1–L3).** Serve the affordance set and schemas at
  `/affordance-sets/sha256-…` and `/schemas/sha256-…` (RFC 8785 canonical JSON), opt-in via
  `Prefer`.
- **RISE envelopes** for asynchronous work, such as photo analysis of a harvest.

## Domain

- **Emptying a cell.** Planting is allowed only into an empty cell, and harvesting never empties
  one, so today a planted cell can never be replanted. Undecided: a spoken "cull plant" or
  "empty cell" command.
- **Harvest weight division.** "Harvested two pounds of tomatoes from row 1" should split the
  weight across the tomato cells in that row (README narrative).

## Read side

- **Projections (planned as slice 5).** The bed view reads every cell's stream on each request.
  Keep a ready-made view up to date with inline projections, and move the season windows from
  `BedCellAggregateState` there (8-month season, 1-week watering, 1-month fertilizing, 2-week
  harvests).
- **Journal from a projection (J2).** The bed journal (by cell, row, column, timeline) is built
  from those same per-cell reads today. A `BedJournal` projection, updated in the append
  transaction, would let it read one table. A command id in event metadata would make folding
  events back into commands exact instead of matching kind and start time.

## Found by the agent runs (agentlog-2.md)

- **Event names:** harvests read `bedHarvested` while the rest read `planted`, `watered`, `fertilized`,
  `mulched`; make it `harvested` (with an upcaster for stored events).
- **Water's default volume:** `water-cells` leaves `volume` optional and the server uses 1 liter, but the
  description does not say so.
- **The fixed `location` field's description** still says "Omit for the whole bed" on cells, rows and
  columns, where it is fixed.
- **Hops inside a row or column:** their `go-to-cell` covers only their own cells, so another cell needs a hop
  out first. Consistent with the page; consider a `go-to-cell` for the whole bed everywhere.

## Bed journal

- **Needs care.** Flag cells that look neglected, for example "dry 3 days" when a planted cell has
  not been watered for longer than its plant wants. Needs a rule per plant or one global window
  (the season windows above), so it waits on that decision. Shows as a flag on cell cards and a
  "⚠ Needs care" filter chip.
- **Plant and cultivar care profiles.** Metadata per plant type, refined per cultivar, that drives
  the care rules instead of one global window: how often it wants water and how much, feeding
  interval and fertilizer, mulch, days to maturity, harvest window, spacing. "Needs care" and
  recommendations read the profile of whatever grows in the cell (a Dark Galaxy tomato may differ
  from tomatoes in general).
  - **Longer-term goal: learning from the bed's own history.** Correlate the care each bed, and
    each cell, actually received (from its event history) with the yields recorded in harvest
    events (quantity, weight), so recommendations adapt to that bed and even that square foot of
    soil rather than only the generic profile. Needs harvest weights and quantities captured
    consistently, enough seasons of history, and care to keep the correlation honest (weather,
    soil and cultivar differences).
- **Care filter chips** (💧 🌿 🪵 🧺) on the journal, from the mockups.
- **Drag gesture** on the handle (collapsed, half, full); taps work today.

## Platform

- **Publish eventstore-kotlin** as a package, so starbornes no longer needs it checked out next
  to it for the composite build.
- **Spring Boot 4 / Spring AI 2 / Jackson 3** migration (api is on Boot 3.5, Spring AI 1.1).
- **AI handlers** (`NlpCommandHandler`, `ai/plant`, `JsonArrayItemStreamer`): left untouched on
  purpose. The streaming parser may drop the last command (one parse event per chunk, never
  drained); it has no tests yet.
