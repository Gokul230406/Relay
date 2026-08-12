'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { removeAuthToken } from '@/lib/api';

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();

  const handleLogout = () => {
    removeAuthToken();
    router.push('/login');
  };

  const navItems = [
    { label: 'Dashboard', href: '/' },
    { label: 'History & Calendar', href: '/history' },
    { label: 'Problem Pool', href: '/problem-pool' },
    { label: 'Settings', href: '/settings' },
  ];

  if (pathname === '/login') return null;

  return (
    <header className="sticky top-0 z-50 border-b border-slate-800 bg-slate-950/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
        <Link href="/" className="flex items-center gap-3 group">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 via-orange-500 to-rose-600 shadow-lg shadow-orange-500/20 group-hover:scale-105 transition-transform">
            <span className="text-xl font-bold text-white">⚡</span>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-2xl font-black tracking-tight text-white font-sans">
                Relay<span className="text-orange-500">.</span>
              </span>
              <span className="rounded-full bg-orange-500/10 px-2.5 py-0.5 text-[10px] font-semibold text-orange-400 border border-orange-500/20">
                Streak Protection Engine
              </span>
            </div>
            <p className="text-xs text-slate-400">Automated Personal Coding Streak Relay System</p>
          </div>
        </Link>

        <nav className="hidden md:flex items-center gap-1">
          {navItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-slate-800 text-orange-400 border border-slate-700'
                    : 'text-slate-300 hover:text-white hover:bg-slate-900'
                }`}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="flex items-center gap-3">
          <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-900 border border-slate-800 text-xs text-slate-300">
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse"></span>
            <span>Relay Engine: Active</span>
          </div>
          <button
            onClick={handleLogout}
            className="px-3 py-1.5 text-xs font-medium text-slate-400 hover:text-slate-200 hover:bg-slate-800/80 rounded-lg transition-colors border border-slate-800"
          >
            Sign Out
          </button>
        </div>
      </div>
    </header>
  );
}
