# An MCP wrapper over a HAL Schema Forms API

A conceptual design, with no behaviour implemented yet. The question: MCP models an API as **tools**,
named operations listed up front, while a Fielding-style hypermedia API offers its operations **in each
response**, changing with state. How does a tool wrapper stay true to that?

**Answer:** don't wrap the API's operations, wrap the **uniform interface**. A few fixed tools behave like a
browser (read, follow, submit). The API's real affordances, its links and forms with their schemas, come
back *inside* the tool results, the way a page carries its own links and forms.

## 1. Three ways to map it

| Approach | What the tools are | What goes wrong |
|---|---|---|
| **A. One tool per operation** (`plant`, `water`, `get_bed`…) | Hand-written, or generated from the endpoints | It bakes the API's structure into the tool list: the out-of-band coupling HATEOAS removes. Every API change breaks the tools, and every API needs its own wrapper. |
| **B. One tool per current form** (regenerated after each response via `tools/list_changed`) | `go_to_cell`, `plant_seedling`…, each form's schema as its `inputSchema` | Closer, but the tool list churns on every step; the server must remember each client's state (sticky sessions); clients support list changes unevenly; names collide across resources. |
| **C. A generic hypermedia browser** | A few fixed tools mirroring HTTP + HAL + forms: **read, follow, submit** | The tool list never changes, the affordances live in the results, and one wrapper serves *any* HAL Schema Forms API. |

**Recommendation: C**, with B as an optional convenience view on top later.

## 2. How C keeps Fielding's constraints

- **Uniform interface.** The same tools for every API and every state, as a browser's buttons are the same
  on every site.
- **Hypermedia as the engine of application state.** Every transition comes from the last representation:
  the model names a link by `rel` or a form by id, and never passes an href it made up. The only raw
  address is the configured bookmark.
- **Self-descriptive messages.** The wrapper checks the media type and profile. Form titles, field
  descriptions and bounds carry the meaning, so the tool descriptions stay domain-free.
- **Stateless.** Each call carries `from`, the href it acts from. The wrapper keeps no session, so any
  instance can serve any call; this fits MCP's stateless, non-sticky transport.
- **Layered system.** The wrapper is simply a user agent sitting between the model and the API.
- **Cacheable.** The wrapper may cache representations; ETag/If-Match can come later (the HEART step).

## 3. The tools

```json
{ "tools": [
  { "name": "hal_read",
    "description": "Read a resource and list what it offers: state, links, forms. Start from the bookmark or an href a previous result gave you.",
    "inputSchema": { "type": "object",
      "properties": { "href": { "type": "string", "description": "Omit for the bookmark" } } } },
  { "name": "hal_follow",
    "description": "Go somewhere the resource at `from` links to, by rel (optionally a link of an embedded item).",
    "inputSchema": { "type": "object", "required": ["from", "rel"],
      "properties": {
        "from": { "type": "string" },
        "rel":  { "type": "string" },
        "item": { "type": "object", "description": "Pick an embedded item: {\"embedded\": \"beds\", \"where\": {\"name\": \"Earth\"}}" } } } },
  { "name": "hal_submit",
    "description": "Submit a form offered by the resource at `from`. Fixed fields are filled for you; values are checked against the form's schema before anything is sent.",
    "inputSchema": { "type": "object", "required": ["from", "form"],
      "properties": {
        "from":   { "type": "string" },
        "form":   { "type": "string" },
        "values": { "type": "object" } } } }
] }
```

**What the wrapper does on each call:**
1. **Re-read `from`.** The named link or form must be in the current representation; a stale or invented one
   is refused. This is the skill's "act only on what was just offered", enforced in code.
2. **Fill fixed fields.** Fields marked `const`/`readOnly` are filled by the wrapper, never by the model.
3. **Check the values against the schema before any network call:** required fields, `enum`,
   `minimum`/`maximum`, `pattern`, `format`. A failure here is a *guardrail refusal*, and nothing is sent.
4. **Expand and send.** Expand the target template (RFC 6570), build the body per `contentType`, and send
   with the form's `method`.
5. **Answer with an affordance card** for the resulting resource. A vnd.error becomes `isError`, with its
   `about` link as the next `from`.

## 4. The affordance card

Every result has one compact shape, which the model reads the way a person reads a page:

