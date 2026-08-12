'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { api, setAuthToken } from '@/lib/api';

export default function LoginPage() {
  const router = useRouter();
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      let res;
      if (isRegister) {
        res = await api.register(email, password, fullName);
      } else {
        res = await api.login(email, password);
      }
      setAuthToken(res.token);
      router.push('/');
    } catch (err: any) {
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = async () => {
    setLoading(true);
    setError(null);
    try {
      let res;
      try {
        res = await api.login('demo@streaksaver.dev', 'password123');
      } catch {
        res = await api.register('demo@streaksaver.dev', 'password123', 'Demo Developer');
      }
      setAuthToken(res.token);
      router.push('/');
    } catch (err: any) {
      setError('Demo login failed: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[80vh] items-center justify-center px-4">
      <div className="w-full max-w-md space-y-8 rounded-3xl border border-slate-800 bg-slate-900/90 p-8 shadow-2xl backdrop-blur-xl">
        <div className="text-center space-y-2">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-amber-500 via-orange-500 to-rose-600 shadow-xl shadow-orange-500/25">
            <span className="text-2xl">🔥</span>
          </div>
          <h2 className="text-2xl font-extrabold text-white tracking-tight">
            {isRegister ? 'Create StreakSaver Account' : 'Welcome back to StreakSaver'}
          </h2>
          <p className="text-xs text-slate-400">
            Personal coding streak protection for LeetCode, CodeChef & GeeksforGeeks
          </p>
        </div>

        {error && (
          <div className="rounded-xl border border-rose-500/30 bg-rose-950/40 p-3 text-center text-xs text-rose-300">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {isRegister && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Full Name</label>
              <input
                type="text"
                required
                placeholder="John Doe"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-3 text-white text-sm focus:border-orange-500 focus:outline-none"
              />
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Email Address</label>
            <input
              type="email"
              required
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-3 text-white text-sm focus:border-orange-500 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Password</label>
            <input
              type="password"
              required
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-xl bg-slate-950 border border-slate-800 px-4 py-3 text-white text-sm focus:border-orange-500 focus:outline-none"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-orange-500 py-3 text-sm font-bold text-white shadow-lg shadow-orange-500/25 hover:bg-orange-600 transition-colors disabled:opacity-50"
          >
            {loading ? 'Processing...' : isRegister ? 'Register Account' : 'Sign In'}
          </button>
        </form>

        <div className="relative flex items-center justify-center my-6">
          <div className="border-t border-slate-800 w-full"></div>
          <span className="bg-slate-900 px-3 text-[11px] font-semibold text-slate-500 uppercase tracking-wider absolute">
            Or Quick Access
          </span>
        </div>

        <button
          type="button"
          onClick={handleDemoLogin}
          disabled={loading}
          className="w-full rounded-xl bg-slate-800 border border-slate-700 py-3 text-xs font-bold text-slate-200 hover:bg-slate-700 transition-colors"
        >
          ⚡ Demo One-Click Sign In
        </button>

        <div className="text-center text-xs text-slate-400 pt-2">
          {isRegister ? 'Already have an account?' : "Don't have an account?"}{' '}
          <button
            type="button"
            onClick={() => setIsRegister(!isRegister)}
            className="font-bold text-orange-400 hover:underline"
          >
            {isRegister ? 'Sign In' : 'Register Now'}
          </button>
        </div>
      </div>
    </div>
  );
}
