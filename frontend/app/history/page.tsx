'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { SubmissionHistory } from '@/types';

export default function HistoryPage() {
  const [history, setHistory] = useState<SubmissionHistory[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadHistory() {
      try {
        const data = await api.getHistory();
        setHistory(data);
      } catch (err) {
        console.error('Failed to load history', err);
      } finally {
        setLoading(false);
      }
    }
    loadHistory();
  }, []);

  const formatTimestamp = (ts?: string) => {
    if (!ts) return '';
    try {
      return new Date(ts).toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        second: '2-digit',
        hour12: true,
      });
    } catch {
      return ts;
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">Relay History & Audit Log</h1>
        <p className="text-slate-400 text-sm mt-1">
          Historical record of all platform checks, streak status evaluations, and submission executions.
        </p>
      </div>

      {loading ? (
        <div className="p-12 text-center text-slate-400">Loading history records...</div>
      ) : history.length === 0 ? (
        <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-12 text-center text-slate-400">
          No historical actions recorded yet. Manual or scheduled emergency runs will appear here.
        </div>
      ) : (
        <div className="space-y-4">
          {history.map((item, idx) => (
            <div
              key={item.id || item.timestamp || `hist_${idx}`}
              className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 backdrop-blur-md hover:border-slate-700 transition-colors"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-mono text-orange-400 font-semibold uppercase tracking-wider">Date</span>
                    <span className="text-xs text-slate-400 font-mono">({formatTimestamp(item.timestamp)})</span>
                  </div>
                  <h3 className="text-xl font-bold text-white font-mono mt-0.5">{item.date}</h3>
                </div>

                <div className="flex items-center gap-2">
                  <span className={`px-3 py-1 rounded-full text-xs font-bold border ${
                    item.submissionStatus === 'SUCCESS'
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                      : item.submissionStatus === 'NO_ACTION'
                      ? 'bg-blue-500/10 text-blue-400 border-blue-500/30'
                      : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                  }`}>
                    {item.botAction || 'Submission Action'}
                  </span>
                </div>
              </div>

              {/* Status grid */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 my-4">
                <div className="rounded-xl bg-slate-950/60 p-3 border border-slate-800 flex items-center justify-between">
                  <span className="text-xs font-medium text-slate-300">LeetCode</span>
                  <span className={item.leetCodeSubmitted ? 'text-emerald-400 font-bold text-xs' : 'text-amber-400 font-bold text-xs'}>
                    {item.leetCodeSubmitted ? '✓ Submitted' : '⚠ Missing'}
                  </span>
                </div>

                <div className="rounded-xl bg-slate-950/60 p-3 border border-slate-800 flex items-center justify-between">
                  <span className="text-xs font-medium text-slate-300">CodeChef</span>
                  <span className={item.codeChefSubmitted ? 'text-emerald-400 font-bold text-xs' : 'text-amber-400 font-bold text-xs'}>
                    {item.codeChefSubmitted ? '✓ Submitted' : '⚠ Missing'}
                  </span>
                </div>

                <div className="rounded-xl bg-slate-950/60 p-3 border border-slate-800 flex items-center justify-between">
                  <span className="text-xs font-medium text-slate-300">GeeksforGeeks</span>
                  <span className={item.gfgSubmitted ? 'text-emerald-400 font-bold text-xs' : 'text-amber-400 font-bold text-xs'}>
                    {item.gfgSubmitted ? '✓ Submitted' : '⚠ Missing'}
                  </span>
                </div>
              </div>

              {item.details && (
                <p className="text-xs text-slate-400 border-t border-slate-800/80 pt-3 font-mono">
                  <strong className="text-slate-300">Execution Log:</strong> {item.details}
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
