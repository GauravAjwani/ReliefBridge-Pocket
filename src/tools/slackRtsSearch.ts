import { randomUUID } from "node:crypto";
import demoMessages from "../data/demoMessages.json" with { type: "json" };
import type { SearchMessage } from "../types.js";

const RTS_ENDPOINT = "https://slack.com/api/agents.search";

type RtsSearchOptions = {
  query: string;
  channels?: string[];
  maxResults?: number;
  botToken?: string;
  mock?: boolean;
};

function tokenize(value: string): string[] {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, " ")
    .split(/\s+/)
    .filter((word) => word.length > 2);
}

function searchMockMessages(query: string, channels?: string[], maxResults = 8): SearchMessage[] {
  const queryTerms = new Set(tokenize(query));
  const messages = demoMessages as SearchMessage[];

  return messages
    .filter((message) => !channels?.length || channels.includes(message.channelId) || channels.includes(message.channelName))
    .map((message) => {
      const textTerms = tokenize(`${message.channelName} ${message.text}`);
      const score = textTerms.reduce((sum, term) => sum + Number(queryTerms.has(term)), 0);
      const reliefSignal = /(urgent|need|available|can|shelter|medical|transport|meals|insulin|wheelchair)/i.test(
        message.text
      )
        ? 2
        : 0;
      return { message, score: score + reliefSignal };
    })
    .filter(({ score }) => score > 0 || queryTerms.size === 0)
    .sort((a, b) => b.score - a.score)
    .slice(0, maxResults)
    .map(({ message }) => message);
}

function parseSseLine(line: string): SearchMessage | undefined {
  if (!line.startsWith("data:")) return undefined;
  const payload = line.slice("data:".length).trim();
  if (!payload || payload === "[DONE]") return undefined;

  try {
    const parsed = JSON.parse(payload) as {
      id?: string;
      channel_id?: string;
      channel_name?: string;
      user?: string;
      text?: string;
      ts?: string;
      timestamp?: string;
      permalink?: string;
      thread_ts?: string;
    };

    if (!parsed.text) return undefined;
    return {
      id: parsed.id ?? parsed.ts ?? randomUUID(),
      channelId: parsed.channel_id ?? "unknown",
      channelName: parsed.channel_name ?? "unknown",
      user: parsed.user ?? "unknown",
      text: parsed.text,
      timestamp: parsed.timestamp ?? parsed.ts ?? new Date().toISOString(),
      permalink: parsed.permalink,
      threadTs: parsed.thread_ts
    };
  } catch {
    return undefined;
  }
}

async function parseSseResponse(response: Response): Promise<SearchMessage[]> {
  const reader = response.body?.getReader();
  if (!reader) return [];

  const decoder = new TextDecoder();
  const results: SearchMessage[] = [];
  let buffer = "";

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    const lines = buffer.split(/\r?\n/);
    buffer = lines.pop() ?? "";
    for (const line of lines) {
      const parsed = parseSseLine(line);
      if (parsed) results.push(parsed);
    }
  }

  const final = parseSseLine(buffer);
  if (final) results.push(final);
  return results;
}

export async function searchSlackContext(options: RtsSearchOptions): Promise<SearchMessage[]> {
  const useMock = options.mock ?? process.env.RELIEFBRIDGE_MOCK_RTS !== "false";
  if (useMock || !options.botToken) {
    return searchMockMessages(options.query, options.channels, options.maxResults);
  }

  const response = await fetch(RTS_ENDPOINT, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${options.botToken}`,
      "Content-Type": "application/json",
      Accept: "text/event-stream"
    },
    body: JSON.stringify({
      query: options.query,
      max_results: options.maxResults ?? 8,
      channels: options.channels
    })
  });

  if (!response.ok) {
    throw new Error(`Slack RTS search failed: ${response.status} ${await response.text()}`);
  }

  return parseSseResponse(response);
}
