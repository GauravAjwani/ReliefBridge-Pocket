import type { KnownBlock, KnownBlock as SlackBlock } from "@slack/types";
import type { TriageItem } from "../types.js";

function evidenceText(item: TriageItem): string {
  return item.evidence
    .slice(0, 2)
    .map((message) => {
      const source = message.permalink ? `<${message.permalink}|#${message.channelName}>` : `#${message.channelName}`;
      return `${source} by ${message.user}`;
    })
    .join(", ");
}

function matchText(item: TriageItem): string {
  if (!item.matchedResource) return "No directory match yet.";
  const volunteer = item.matchedResource.volunteer ? `Volunteer: ${item.matchedResource.volunteer.name}. ` : "";
  const resource = item.matchedResource.resource
    ? `Resource: ${item.matchedResource.resource.quantity} ${item.matchedResource.resource.unit} ${item.matchedResource.resource.name}. `
    : "";
  return `${volunteer}${resource}${item.matchedResource.reason}`;
}

function actionValue(item: TriageItem): string {
  return JSON.stringify({
    id: item.id,
    title: item.title,
    category: item.category,
    location: item.location
  });
}

export function renderBriefingBlocks(items: TriageItem[], briefing: string): KnownBlock[] {
  const blocks: SlackBlock[] = [
    {
      type: "header",
      text: {
        type: "plain_text",
        text: "ReliefBridge Priority Briefing"
      }
    },
    {
      type: "section",
      text: {
        type: "mrkdwn",
        text: briefing
      }
    },
    { type: "divider" }
  ];

  for (const item of items.slice(0, 5)) {
    blocks.push({
      type: "section",
      text: {
        type: "mrkdwn",
        text: `*${item.urgency}/10 ${item.classification.toUpperCase()}* - ${item.title}\n${item.summary}\n*Match:* ${matchText(item)}\n*Evidence:* ${evidenceText(item)}`
      }
    });

    if (item.classification === "need") {
      blocks.push({
        type: "actions",
        elements: [
          {
            type: "button",
            text: { type: "plain_text", text: "Claim" },
            style: "primary",
            action_id: "relief_claim",
            value: actionValue(item)
          },
          {
            type: "button",
            text: { type: "plain_text", text: "Resolve" },
            action_id: "relief_resolve",
            value: actionValue(item)
          },
          {
            type: "button",
            text: { type: "plain_text", text: "Escalate" },
            style: "danger",
            action_id: "relief_escalate",
            value: actionValue(item)
          }
        ]
      });
    }

    blocks.push({ type: "divider" });
  }

  blocks.push({
    type: "context",
    elements: [
      {
        type: "mrkdwn",
        text: "Free demo mode uses seeded Slack search data unless `RELIEFBRIDGE_MOCK_RTS=false` is set."
      }
    ]
  });

  return blocks as KnownBlock[];
}

export function renderStatusText(action: "claimed" | "resolved" | "escalated", title: string, userId?: string): string {
  const actor = userId ? `<@${userId}>` : "A coordinator";
  if (action === "claimed") return `${actor} claimed: ${title}`;
  if (action === "resolved") return `${actor} resolved: ${title}`;
  return `${actor} escalated: ${title}`;
}
