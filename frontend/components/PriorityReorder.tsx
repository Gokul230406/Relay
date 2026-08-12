'use client';

import { PlatformEnum } from '@/types';

interface Props {
  priority: PlatformEnum[];
  onChange: (newPriority: PlatformEnum[]) => void;
}

const PLATFORM_LABELS: Record<PlatformEnum, { name: string; icon: string }> = {
  LEETCODE: { name: 'LeetCode', icon: '💻' },
  CODECHEF: { name: 'CodeChef', icon: '👨‍🍳' },
  GEEKSFORGEEKS: { name: 'GeeksforGeeks', icon: '🚀' },
};

export default function PriorityReorder({ priority, onChange }: Props) {
  const moveUp = (index: number) => {
    if (index === 0) return;
    const newPriority = [...priority];
    const temp = newPriority[index];
    newPriority[index] = newPriority[index - 1];
    newPriority[index - 1] = temp;
    onChange(newPriority);
  };

  const moveDown = (index: number) => {
    if (index === priority.length - 1) return;
    const newPriority = [...priority];
    const temp = newPriority[index];
    newPriority[index] = newPriority[index + 1];
    newPriority[index + 1] = temp;
    onChange(newPriority);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between text-xs text-slate-400">
        <span>Order determines which platform receives submission when multiple are missing</span>
        <span className="font-mono">Highest Priority → Lowest</span>
      </div>

      <div className="space-y-2">
        {priority.map((plat, idx) => {
          const item = PLATFORM_LABELS[plat];
          return (
            <div
              key={plat}
              className="flex items-center justify-between p-3.5 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 transition-colors"
            >
              <div className="flex items-center gap-3">
                <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-orange-500/10 text-xs font-bold text-orange-400 border border-orange-500/20">
                  #{idx + 1}
                </span>
                <span className="text-xl">{item.icon}</span>
                <span className="font-semibold text-slate-200">{item.name}</span>
              </div>

              <div className="flex items-center gap-1">
                <button
                  type="button"
                  onClick={() => moveUp(idx)}
                  disabled={idx === 0}
                  className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 disabled:opacity-30 disabled:cursor-not-allowed text-xs"
                >
                  ▲
                </button>
                <button
                  type="button"
                  onClick={() => moveDown(idx)}
                  disabled={idx === priority.length - 1}
                  className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 disabled:opacity-30 disabled:cursor-not-allowed text-xs"
                >
                  ▼
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
