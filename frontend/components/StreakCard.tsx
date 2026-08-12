'use client';

import { PlatformEnum, PlatformStatusResult } from '@/types';
import PlatformStatusBadge from './PlatformStatusBadge';

interface Props {
  platform: PlatformEnum;
  status?: PlatformStatusResult;
  priorityRank: number;
}

const PLATFORM_META: Record<PlatformEnum, { name: string; icon: string; color: string; bgGradient: string }> = {
  LEETCODE: {
    name: 'LeetCode',
    icon: '💻',
    color: 'from-amber-400 to-orange-500',
    bgGradient: 'from-amber-500/10 via-slate-900 to-slate-900',
  },
  CODECHEF: {
    name: 'CodeChef',
    icon: '👨‍🍳',
    color: 'from-amber-600 to-yellow-600',
    bgGradient: 'from-amber-600/10 via-slate-900 to-slate-900',
  },
  GEEKSFORGEEKS: {
    name: 'GeeksforGeeks',
    icon: '🚀',
    color: 'from-emerald-400 to-teal-500',
    bgGradient: 'from-emerald-500/10 via-slate-900 to-slate-900',
  },
};

export default function StreakCard({ platform, status, priorityRank }: Props) {
  const meta = PLATFORM_META[platform];
  const submitted = status?.submittedToday ?? false;
  const streak = status?.streakCount ?? 0;
  const totalSolved = status?.totalSolved ?? 0;

  return (
    <div className={`relative overflow-hidden rounded-2xl border border-slate-800 bg-gradient-to-br ${meta.bgGradient} p-5 shadow-lg backdrop-blur-sm transition-all hover:border-slate-700 hover:shadow-xl`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-slate-900 border border-slate-800 text-2xl shadow-inner">
            {meta.icon}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-slate-100 text-lg">{meta.name}</h3>
              <span className="rounded-md bg-slate-800 px-2 py-0.5 text-[10px] font-semibold text-slate-400 border border-slate-700">
                Priority #{priorityRank}
              </span>
            </div>
            <p className="text-xs text-slate-400">Handle: <span className="text-slate-300 font-mono">{status?.username || 'Not Connected'}</span></p>
          </div>
        </div>

        <PlatformStatusBadge submitted={submitted} message={status?.message} />
      </div>

      <div className="mt-5 grid grid-cols-2 gap-3 pt-4 border-t border-slate-800/80">
        <div className="rounded-xl bg-slate-950/60 p-3 border border-slate-800/50">
          <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">Current Streak</span>
          <div className="flex items-center gap-1.5 mt-1">
            <span className="text-xl">🔥</span>
            <span className="text-2xl font-black text-white">{streak}</span>
            <span className="text-xs text-slate-400">days</span>
          </div>
        </div>

        <div className="rounded-xl bg-slate-950/60 p-3 border border-slate-800/50">
          <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">Total Solved</span>
          <div className="flex items-center gap-1.5 mt-1">
            <span className="text-xl font-extrabold text-slate-200">{totalSolved}</span>
            <span className="text-xs text-slate-400">problems</span>
          </div>
        </div>
      </div>
    </div>
  );
}
