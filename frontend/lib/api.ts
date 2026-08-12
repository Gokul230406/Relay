import {
  AuthResponse,
  DashboardData,
  PlatformConnection,
  PlatformEnum,
  ProblemPoolItem,
  SettingsData,
  SubmissionExecutionResponse,
  SubmissionHistory,
} from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export function getAuthToken(): string | null {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('streaksaver_token');
  }
  return null;
}

export function setAuthToken(token: string) {
  if (typeof window !== 'undefined') {
    localStorage.setItem('streaksaver_token', token);
  }
}

export function removeAuthToken() {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('streaksaver_token');
  }
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401 || response.status === 403) {
    removeAuthToken();
    if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login') && !options.headers) {
      window.location.href = '/login';
    }
  }

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `Request failed with status ${response.status}`);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const api = {
  // Auth
  register: async (email: string, password: string, fullName?: string) => {
    const res = await request<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, fullName }),
    });
    if (res.token) setAuthToken(res.token);
    return res;
  },

  login: async (email: string, password: string) => {
    const res = await request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (res.token) setAuthToken(res.token);
    return res;
  },

  logout: () => {
    removeAuthToken();
    return request<{ message: string }>('/auth/logout', {
      method: 'POST',
    });
  },

  // Dashboard
  getDashboard: () => request<DashboardData>('/dashboard'),

  // Streak
  checkStreaks: () =>
    request<Record<PlatformEnum, any>>('/streak/check', {
      method: 'POST',
    }),

  triggerEmergencySubmit: () =>
    request<SubmissionExecutionResponse>('/streak/emergency-submit', {
      method: 'POST',
    }),

  // Platforms
  getPlatformConnections: () => request<PlatformConnection[]>('/platforms/status'),

  connectPlatform: (platform: PlatformEnum, username: string) =>
    request<PlatformConnection>(`/platforms/${platform}/connect`, {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),

  disconnectPlatform: (platform: PlatformEnum) =>
    request<PlatformConnection>(`/platforms/${platform}/disconnect`, {
      method: 'DELETE',
    }),

  // History
  getHistory: () => request<SubmissionHistory[]>('/history'),

  // Settings
  getSettings: () => request<SettingsData>('/settings'),

  updateSettings: (settings: Partial<SettingsData>) =>
    request<SettingsData>('/settings', {
      method: 'PUT',
      body: JSON.stringify(settings),
    }),

  // Problem Pool
  getProblemPool: () => request<ProblemPoolItem[]>('/problem-pool'),

  addProblemToPool: (platform: PlatformEnum, item: Partial<ProblemPoolItem>) =>
    request<ProblemPoolItem>(`/problem-pool/${platform}`, {
      method: 'POST',
      body: JSON.stringify(item),
    }),

  deleteProblemFromPool: (platform: PlatformEnum, id: string) =>
    request<void>(`/problem-pool/${platform}/${id}`, {
      method: 'DELETE',
    }),
};
