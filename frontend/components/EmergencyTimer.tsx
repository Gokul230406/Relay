'use client';

import { useState } from 'react';
import { api } from '@/lib/api';
import { SubmissionExecutionResponse } from '@/types';

interface Props {
  emergencyTime: string;
  timezone: string;
  botStatus: string;
  dailyLimitReached: boolean;
  onUpdate: () => void;
}

export default function EmergencyTimer({
  emergencyTime,
  timezone,
  botStatus,
  dailyLimitReached,
  onUpdate,
}: Props) {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<SubmissionExecutionResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleTriggerEmergencySubmit = async () => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const res = await api.triggerEmergencySubmit();
      setResult(res);
      onUpdate();
    } catch (err: any) {
      setError(err.message || 'Emergency submission failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rounded-2xl border border-slate-800 bg-slate-900/90 p-6 shadow-xl backdrop-blur-md">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="flex h-3 w-3 rounded-full bg-amber-400 animate-ping"></span>
            <h2 className="text-lg font-bold text-white">Relay Emergency Submission Engine</h2>
            <span className="rounded-full bg-orange-500/10 px-2.5 py-0.5 text-xs font-semibold text-orange-400 border border-orange-500/20">
              Cutoff: {emergencyTime} ({timezone})
            </span>
          </div>

          <p className="text-sm text-slate-300">
            {botStatus}
          </p>

          <div className="flex items-center gap-2 text-xs text-slate-400 pt-1">
            <span className="font-semibold text-slate-300">Relay Execution Mode:</span>
            <span>Single-trigger multi-platform check & submission active.</span>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
          <button
            onClick={handleTriggerEmergencySubmit}
            disabled={loading}
            className="flex items-center justify-center gap-2 px-6 py-3 rounded-xl font-bold text-sm shadow-lg transition-all bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 text-white shadow-orange-500/25 hover:shadow-orange-500/40 active:scale-95 cursor-pointer"
          >
            {loading ? (
              <>
                <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>Executing Relay Multi-Platform Submit...</span>
              </>
            ) : (
              <>
                <span>⚡ Trigger Emergency Submission Now</span>
              </>
            )}
          </button>
        </div>
      </div>

      {result && (
        <div className={`mt-4 p-4 rounded-xl border text-sm ${
          result.executed
            ? 'bg-emerald-950/40 border-emerald-500/30 text-emerald-300'
            : result.status === 'NO_ACTION'
            ? 'bg-blue-950/40 border-blue-500/30 text-blue-300'
            : 'bg-amber-950/40 border-amber-500/30 text-amber-300'
        }`}>
          <div className="font-semibold text-base mb-1">{result.message}</div>
          {result.selectedPlatform && (
            <div className="text-xs text-slate-300 font-mono mt-1">
              Trigger Execution ID: {result.submissionId || 'N/A'}
            </div>
          )}
        </div>
      )}

      {error && (
        <div className="mt-4 p-4 rounded-xl border bg-rose-950/40 border-rose-500/30 text-rose-300 text-sm">
          <span className="font-bold">Error:</span> {error}
        </div>
      )}
    </div>
  );
}
