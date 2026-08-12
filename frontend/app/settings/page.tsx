'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { PlatformConnection, PlatformEnum, SettingsData } from '@/types';
import PriorityReorder from '@/components/PriorityReorder';

const TIMEZONES = [
  'Asia/Kolkata',
  'UTC',
  'America/New_York',
  'America/Los_Angeles',
  'Europe/London',
  'Asia/Tokyo',
  'Australia/Sydney',
];

export default function SettingsPage() {
  const [settings, setSettings] = useState<SettingsData | null>(null);
  const [connections, setConnections] = useState<PlatformConnection[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Connection handles inputs
  const [handles, setHandles] = useState<Record<PlatformEnum, string>>({
    LEETCODE: '',
    CODECHEF: '',
    GEEKSFORGEEKS: '',
  });

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        const [settData, connData] = await Promise.all([
          api.getSettings(),
          api.getPlatformConnections(),
        ]);
        setSettings(settData);
        setConnections(connData);

        const hMap: Record<PlatformEnum, string> = {
          LEETCODE: '',
          CODECHEF: '',
          GEEKSFORGEEKS: '',
        };
        connData.forEach((c) => {
          hMap[c.platform] = c.platformUsername;
        });
        setHandles(hMap);
      } catch (err: any) {
        setError(err.message || 'Failed to load settings');
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  const handleSaveSettings = async () => {
    if (!settings) return;
    setSaving(true);
    setSaveSuccess(false);
    setError(null);

    try {
      await api.updateSettings(settings);
      
      // Save platform connections
      for (const p of ['LEETCODE', 'CODECHEF', 'GEEKSFORGEEKS'] as PlatformEnum[]) {
        if (handles[p]) {
          await api.connectPlatform(p, handles[p]);
        }
      }

      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to save settings');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="p-12 text-center text-slate-400">Loading Relay settings configuration...</div>;
  }

  if (!settings) return null;

  return (
    <div className="max-w-4xl space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">Relay Configuration & Settings</h1>
        <p className="text-slate-400 text-sm mt-1">
          Configure platform priority, emergency submission schedule, timezone, and platform handles.
        </p>
      </div>

      {saveSuccess && (
        <div className="p-4 rounded-xl bg-emerald-950/40 border border-emerald-500/30 text-emerald-300 text-sm font-semibold">
          ✓ Relay configuration saved successfully!
        </div>
      )}

      {error && (
        <div className="p-4 rounded-xl bg-rose-950/40 border border-rose-500/30 text-rose-300 text-sm">
          {error}
        </div>
      )}

      {/* Platform Priority Section */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6 backdrop-blur-md space-y-4">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <span>⚙️</span> Platform Selection Priority
        </h2>
        <PriorityReorder
          priority={settings.priorityOrder}
          onChange={(newPriority) => setSettings({ ...settings, priorityOrder: newPriority })}
        />
      </div>

      {/* Emergency Schedule & Timezone */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6 backdrop-blur-md space-y-6">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <span>⏰</span> Emergency Scheduler & Timezone
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Emergency Submission Cutoff Time (24h format)
            </label>
            <input
              type="time"
              value={settings.emergencyTime}
              onChange={(e) => setSettings({ ...settings, emergencyTime: e.target.value })}
              className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-3 text-white font-mono focus:border-orange-500 focus:outline-none"
            />
            <p className="text-xs text-slate-400 mt-1">Default: 23:30 (11:30 PM)</p>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              User Timezone
            </label>
            <select
              value={settings.timezone}
              onChange={(e) => setSettings({ ...settings, timezone: e.target.value })}
              className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-3 text-white font-mono focus:border-orange-500 focus:outline-none"
            >
              {TIMEZONES.map((tz) => (
                <option key={tz} value={tz}>
                  {tz}
                </option>
              ))}
            </select>
            <p className="text-xs text-slate-400 mt-1">Default: Asia/Kolkata</p>
          </div>
        </div>
      </div>

      {/* Platform Connections Handles */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6 backdrop-blur-md space-y-6">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <span>🔗</span> Platform User Handles
        </h2>

        <div className="space-y-4">
          {(['LEETCODE', 'CODECHEF', 'GEEKSFORGEEKS'] as PlatformEnum[]).map((plat) => (
            <div key={plat} className="grid grid-cols-1 sm:grid-cols-3 items-center gap-4 p-4 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="font-semibold text-sm text-slate-200">
                {plat === 'LEETCODE' ? 'LeetCode Handle' : plat === 'CODECHEF' ? 'CodeChef Handle' : 'GeeksforGeeks Handle'}
              </span>
              <input
                type="text"
                value={handles[plat]}
                onChange={(e) => setHandles({ ...handles, [plat]: e.target.value })}
                placeholder={`Enter your ${plat} username`}
                className="sm:col-span-2 rounded-lg bg-slate-900 border border-slate-800 px-3 py-2 text-sm text-white font-mono focus:border-orange-500 focus:outline-none"
              />
            </div>
          ))}
        </div>
      </div>

      {/* Toggles */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6 backdrop-blur-md space-y-4">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <span>🔔</span> Automation & Notifications
        </h2>

        <div className="flex items-center justify-between py-2">
          <div>
            <div className="font-semibold text-sm text-slate-200">Enable Automatic Emergency Submissions</div>
            <div className="text-xs text-slate-400">Allow Relay to attempt emergency submission at scheduled cutoff if streak is missing</div>
          </div>
          <input
            type="checkbox"
            checked={settings.autoSubmitEnabled}
            onChange={(e) => setSettings({ ...settings, autoSubmitEnabled: e.target.checked })}
            className="h-5 w-5 rounded border-slate-700 text-orange-500 focus:ring-orange-500 cursor-pointer"
          />
        </div>

        <div className="flex items-center justify-between py-2 border-t border-slate-800">
          <div>
            <div className="font-semibold text-sm text-slate-200">Streak Alerts & Notifications</div>
            <div className="text-xs text-slate-400">Receive alerts when no submission is detected on active profiles</div>
          </div>
          <input
            type="checkbox"
            checked={settings.notificationsEnabled}
            onChange={(e) => setSettings({ ...settings, notificationsEnabled: e.target.checked })}
            className="h-5 w-5 rounded border-slate-700 text-orange-500 focus:ring-orange-500 cursor-pointer"
          />
        </div>
      </div>

      {/* Save Button */}
      <div className="flex justify-end pt-4">
        <button
          onClick={handleSaveSettings}
          disabled={saving}
          className="px-8 py-3 rounded-xl bg-orange-500 hover:bg-orange-600 font-bold text-sm text-white shadow-lg shadow-orange-500/20 transition-colors disabled:opacity-50 cursor-pointer"
        >
          {saving ? 'Saving Settings...' : 'Save Relay Configuration'}
        </button>
      </div>
    </div>
  );
}
