'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api, getAuthToken, setAuthToken, removeAuthToken } from '@/lib/api';
import { DashboardData, PlatformEnum } from '@/types';
import StreakCard from '@/components/StreakCard';
import EmergencyTimer from '@/components/EmergencyTimer';
import PlatformStatusBadge from '@/components/PlatformStatusBadge';

export default function DashboardPage() {
  const router = useRouter();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError(null);

      let token = getAuthToken();
      if (!token) {
        try {
          const authRes = await api.login('demo@streaksaver.dev', 'password123');
          token = authRes.token;
        } catch {
          const regRes = await api.register('demo@streaksaver.dev', 'password123', 'Demo Developer');
          token = regRes.token;
        }
      }

      try {
        const res = await api.getDashboard();
        setData(res);
      } catch (err: any) {
        if (err.message && (err.message.includes('403') || err.message.includes('401'))) {
          removeAuthToken();
          try {
            const authRes = await api.login('demo@streaksaver.dev', 'password123');
            setAuthToken(authRes.token);
          } catch {
            const regRes = await api.register('demo@streaksaver.dev', 'password123', 'Demo Developer');
            setAuthToken(regRes.token);
          }
          const retryRes = await api.getDashboard();
          setData(retryRes);
        } else {
          throw err;
        }
      }
    } catch (err: any) {
      console.error('Failed to load dashboard', err);
      setError(err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4">
        <div className="h-12 w-12 rounded-full border-4 border-orange-500/20 border-t-orange-500 animate-spin"></div>
        <p className="text-sm font-medium text-slate-400">Loading Relay Dashboard...</p>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="rounded-2xl border border-rose-500/30 bg-rose-950/20 p-8 text-center max-w-xl mx-auto my-12">
        <span className="text-4xl">⚠️</span>
        <h2 className="text-xl font-bold text-white mt-3">Failed to connect to Relay Backend</h2>
        <p className="text-sm text-slate-400 mt-2">{error}</p>
        <button
          onClick={loadDashboard}
          className="mt-6 px-6 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 text-white font-semibold text-sm transition-colors cursor-pointer"
        >
          Retry Connection
        </button>
      </div>
    );
  }

  const platforms: PlatformEnum[] = data.priorityOrder || ['LEETCODE', 'CODECHEF', 'GEEKSFORGEEKS'];

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Relay Streak Dashboard</h1>
          <p className="text-slate-400 text-sm mt-1">
            Monitoring submission activity across all 3 platforms for date: <span className="text-slate-200 font-mono font-semibold">{data.date}</span>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={loadDashboard}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white text-xs font-semibold hover:border-slate-700 transition-colors cursor-pointer"
          >
            <span>🔄</span> Refresh Status
          </button>
        </div>
      </div>

      {/* Emergency Timer Card */}
      <EmergencyTimer
        emergencyTime={data.emergencyTime}
        timezone={data.timezone}
        botStatus={data.botStatus}
        dailyLimitReached={data.dailyLimitReached}
        onUpdate={loadDashboard}
      />

      {/* Current Streaks Section */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <span>🔥</span> Current Streaks & Platform Status
          </h2>
          <span className="text-xs text-slate-400">Order by Priority Configuration</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {platforms.map((plat, idx) => (
            <StreakCard
              key={plat}
              platform={plat}
              status={data.platformStatuses[plat]}
              priorityRank={idx + 1}
            />
          ))}
        </div>
      </div>

      {/* Today's Checklist Table */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6 backdrop-blur-md">
        <h3 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
          <span>📋</span> Today's Platform Submission Checklist
        </h3>

        <div className="divide-y divide-slate-800">
          {platforms.map((plat) => {
            const st = data.platformStatuses[plat];
            const isSub = st?.submittedToday ?? false;
            return (
              <div key={plat} className="py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="text-xl">
                    {plat === 'LEETCODE' ? '💻' : plat === 'CODECHEF' ? '👨‍🍳' : '🚀'}
                  </span>
                  <div>
                    <h4 className="font-semibold text-slate-200">{plat === 'LEETCODE' ? 'LeetCode' : plat === 'CODECHEF' ? 'CodeChef' : 'GeeksforGeeks'}</h4>
                    <p className="text-xs text-slate-400">{st?.message || 'Checked'}</p>
                  </div>
                </div>

                <PlatformStatusBadge submitted={isSub} />
              </div>
            );
          })}
        </div>
      </div>

      {/* Bottom Safety Guard Invariant Info */}
      <div className="rounded-2xl border border-orange-500/20 bg-orange-950/10 p-5 flex items-start gap-4">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-orange-500/20 text-orange-400 font-bold">
          🛡️
        </div>
        <div>
          <h4 className="font-bold text-orange-300 text-sm">Relay Multi-Platform Streak Guarantee</h4>
          <p className="text-xs text-slate-400 mt-1 leading-relaxed">
            Relay monitors your profiles across LeetCode, CodeChef, and GeeksforGeeks. If no submission is detected on any enabled platform when your emergency cutoff time arrives, Relay automatically executes a pre-approved Java problem solution from your pool.
          </p>
        </div>
      </div>
    </div>
  );
}
