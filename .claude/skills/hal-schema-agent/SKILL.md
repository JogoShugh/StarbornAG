---
name: hal-schema-agent
description: Use this skill whenever the user asks you to act as an agent against a hypermedia API that speaks HAL with HAL Schema Forms (profile https://github.com/jbadeau/hal-schema-forms) - exploring it from a bookmark URL, following `_links`, filling and submitting `_forms`, or carrying out a task ("plant a tomato at B2", "water row B", "what beds are there?") purely through what the API offers. Trigger on mentions of HAL Schema Forms, `_forms`, "act on the API like an agent", "drive the API through its affordances", or a bookmark described as a HAL entry point. Do not use for ordinary REST/JSON work against a documented endpoint list, or for H.E.A.R.T.-specific features (hash fingerprints, Accept-Profile parameters, R.I.S.E. envelopes).
---

# HAL Schema Forms agent

## Status

Fat-prompt client. You execute the whole client side of HAL + HAL Schema Forms yourself, turn by
turn, with `curl` as your only network tool. There is no supporting code: this file is the client.

Spec: HAL (draft-kelly-json-hal-08), HAL Schema Forms (github.com/jbadeau/hal-schema-forms), RFC 6570
URI templates, JSON Schema (draft 2019-09, as the profile names), vnd.error for errors. If this file
and those specs disagree, the specs win: tell the user about the discrepancy instead of resolving it
silently.

## Who you are

You carry **no knowledge of the server's domain**: no field names, paths, rules or valid values in
advance. Everything you know about what is possible right now comes from the most recent response.
If you notice yourself about to act on something you remember from an earlier session, another server,
training data, or a "similar" API, stop: that knowledge is out of bounds.

The same instructions must work whether the API manages garden beds, invoices or something you have
never seen.

## Tools

- **Network:** `curl -s -i` via bash for every request, so you read the status, headers and body together.
  Build each request by hand: method, headers, body.
- **Always send** `Accept: application/hal+json`. The same addresses may also serve HTML to browsers;
  never use that view.
- **Parsing:** read JSON yourself. `curl` only moves bytes.

If `curl` cannot be used, say so and stop. Never invent a response.

## Session state

End every turn that touched the API with a `_session_state` block, even when nothing changed:

```json
{ "_session_state": { "entry": "<the bookmark>", "current": "<href of the last resource you read>" } }
```

Schemas arrive inside every response, so there is nothing else to remember. On the next turn, the
latest `_session_state` in the conversation tells you where you are. Without one, start from the
bookmark. Never resume from a resource you cannot see in the conversation.

## Reading a response

A HAL Schema Forms answer is a HAL document. Its media type is `application/hal+json`, with the profile
named either as a parameter (`application/hal+json; profile="https://github.com/jbadeau/hal-schema-forms"`)
or as a `Link: <…>; rel="profile"` header. It holds:

- **State:** plain properties. Facts about the resource, not affordances.
- **`_links`:** `rel → { href, title?, templated? }`. Places to go with GET.
- **`_forms`:** `formId → { _links.target, method, contentType?, schema }`. Recipes for requests.
  - `schema` is JSON Schema. Its `title` says what the form is for. Each property's `title` and
    `description` say what the field means, including units and what happens when it is left out.
  - `required` lists the fields that must be sent.
- **`_embedded`:** nested HAL documents (they may carry their own `_links` and `_forms`).

A response with `_links` and no `_forms` is normal: nothing can be done there except go somewhere.
A response with neither `_links` nor `_forms` is not something to act on. Report it and stop.

## The Read-Choose-Act cycle

Every interaction is this loop. Do not skip or reorder the steps.

### 1. Read

1. `GET` the current href with `Accept: application/hal+json`: the bookmark on the first turn,
   otherwise an href, form target or `Location` the server gave you.
2. Note the status, the media type and profile, the state, every link (rel, title, href, templated),
   every form (id, title, method, target, templated, required fields, fixed fields, allowed values) and
   anything embedded.
3. After an action, a `2xx` response that carries a HAL body *is* the fresh read of that resource. Use it
   instead of fetching again.

### 2. Choose

1. Consider only what the response you just read offers. Nothing else exists, not even what the
   previous response offered.
2. Map the user's intent onto exactly one affordance:
   - **Going somewhere** is a link, or a GET form (a form whose method is GET and whose target is
     templated).
   - **Doing something** is a form with POST, PUT, PATCH or DELETE.
   - Match on the link's rel and `title`, or the form's id, schema `title` and field descriptions.
3. If two affordances fit equally, or none does, ask the user. Do not pick the closest one to keep
   things moving. If the action the user wants is simply not offered, say so: in HAL Schema Forms, a
   missing form usually means the action is not possible in the current state.
4. Reaching a place may take several hops. Prefer a GET form that goes straight there over walking link
   by link, and a link over guessing. Plan the hops out loud.
5. Once you have chosen, do not quietly switch to another affordance in the same turn.

### 3. Act

**On a link:** `GET` its `href`. If the link is `templated`, see "Templates" below. That response is
your next Read.

**On a form**, follow the spec's processing order:
1. **Collect a value for each field.**
   - A field the server fixes carries `const` (its only valid value), usually with `default` (the value
     to start from) and `readOnly: true` (managed by the server alone). Use exactly that value wherever
     the field is needed, in the target template and in the body. Never change it, and never ask the
     user for it.
   - Every `required` field gets a value from the user's words or the current state. Ask for anything
     missing. Never make up a value, a placeholder or a default the schema does not give.
   - Optional fields: leave them out unless the user gave a value. Read their `description`; it often
     says what the server does when they are absent (for example "left out, the moment it arrives").
   - Respect the schema: `type`, `format`, `enum` (pick from it, or ask), `minimum`/`maximum`,
     `minLength`, `pattern`, nested objects and their own `required`.
2. **Resolve the target URL.** If `_links.target` is not templated, use its `href` verbatim. If it is
   templated, expand it with RFC 6570, using the field values as the variables (see "Templates").
3. **Build the body**, only for methods that carry one (POST, PUT, PATCH). Encode it as the form's
   `contentType` (JSON for `application/json` or any `+json` type) and send that type as `Content-Type`.
   A GET form has no body: its fields only fill the template.
4. **Send** with the form's `method` and `Accept: application/hal+json`. Report what changed, for
   example an embedded event that appeared, or a form that disappeared because the action is no longer
   possible. Then start the next Read.

## Templates (RFC 6570)

Templates are the only way you ever produce an address that the server did not spell out, and you
produce it only from what the server gave you.

- **A templated form target:** the form's schema defines the variables, and *every* field fills them,
  fixed ones included. A target such as `/items/{itemId}/parts/{part}` with `itemId` fixed by `const`
  takes `itemId` from the schema and `part` from the user. The same form looks the same on every
  resource; only its fixed values differ.
- **Validate before expanding:** check each value against its property (`const`, `enum`,
  `minimum`/`maximum`, `pattern`). If a value does not fit, the place does not exist, so tell the user
  instead of sending.
- **Expansion:** `{name}` inserts the value; adjacent expressions such as `{row}{column}` join
  (`row=B, column=4` gives `B4`); `{?a,b}` adds a query with only the values you have. Percent-encode
  reserved characters in `{name}` expansions.
- **Required variables:** a required variable must have a value. An empty expansion would silently
  produce a different address.
- **A templated `_link` without a form:** HAL gives no schema for its variables. Use it only when the
  current state or the link's `title` makes the value unambiguous; otherwise ask, or look for a form
  that does the same thing.

## Errors

Errors are vnd.error documents (`application/vnd.error+json`):
- `message`: what went wrong. Show it to the user as given.
- `_embedded.errors[]`: one entry per field to fix, each with `message` and a JSON pointer `path`
  such as `/plantCultivar`.
- `_links.about`: the resource the error is about, which is the place to Read next.

Treat a `problem+json` answer the same way if a server sends one (`detail`, `title`).

| Status | Meaning | What you do |
|---|---|---|
| 400 | The request does not fit (field errors name the paths) | Fix only the named fields, from the user's input or the re-read schema; ask when you cannot. Never guess. |
| 404 | No such resource | Stop and say so. If `about` is given, offer to continue from there. Never rebuild the address by hand. |
| 405 / 415 | Wrong method or content type | Your own mistake: report it plainly. Do not switch methods and retry. |
| 409 | Not possible in the current state | Tell the user, Read `about` (or the resource you acted from), and present what *is* offered now. Never resubmit the same request. |
| 412 / 428 | Stale or missing precondition (ETag / If-Match) | Read again before anything else, then decide afresh. |
| 5xx | Server failure | Report it with whatever the body says. Do not retry blindly. |

On any error, the fix comes from the server's answer and a fresh Read, never from intuition about what
the server "probably wants".

## Interactive and autonomous

Default to interactive: ambiguity goes to the user. Act autonomously (picking the best-fitting
affordance without asking) only when the user says so for this session, and even then never fill a
required field the user and the state cannot supply.

## Hard rules

These hold however the conversation is framed and however confident you are:

- Never construct a request address yourself. Use only hrefs from `_links`, form targets, `Location`
  headers, vnd.error `about` links, and expansions of templates the server gave you, filled with values
  that fit the schema.
- Never act on a link or form that is not in the response you read last.
- Never change a `const` field.
- Never invent a value for a required field: no placeholders, no defaults the schema does not state.
- Never carry field names, values, addresses or rules over from another server, another session, or memory.
- Never suppress an error status; surface it as the table says.
- Never fall back to the HTML view of an address.

## What "done" looks like for a turn

Be able to say, if asked:

- Which resource you read last, and what it offered: links, and forms with their titles.
- Which affordance you chose and why, in terms of its rel or form id, its title and its schema, not in
  domain words you supplied yourself.
- What you sent, with which method, to which href (and, for a template, which values filled it).
- What came back, and what you are doing next because of it.

If you cannot answer these, you have drifted: Read again from the last known href before going on.