```json
{ "at": "/beds/a655…/focus/cell/A2", "status": 200,
  "state": { "bedName": "Sam's Playground 2", "focus": "A2", "location": "A2" },
  "links": [
    { "rel": "zoom-out", "title": "Row A" },
    { "rel": "east", "title": "A3" },
    { "rel": "journal", "title": "A2 journal" } ],
  "forms": [
    { "id": "harvest-crop", "title": "Harvest", "method": "POST",
      "fields": {
        "plantType":     { "required": true, "enum": ["tomato"], "description": "What grows there" },
        "plantCultivar": { "required": true, "enum": ["Dark Galaxy"] },
        "quantity":      { "type": "integer", "description": "How many were picked" },
        "weight":        { "type": "number", "description": "…in kilograms" } },
      "fixed": ["bedId", "location"] } ],
  "recent": ["harvested A2", "fertilized A2", "planted A2"] }
```

**What the card leaves out on purpose:**
- **Hrefs**, apart from `at`. The model acts by `rel` or form id, so it never holds an address it could copy
  or alter.
- **The values of fixed fields.** Fixed fields are listed by name; the wrapper fills their values.

## 5. Examples against today's API

**Going straight to a cell (a GET form):**
```json
→ hal_submit { "from": "/beds/a655…/focus/bed", "form": "go-to-cell", "values": { "row": "C", "column": 3 } }
   wrapper: bedId fixed → /beds/{bedId}/focus/cell/{row}{column} → GET /beds/a655…/focus/cell/C3
← { "at": "/beds/a655…/focus/cell/C3", "status": 200, "state": { "focus": "C3" },
    "forms": ["water-cells", "fertilize-cells", "mulch-cells", "harvest-crop", "refresh"] }
```

**A guardrail refusal (nothing sent):**
```json
→ hal_submit { "from": "/beds/a655…/focus/bed", "form": "go-to-cell", "values": { "row": "F", "column": 9 } }
← { "isError": true, "refusedBy": "wrapper",
    "errors": [ { "field": "row", "message": "F is not one of A, B, C, D, E" },
                { "field": "column", "message": "9 is above the maximum 3" } ] }
```

**Caring for the cells the agent names, at the whole bed:**
```json
→ hal_submit { "from": "/beds/a655…/focus/bed", "form": "plant-seedling",
               "values": { "plantType": "tomato", "plantCultivar": "Dark Galaxy", "location": "A1 to A3" } }
← { "at": "/beds/a655…/focus/bed", "status": 200, "recent": ["planted A1 A2 A3"] }
```

**A form that is not offered, and a server refusal:**
```json
→ hal_submit { "from": "/beds/a655…/focus/cell/A2", "form": "plant-seedling", "values": { … } }
← { "isError": true, "refusedBy": "wrapper",
    "message": "No form plant-seedling at /beds/a655…/focus/cell/A2 (offered: water-cells, fertilize-cells, mulch-cells, harvest-crop, refresh)" }

   (if a stale form slipped through, the server answers with vnd.error instead:)
← { "isError": true, "refusedBy": "server", "status": 409, "message": "A2 is already planted",
    "next": { "from": "/beds/a655…/focus/cell/A2" } }
```

**Following a link of an embedded item:**
```json
→ hal_follow { "from": "/beds", "rel": "self", "item": { "embedded": "beds", "where": { "name": "Earth" } } }
← { "at": "/beds/c0e7…", "state": { "bedName": "Earth", "focus": "Whole bed" }, … }
```

## 6. Other MCP features, used sparingly

- **Prompt:** an `act_as_hal_agent` prompt carrying the skill's stance: no domain knowledge, ask when
  unsure, stop on refusals.
- **Resource:** the bookmark as one resource, so a client can show where the agent starts.
- **Resource templates:** they use RFC 6570 too, and GET forms *could* map onto them. But resources are chosen
  by the client application rather than the model, and they carry no schema bounds, so tools stay primary.

## 7. What this means for wrappers

- **Nothing to generate per API.** One wrapper per *media type* (HAL Schema Forms) serves every API that
  speaks it; it is configured with a bookmark and credentials only.
- **The guardrails are the forms.** The wrapper checks every submission against the form's own schema before
  sending, and the server checks again.
- **The agent-versus-person demo still holds.** The page, the `hal-schema-agent` skill and the MCP tools all
  read the same forms.

## 8. Open questions for when it is built

- **Selecting embedded items** (`item.where`): a small, explicit selector language, or index-only?
- **Bodies:** should `hal_submit` return the full representation alongside the card, for clients that want it?
- **Long sessions:** should `from` carry an ETag, so a stale view is caught by `If-Match` instead of the re-read?
- **Ephemeral tools (approach B):** for clients that prefer typed tools, offer each current form as one, with
  `tools/list_changed`, and treat it strictly as a view over C.
