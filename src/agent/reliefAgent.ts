import { buildCoordinatorBriefing, triageMessages } from "../tools/triage.js";
import { searchSlackContext } from "../tools/slackRtsSearch.js";
import { renderBriefingBlocks } from "../slack/render.js";
import type { TriageItem } from "../types.js";

type ReliefAgentResult = {
  text: string;
  items: TriageItem[];
  blocks: ReturnType<typeof renderBriefingBlocks>;
};

function normalizePrompt(text: string): string {
  const cleaned = text.replace(/<@[A-Z0-9]+>/g, "").trim();
  if (!cleaned) {
    return "urgent unclaimed relief needs volunteers resources medical transport shelter food";
  }
  return cleaned;
}

export async function runReliefAgent(prompt: string): Promise<ReliefAgentResult> {
  const query = normalizePrompt(prompt);
  const messages = await searchSlackContext({
    query,
    maxResults: 10,
    botToken: process.env.SLACK_BOT_TOKEN,
    mock: process.env.RELIEFBRIDGE_MOCK_RTS !== "false"
  });
  const items = triageMessages(messages);
  const text = buildCoordinatorBriefing(items);
  const blocks = renderBriefingBlocks(items, text);

  return { text, items, blocks };
}
