'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { PlatformEnum, ProblemPoolItem } from '@/types';

export default function ProblemPoolPage() {
  const [items, setItems] = useState<ProblemPoolItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [activePlatform, setActivePlatform] = useState<PlatformEnum>('LEETCODE');
  const [showAddModal, setShowAddModal] = useState(false);

  // Form state
  const [problemId, setProblemId] = useState('');
  const [problemTitle, setProblemTitle] = useState('');
  const [language, setLanguage] = useState('java');
  const [solutionCode, setSolutionCode] = useState('');
  const [targetUrl, setTargetUrl] = useState('');

  const loadPool = async () => {
    try {
      setLoading(true);
      const data = await api.getProblemPool();
      setItems(data);
    } catch (err) {
      console.error('Failed to load problem pool', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPool();
  }, []);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!problemTitle || !solutionCode) return;

    try {
      await api.addProblemToPool(activePlatform, {
        problemId: problemId || `P-${Date.now()}`,
        problemTitle,
        language,
        solutionCode,
        targetUrl,
      });

      setShowAddModal(false);
      setProblemId('');
      setProblemTitle('');
      setSolutionCode('');
      setTargetUrl('');
      loadPool();
    } catch (err) {
      console.error('Failed to add problem', err);
    }
  };

  const handleDelete = async (id: string, platform: PlatformEnum) => {
    try {
      await api.deleteProblemFromPool(platform, id);
      loadPool();
    } catch (err) {
      console.error('Failed to delete problem', err);
    }
  };

  const filteredItems = items.filter((i) => i.platform === activePlatform);

  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Relay Pre-Approved Java Problem Pool</h1>
          <p className="text-slate-400 text-sm mt-1">
            Configure user-approved Java problems and solutions. Relay will ONLY submit solutions explicitly provided here.
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="px-5 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 font-bold text-sm text-white shadow-lg shadow-orange-500/20 transition-colors self-start sm:self-auto cursor-pointer"
        >
          + Add Approved Solution
        </button>
      </div>

      {/* Platform Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-2">
        {(['LEETCODE', 'CODECHEF', 'GEEKSFORGEEKS'] as PlatformEnum[]).map((p) => {
          const count = items.filter((i) => i.platform === p).length;
          return (
            <button
              key={p}
              onClick={() => setActivePlatform(p)}
              className={`px-5 py-2.5 rounded-xl text-sm font-bold transition-colors ${
                activePlatform === p
                  ? 'bg-slate-800 text-orange-400 border border-slate-700'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
              }`}
            >
              {p === 'LEETCODE' ? '💻 LeetCode Pool' : p === 'CODECHEF' ? '👨‍🍳 CodeChef Pool' : '🚀 GeeksforGeeks Pool'} ({count})
            </button>
          );
        })}
      </div>

      {loading ? (
        <div className="p-12 text-center text-slate-400">Loading problem pool...</div>
      ) : filteredItems.length === 0 ? (
        <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-12 text-center text-slate-400 space-y-3">
          <span className="text-4xl">📚</span>
          <p className="font-semibold text-slate-200">No pre-approved solutions configured for {activePlatform}</p>
          <p className="text-xs text-slate-400 max-w-md mx-auto">
            Relay never generates arbitrary code. Click "Add Approved Solution" above to add your first solution snippet.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredItems.map((item) => (
            <div
              key={item.id}
              className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 backdrop-blur-md space-y-4 hover:border-slate-700 transition-colors"
            >
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-xs font-mono text-slate-400 uppercase">ID: {item.problemId}</span>
                  <h3 className="text-lg font-bold text-white mt-0.5">{item.problemTitle}</h3>
                </div>

                <button
                  onClick={() => item.id && handleDelete(item.id, item.platform)}
                  className="p-1.5 rounded-lg bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 border border-rose-500/20 text-xs font-semibold cursor-pointer"
                >
                  Delete
                </button>
              </div>

              <div className="flex items-center gap-2">
                <span className="rounded-md bg-slate-800 px-2.5 py-1 text-xs font-mono text-orange-300 border border-slate-700 uppercase font-bold">
                  {item.language || 'java'}
                </span>
                <span className="rounded-md bg-emerald-500/10 px-2.5 py-1 text-xs font-semibold text-emerald-400 border border-emerald-500/20">
                  Status: Approved
                </span>
              </div>

              <div className="rounded-xl bg-slate-950 p-4 border border-slate-800 overflow-x-auto max-h-48">
                <pre className="text-xs font-mono text-slate-300 leading-relaxed">{item.solutionCode}</pre>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="w-full max-w-lg rounded-2xl border border-slate-800 bg-slate-900 p-6 shadow-2xl space-y-5">
            <h3 className="text-xl font-bold text-white">Add Approved Problem Solution ({activePlatform})</h3>

            <form onSubmit={handleAdd} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Problem Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Two Sum"
                  value={problemTitle}
                  onChange={(e) => setProblemTitle(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-2.5 text-white text-sm focus:border-orange-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Problem ID / Slug</label>
                  <input
                    type="text"
                    placeholder="e.g. two-sum"
                    value={problemId}
                    onChange={(e) => setProblemId(e.target.value)}
                    className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-2.5 text-white text-sm focus:border-orange-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Language</label>
                  <select
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                    className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-2.5 text-white text-sm focus:border-orange-500 focus:outline-none"
                  >
                    <option value="java">Java</option>
                    <option value="python3">Python 3</option>
                    <option value="cpp">C++</option>
                    <option value="javascript">JavaScript</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Solution Code</label>
                <textarea
                  required
                  rows={5}
                  placeholder="Paste your tested, working Java solution code here..."
                  value={solutionCode}
                  onChange={(e) => setSolutionCode(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-800 p-4 text-xs font-mono text-white focus:border-orange-500 focus:outline-none"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 hover:bg-slate-700 text-xs font-semibold cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold shadow-lg shadow-orange-500/20 cursor-pointer"
                >
                  Save to Pool
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
