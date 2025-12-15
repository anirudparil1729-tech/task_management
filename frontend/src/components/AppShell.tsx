'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import * as React from 'react';
import {
  CalendarDays,
  LayoutDashboard,
  Settings as SettingsIcon,
  Tags,
  Timer,
} from 'lucide-react';

import { cn } from '@/design-system/utils';
import { Button } from '@/design-system/components';
import { useSync } from '@/lib/sync/SyncProvider';

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/categories', label: 'Categories', icon: Tags },
  { href: '/planner', label: 'Planner', icon: CalendarDays },
  { href: '/productivity', label: 'Productivity', icon: Timer },
  { href: '/settings', label: 'Settings', icon: SettingsIcon },
] as const;

function NavLink({
  href,
  label,
  icon: Icon,
  variant,
}: {
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  variant: 'sidebar' | 'bottom';
}) {
  const pathname = usePathname();
  const active = pathname === href;

  if (variant === 'bottom') {
    return (
      <Link
        href={href}
        className={cn(
          'flex flex-1 flex-col items-center justify-center gap-1 py-2 text-xs',
          active ? 'text-primary-600' : 'text-foreground-tertiary'
        )}
      >
        <Icon className="h-5 w-5" />
        <span>{label}</span>
      </Link>
    );
  }

  return (
    <Link
      href={href}
      className={cn(
        'flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
        active
          ? 'bg-background-tertiary text-foreground-primary'
          : 'text-foreground-secondary hover:bg-background-tertiary hover:text-foreground-primary'
      )}
    >
      <Icon className="h-5 w-5" />
      <span>{label}</span>
    </Link>
  );
}

export function AppShell({ children }: { children: React.ReactNode }) {
  const { status: syncStatus, syncNow } = useSync();

  return (
    <div className="min-h-screen bg-background-secondary">
      <div className="flex min-h-screen">
        <aside className="hidden w-64 flex-col border-r border-border-primary bg-background-primary p-4 md:flex">
          <div className="mb-4 flex items-center justify-between">
            <div className="text-base font-semibold">App Shell</div>
          </div>

          <nav className="flex flex-col gap-1">
            {navItems.map((item) => (
              <NavLink key={item.href} variant="sidebar" {...item} />
            ))}
          </nav>

          <div className="mt-auto pt-4">
            <div className="text-xs text-foreground-tertiary">Sync: {syncStatus}</div>
          </div>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="sticky top-0 z-10 border-b border-border-primary bg-background-primary/80 backdrop-blur">
            <div className="flex items-center justify-between px-4 py-3">
              <div className="md:hidden text-sm font-semibold">App Shell</div>
              <div className="flex items-center gap-3">
                <div className="hidden text-xs text-foreground-tertiary md:block">Sync: {syncStatus}</div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => void syncNow()}
                  disabled={syncStatus === 'syncing'}
                >
                  Sync now
                </Button>
              </div>
            </div>
          </header>

          <main className="flex-1 p-4 pb-20 md:p-6 md:pb-6">{children}</main>
        </div>
      </div>

      <nav className="fixed bottom-0 left-0 right-0 z-20 border-t border-border-primary bg-background-primary md:hidden">
        <div className="flex items-stretch">
          {navItems.map((item) => (
            <NavLink key={item.href} variant="bottom" {...item} />
          ))}
        </div>
      </nav>
    </div>
  );
}
