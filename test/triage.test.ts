import { describe, expect, it } from "vitest";
import demoMessages from "../src/data/demoMessages.json" with { type: "json" };
import { triageMessages } from "../src/tools/triage.js";
import type { SearchMessage } from "../src/types.js";

describe("triageMessages", () => {
  it("prioritizes urgent needs and attaches resource matches", () => {
    const items = triageMessages(demoMessages as SearchMessage[]);
    const top = items[0];

    expect(top.classification).toBe("need");
    expect(top.urgency).toBeGreaterThanOrEqual(7);
    expect(top.matchedResource?.reason).toBeTruthy();
  });

  it("filters non-actionable noise", () => {
    const items = triageMessages([
      {
        id: "noise",
        channelId: "C_RANDOM",
        channelName: "random",
        user: "Lee",
        text: "The weather is nice today.",
        timestamp: new Date().toISOString()
      }
    ]);

    expect(items).toHaveLength(0);
  });
});
