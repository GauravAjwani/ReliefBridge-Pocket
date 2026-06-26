# 3-Minute Demo Script

## 0:00-0:25 - Problem

"In disaster relief and nonprofit operations, the work often happens in Slack,
but urgent asks, volunteer offers, and status updates get scattered across
channels. Coordinators lose time scanning threads when every minute matters."

## 0:25-0:50 - Product

"ReliefBridge is a Slack Agent for Good that turns that messy coordination into
a ranked, claimable relief queue. It uses Slack Real-Time Search for live
workspace context and a custom MCP relief directory for matching volunteers and
supplies."

## 0:50-1:45 - Live Slack Demo

Seed channels:

- `#flood-response`: wheelchair-accessible transport request.
- `#volunteers`: accessible vans available now.
- `#medical`: insulin cooler kit need and matching clinic offer.
- `#shelter-ops`: food delivery offer.

Prompt:

```text
@ReliefBridge what urgent requests are still unclaimed, and who can help?
```

Show ReliefBridge returning:

- Count of open needs and offers.
- Top priority item with urgency score.
- Matched volunteer/resource.
- Evidence links back to Slack channels.
- Claim, resolve, and escalate buttons.

Click `Claim` on the transport need and show the confirmation message.

## 1:45-2:25 - Architecture

Show `docs/architecture.md`.

"The Slack agent calls the RTS wrapper. For local demos, the same wrapper uses
seeded Slack data. The deterministic triage engine classifies, scores, and
deduplicates items. The MCP server exposes tools like `search_resources`,
`search_volunteers`, and `suggest_match`."

## 2:25-2:55 - Impact

"This helps small nonprofits and mutual-aid teams respond faster without buying
new systems. It reduces missed urgent requests, helps volunteers take the next
right action, and keeps the whole workflow inside Slack."

## 2:55-3:00 - Close

"ReliefBridge makes Slack the command center for real-world relief operations."
