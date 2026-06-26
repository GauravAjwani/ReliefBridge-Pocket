import { suggestMatch } from "../mcp/reliefDirectory.js";
import type { Classification, ReliefCategory, SearchMessage, TriageItem } from "../types.js";

const categoryRules: Array<[ReliefCategory, RegExp]> = [
  ["medical", /\b(medical|medicine|insulin|clinic|doctor|nurse|fridge|cooler|prescription)\b/i],
  ["transport", /\b(transport|ride|driver|van|pickup|wheelchair-accessible|accessible van|road)\b/i],
  ["shelter", /\b(shelter|beds?|gym|housing|evacuee|evacuation)\b/i],
  ["food", /\b(food|meal|kitchen|water|snack|grocer|formula)\b/i],
  ["accessibility", /\b(wheelchair|accessible|interpreter|mobility|disabled|disability)\b/i],
  ["logistics", /\b(drop-?off|badge|supply|warehouse|inventory|coordinate|dispatch)\b/i]
];

const locationPatterns = [
  /(?:at|to|near|from)\s+([A-Z][A-Za-z]+(?:\s+[A-Z][A-Za-z]+){0,3})/,
  /\b(North Shelter|West High School|Community Kitchen|East Gym|Riverside Apartments)\b/i
];

function classify(text: string): Classification {
  if (/\b(urgent|need|needs|request|missing|required|time sensitive|before)\b/i.test(text)) return "need";
  if (/\b(can bring|can send|available|have|has|offering|donate|I can)\b/i.test(text)) return "offer";
  if (/\b(done|resolved|confirmed|update|arrived|completed|claimed)\b/i.test(text)) return "update";
  return "noise";
}

function detectCategory(text: string): ReliefCategory {
  for (const [category, pattern] of categoryRules) {
    if (pattern.test(text)) return category;
  }
  return "other";
}

function detectLocation(text: string): string | undefined {
  for (const pattern of locationPatterns) {
    const match = text.match(pattern);
    if (match?.[1]) {
      return match[1].replace(/\s+(before|and|with|who|if)$/i, "").trim();
    }
  }
  return undefined;
}

function scoreUrgency(text: string, classification: Classification): number {
  let score = classification === "need" ? 4 : classification === "offer" ? 2 : 1;
  if (/\burgent|asap|critical|time sensitive\b/i.test(text)) score += 3;
  if (/\bmedical|insulin|seniors?|wheelchair|road closes|children|evacuee\b/i.test(text)) score += 2;
  if (/\bbefore|tonight|now|today\b/i.test(text)) score += 1;
  return Math.max(1, Math.min(10, score));
}

function titleFrom(text: string, category: ReliefCategory): string {
  const cleaned = text.replace(/\s+/g, " ").trim();
  const sentence = cleaned.split(/[.!?]/)[0] ?? cleaned;
  const title = sentence.replace(/^urgent:\s*/i, "").slice(0, 90);
  return title || `${category} coordination item`;
}

function dedupeKey(item: TriageItem): string {
  return [item.classification, item.category, item.location ?? "", item.title.toLowerCase().slice(0, 32)].join("|");
}

function summarize(message: SearchMessage, category: ReliefCategory, classification: Classification): string {
  const channel = `#${message.channelName}`;
  if (classification === "need") {
    return `${message.user} raised a ${category} need in ${channel}: ${message.text}`;
  }
  if (classification === "offer") {
    return `${message.user} offered ${category} support in ${channel}: ${message.text}`;
  }
  if (classification === "update") {
    return `${message.user} posted a status update in ${channel}: ${message.text}`;
  }
  return `${message.user} mentioned related context in ${channel}: ${message.text}`;
}

export function triageMessages(messages: SearchMessage[]): TriageItem[] {
  const seen = new Map<string, TriageItem>();

  for (const message of messages) {
    const classification = classify(message.text);
    if (classification === "noise") continue;

    const category = detectCategory(message.text);
    const location = detectLocation(message.text);
    const urgency = scoreUrgency(message.text, classification);
    const item: TriageItem = {
      id: message.id,
      classification,
      category,
      title: titleFrom(message.text, category),
      summary: summarize(message, category, classification),
      urgency,
      status: classification === "update" ? "claimed" : "open",
      evidence: [message],
      location,
      requestedBy: message.user
    };

    const key = dedupeKey(item);
    const existing = seen.get(key);
    if (existing) {
      existing.evidence.push(message);
      existing.urgency = Math.max(existing.urgency, item.urgency);
      existing.summary = `${existing.summary}\nRelated: ${message.user} in #${message.channelName}: ${message.text}`;
      continue;
    }

    seen.set(key, item);
  }

  return [...seen.values()]
    .map((item) => ({
      ...item,
      matchedResource: item.classification === "need" ? suggestMatch(item) : undefined
    }))
    .sort((a, b) => b.urgency - a.urgency || (b.matchedResource?.score ?? 0) - (a.matchedResource?.score ?? 0));
}

export function buildCoordinatorBriefing(items: TriageItem[]): string {
  if (!items.length) {
    return "No open relief needs or resource offers were found in the current Slack context.";
  }

  const needs = items.filter((item) => item.classification === "need");
  const offers = items.filter((item) => item.classification === "offer");
  const topNeed = needs[0];

  const lines = [
    `Found ${needs.length} open need${needs.length === 1 ? "" : "s"} and ${offers.length} offer${offers.length === 1 ? "" : "s"}.`,
    topNeed
      ? `Top priority: ${topNeed.title} (${topNeed.urgency}/10 urgency). ${topNeed.matchedResource?.reason ?? "No match yet."}`
      : "No urgent need is currently above the action threshold."
  ];

  return lines.join(" ");
}
