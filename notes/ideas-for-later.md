# Ideas for later

Ideas that came up while rebuilding StarbornAG and were parked on purpose.
Newest first. Move an item out when it becomes a slice.

## Bed page polish (seen in the first screenshots of the focus page)

- **Phone width.** 10 columns of 75 px cells do not fit a phone; the grid needs to scale down.
- **Row and column pads.** A row focus shows an empty top row in the pad; show ↑/↓ only (←/→ for a
  column), with zoom-out beside them.
- **Microphone button.** The SpeechKITT mic sits over the bottom-left corner of the focus panel.
- **Zoom-out label.** The ⤢ symbol reads small; a label such as "Zoom out" or "Row A ↑" is clearer.
- **Harvest cultivar.** The harvest form asks for the cultivar; pre-fill it from what grows in focus.

## Hypermedia and HEART + RISE

- **Per-cell forms.** Today `_forms` exist only at bed level (slice 4c). Give each cell its own
  forms, so an agent reading cell A1 sees exactly what A1 allows: for example `harvest-crop`
  only on the cells growing that plant, and `plant-seedling` only on empty cells. Built from the
  same `BedCell` predicates as the soil rules, like `possibleCare`.
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

## Platform

- **Publish eventstore-kotlin** as a package, so starbornes no longer needs it checked out next
  to it for the composite build.
- **Spring Boot 4 / Spring AI 2 / Jackson 3** migration (api is on Boot 3.5, Spring AI 1.1).
- **AI handlers** (`NlpCommandHandler`, `ai/plant`, `JsonArrayItemStreamer`): left untouched on
  purpose. The streaming parser may drop the last command (one parse event per chunk, never
  drained); it has no tests yet.
