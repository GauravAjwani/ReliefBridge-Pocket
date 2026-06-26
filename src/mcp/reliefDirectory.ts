import directoryData from "../data/demoDirectory.json" with { type: "json" };
import type {
  ReliefCase,
  ReliefCategory,
  ReliefDirectory,
  Resource,
  ResourceMatch,
  TriageItem,
  Volunteer
} from "../types.js";

const directory = directoryData as ReliefDirectory;

function sameLocation(a?: string, b?: string): boolean {
  if (!a || !b) return false;
  return a.toLowerCase() === b.toLowerCase();
}

function locationOverlap(a?: string, b?: string): boolean {
  if (!a || !b) return false;
  const left = a.toLowerCase();
  const right = b.toLowerCase();
  return left.includes(right) || right.includes(left);
}

export function listDirectory(): ReliefDirectory {
  return structuredClone(directory);
}

export function searchResources(category?: ReliefCategory, location?: string): Resource[] {
  return directory.resources
    .filter((resource) => !category || resource.type === category)
    .filter((resource) => !location || locationOverlap(resource.location, location))
    .sort((a, b) => Number(sameLocation(b.location, location)) - Number(sameLocation(a.location, location)));
}

export function searchVolunteers(category?: ReliefCategory, location?: string): Volunteer[] {
  return directory.volunteers
    .filter((volunteer) => !category || volunteer.skills.includes(category))
    .filter((volunteer) => !location || locationOverlap(volunteer.location, location) || volunteer.availability === "now")
    .sort((a, b) => b.capacity - a.capacity);
}

export function createCase(input: Omit<ReliefCase, "id" | "createdAt">): ReliefCase {
  const reliefCase: ReliefCase = {
    ...input,
    id: `case-${String(directory.cases.length + 1).padStart(3, "0")}`,
    createdAt: new Date().toISOString()
  };
  directory.cases.push(reliefCase);
  return reliefCase;
}

export function updateCaseStatus(
  id: string,
  status: ReliefCase["status"],
  owner?: string
): ReliefCase | undefined {
  const reliefCase = directory.cases.find((item) => item.id === id);
  if (!reliefCase) return undefined;
  reliefCase.status = status;
  reliefCase.owner = owner ?? reliefCase.owner;
  return reliefCase;
}

export function suggestMatch(item: Pick<TriageItem, "category" | "location" | "urgency">): ResourceMatch {
  const volunteer = searchVolunteers(item.category, item.location)[0];
  const resource = searchResources(item.category, item.location)[0];
  const locationBonus = Number(
    sameLocation(volunteer?.location, item.location) || sameLocation(resource?.location, item.location)
  );
  const score = item.urgency + (volunteer ? 2 : 0) + (resource ? 2 : 0) + locationBonus;

  if (!volunteer && !resource) {
    return {
      reason: "No exact directory match yet. Escalate to the coordinator channel for manual assignment.",
      score
    };
  }

  const parts = [
    volunteer ? `${volunteer.name} is available ${volunteer.availability}` : undefined,
    resource ? `${resource.quantity} ${resource.unit} of ${resource.name} at ${resource.location}` : undefined
  ].filter(Boolean);

  return {
    volunteer,
    resource,
    reason: parts.join("; "),
    score
  };
}
