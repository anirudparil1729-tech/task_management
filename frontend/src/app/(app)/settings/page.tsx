'use client';

import * as React from 'react';

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/design-system/components';
import { PageSkeleton } from '@/components/Skeleton';
import { useAuth } from '@/lib/auth/AuthProvider';
import { useNotifications } from '@/lib/notifications/NotificationsProvider';
import { useAppState } from '@/lib/state/AppStateProvider';
import { useSync } from '@/lib/sync/SyncProvider';
import { type Theme, useTheme } from '@/lib/theme/ThemeProvider';

export default function SettingsPage() {
  const { hydrated, state } = useAppState();
  const { logout } = useAuth();
  const { status: syncStatus, lastError, lastSyncedAt, syncNow } = useSync();
  const { permission, requestPermission } = useNotifications();
  const { theme, setTheme } = useTheme();

  const [loggingOut, setLoggingOut] = React.useState(false);

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const themes: Theme[] = ['system', 'light', 'dark'];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Settings</h1>
        <p className="text-sm text-foreground-tertiary">Theme, notifications, and sync controls.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Theme</CardTitle>
          <CardDescription>Choose system, light, or dark.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-2">
            {themes.map((t) => (
              <Button
                key={t}
                variant={t === theme ? 'primary' : 'outline'}
                size="sm"
                onClick={() => setTheme(t)}
              >
                {t}
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Notifications</CardTitle>
          <CardDescription>Used for reminders and focus timer completion.</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center justify-between gap-4">
          <div className="text-sm text-foreground-tertiary">Permission: {permission}</div>
          <Button variant="outline" size="sm" onClick={() => void requestPermission()}>
            Request permission
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Sync</CardTitle>
          <CardDescription>Manual sync and local pending changes.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="text-sm text-foreground-tertiary">Status: {syncStatus}</div>
          <div className="text-sm text-foreground-tertiary">
            Pending changes: {state.outbox.length}
          </div>
          {lastSyncedAt ? (
            <div className="text-sm text-foreground-tertiary">
              Last sync: {new Date(lastSyncedAt).toLocaleString()}
            </div>
          ) : null}
          {lastError ? <div className="text-sm text-danger-600">{lastError}</div> : null}

          <Button onClick={() => void syncNow()} disabled={syncStatus === 'syncing'}>
            Sync now
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Account</CardTitle>
          <CardDescription>Static password gate (local token persisted in browser storage).</CardDescription>
        </CardHeader>
        <CardContent>
          <Button
            variant="danger"
            onClick={() => {
              setLoggingOut(true);
              void logout();
            }}
            disabled={loggingOut}
          >
            Log out
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
