import "dotenv/config";
import { App } from "@slack/bolt";
import { runReliefAgent } from "./agent/reliefAgent.js";
import { createCase, updateCaseStatus } from "./mcp/reliefDirectory.js";
import { renderStatusText } from "./slack/render.js";
import type { ReliefCase } from "./types.js";

type ActionStatus = Exclude<ReliefCase["status"], "open">;

const hasSlackCredentials =
  Boolean(process.env.SLACK_BOT_TOKEN) &&
  Boolean(process.env.SLACK_APP_TOKEN) &&
  Boolean(process.env.SLACK_SIGNING_SECRET);

if (!hasSlackCredentials) {
  console.log(
    "ReliefBridge is ready, but Slack credentials are missing. Use `npm run demo` for the free local demo or copy `.env.sample` to `.env` for Slack."
  );
  process.exit(0);
}

const app = new App({
  token: process.env.SLACK_BOT_TOKEN,
  appToken: process.env.SLACK_APP_TOKEN,
  signingSecret: process.env.SLACK_SIGNING_SECRET,
  socketMode: true
});

app.event("app_mention", async ({ event, say }) => {
  const result = await runReliefAgent(event.text ?? "");
  await say({ text: result.text, blocks: result.blocks, thread_ts: event.ts });
});

app.message(async ({ message, say }) => {
  if ("subtype" in message && message.subtype) return;
  if (!("channel_type" in message) || message.channel_type !== "im") return;

  const result = await runReliefAgent("text" in message ? message.text ?? "" : "");
  await say({ text: result.text, blocks: result.blocks });
});

async function recordAction(value: string | undefined, status: ActionStatus, userId?: string): Promise<string> {
  const payload = value ? (JSON.parse(value) as { id: string; title: string; category: ReliefCase["category"]; location?: string }) : undefined;
  if (!payload) return renderStatusText(status, "unknown relief item", userId);

  const owner = userId ? `<@${userId}>` : undefined;
  const updated = updateCaseStatus(payload.id, status, owner);
  if (!updated) {
    createCase({
      title: payload.title,
      category: payload.category,
      location: payload.location,
      owner,
      status
    });
  }

  return renderStatusText(status, payload.title, userId);
}

app.action("relief_claim", async ({ ack, body, action, respond }) => {
  await ack();
  const value = "value" in action ? action.value : undefined;
  await respond({ text: await recordAction(value, "claimed", body.user?.id), replace_original: false });
});

app.action("relief_resolve", async ({ ack, body, action, respond }) => {
  await ack();
  const value = "value" in action ? action.value : undefined;
  await respond({ text: await recordAction(value, "resolved", body.user?.id), replace_original: false });
});

app.action("relief_escalate", async ({ ack, body, action, respond }) => {
  await ack();
  const value = "value" in action ? action.value : undefined;
  await respond({ text: await recordAction(value, "escalated", body.user?.id), replace_original: false });
});

await app.start();
console.log("ReliefBridge Slack agent is running in Socket Mode.");
