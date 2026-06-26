export type ReliefCategory =
  | "medical"
  | "shelter"
  | "food"
  | "transport"
  | "accessibility"
  | "logistics"
  | "other";

export type SearchMessage = {
  id: string;
  channelId: string;
  channelName: string;
  user: string;
  text: string;
  timestamp: string;
  permalink?: string;
  threadTs?: string;
};

export type Classification = "need" | "offer" | "update" | "noise";

export type TriageItem = {
  id: string;
  classification: Classification;
  category: ReliefCategory;
  title: string;
  summary: string;
  urgency: number;
  status: "open" | "claimed" | "resolved" | "escalated";
  evidence: SearchMessage[];
  location?: string;
  requestedBy?: string;
  matchedResource?: ResourceMatch;
};

export type Volunteer = {
  id: string;
  name: string;
  slackUserId: string;
  skills: ReliefCategory[];
  location: string;
  availability: string;
  capacity: number;
};

export type Resource = {
  id: string;
  type: ReliefCategory;
  name: string;
  location: string;
  quantity: number;
  unit: string;
  owner: string;
};

export type ReliefCase = {
  id: string;
  title: string;
  category: ReliefCategory;
  status: "open" | "claimed" | "resolved" | "escalated";
  owner?: string;
  location?: string;
  createdAt: string;
};

export type ReliefDirectory = {
  volunteers: Volunteer[];
  resources: Resource[];
  cases: ReliefCase[];
};

export type ResourceMatch = {
  volunteer?: Volunteer;
  resource?: Resource;
  reason: string;
  score: number;
};
