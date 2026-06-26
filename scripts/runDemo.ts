import "dotenv/config";
import { runReliefAgent } from "../src/agent/reliefAgent.js";

process.env.RELIEFBRIDGE_MOCK_RTS = process.env.RELIEFBRIDGE_MOCK_RTS ?? "true";

const prompt =
  process.argv.slice(2).join(" ") ||
  "What urgent requests are still unclaimed, and who can help with transport or medical needs?";

const result = await runReliefAgent(prompt);

console.log("ReliefBridge demo prompt:");
console.log(prompt);
console.log("");
console.log(result.text);
console.log("");

for (const [index, item] of result.items.entries()) {
  console.log(`${index + 1}. [${item.urgency}/10] ${item.title}`);
  console.log(`   Category: ${item.category}; status: ${item.status}; location: ${item.location ?? "unknown"}`);
  console.log(`   Match: ${item.matchedResource?.reason ?? "none"}`);
  console.log(`   Evidence: ${item.evidence.map((message) => `#${message.channelName}`).join(", ")}`);
}
