export type PlatformEnum = 'LEETCODE' | 'CODECHEF' | 'GEEKSFORGEEKS';

export type GuardStatusEnum = 'PENDING' | 'SUCCESS' | 'FAILED' | 'NO_ACTION';

export interface User {
  id: string;
  email: string;
  fullName: string;
  timezone: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  fullName: string;
  timezone: string;
}

export interface PlatformStatusResult {
  platform: PlatformEnum;
  username: string;
  date: string;
  submittedToday: boolean;
  streakCount: number;
  totalSolved: number;
  message: string;
  checkedAt: string;
}

export interface DashboardData {
  userId: string;
  date: string;
  platformStatuses: Record<PlatformEnum, PlatformStatusResult>;
  streaks: Record<PlatformEnum, number>;
  priorityOrder: PlatformEnum[];
  emergencyTime: string;
  timezone: string;
  botStatus: string;
  dailyLimitReached: boolean;
  lastSubmissionPlatform: string;
  lastSubmissionTime: string;
}

export interface SubmissionHistory {
  id: string;
  userId: string;
  date: string;
  leetCodeSubmitted: boolean;
  codeChefSubmitted: boolean;
  gfgSubmitted: boolean;
  botAction: string;
  selectedPlatform: PlatformEnum | null;
  submissionStatus: GuardStatusEnum;
  problemTitle?: string;
  details: string;
  timestamp: string;
}

export interface SettingsData {
  priorityOrder: PlatformEnum[];
  enabledPlatforms: PlatformEnum[];
  emergencyTime: string;
  timezone: string;
  autoSubmitEnabled: boolean;
  notificationsEnabled: boolean;
}

export interface ProblemPoolItem {
  id?: string;
  platform: PlatformEnum;
  problemId: string;
  problemTitle: string;
  language: string;
  solutionCode: string;
  targetUrl?: string;
  active?: boolean;
}

export interface PlatformConnection {
  platform: PlatformEnum;
  platformUsername: string;
  connected: boolean;
  connectionMessage: string;
}

export interface SubmissionExecutionResponse {
  executed: boolean;
  date: string;
  selectedPlatform: PlatformEnum | null;
  status: GuardStatusEnum;
  submissionId: string | null;
  problemTitle?: string;
  message: string;
  dailyLimitReached: boolean;
  timestamp: string;
}
