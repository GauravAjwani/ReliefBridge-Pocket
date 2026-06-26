import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import {
  createCase,
  listDirectory,
  searchResources,
  searchVolunteers,
  suggestMatch,
  updateCaseStatus
} from "./reliefDirectory.js";
import type { ReliefCategory } from "../types.js";

const categorySchema = z.enum([
  "medical",
  "shelter",
  "food",
  "transport",
  "accessibility",
  "logistics",
  "other"
]);

const statusSchema = z.enum(["open", "claimed", "resolved", "escalated"]);

const server = new McpServer({
  name: "relief-directory",
  version: "0.1.0"
});

server.tool("list_directory", "Return the demo relief volunteer, resource, and case directory.", {}, async () => ({
  content: [{ type: "text", text: JSON.stringify(listDirectory(), null, 2) }]
}));

server.tool(
  "search_resources",
  "Find available supplies or facilities by relief category and optional location.",
  {
    category: categorySchema.optional(),
    location: z.string().optional()
  },
  async ({ category, location }) => ({
    content: [
      {
        type: "text",
        text: JSON.stringify(searchResources(category as ReliefCategory | undefined, location), null, 2)
      }
    ]
  })
);

server.tool(
  "search_volunteers",
  "Find volunteers by relief category and optional location.",
  {
    category: categorySchema.optional(),
    location: z.string().optional()
  },
  async ({ category, location }) => ({
    content: [
      {
        type: "text",
        text: JSON.stringify(searchVolunteers(category as ReliefCategory | undefined, location), null, 2)
      }
    ]
  })
);

server.tool(
  "suggest_match",
  "Suggest the best volunteer and resource match for a triaged relief need.",
  {
    category: categorySchema,
    location: z.string().optional(),
    urgency: z.number().min(1).max(10)
  },
  async ({ category, location, urgency }) => ({
    content: [
      {
        type: "text",
        text: JSON.stringify(suggestMatch({ category: category as ReliefCategory, location, urgency }), null, 2)
      }
    ]
  })
);

server.tool(
  "create_case",
  "Create a case in the demo relief directory.",
  {
    title: z.string(),
    category: categorySchema,
    status: statusSchema.default("open"),
    owner: z.string().optional(),
    location: z.string().optional()
  },
  async ({ title, category, status, owner, location }) => ({
    content: [
      {
        type: "text",
        text: JSON.stringify(
          createCase({ title, category: category as ReliefCategory, status, owner, location }),
          null,
          2
        )
      }
    ]
  })
);

server.tool(
  "update_case_status",
  "Update a demo case status after a Slack action is claimed, resolved, or escalated.",
  {
    id: z.string(),
    status: statusSchema,
    owner: z.string().optional()
  },
  async ({ id, status, owner }) => {
    const updated = updateCaseStatus(id, status, owner);
    return {
      content: [
        {
          type: "text",
          text: JSON.stringify(updated ?? { error: `Case ${id} was not found.` }, null, 2)
        }
      ],
      isError: !updated
    };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
