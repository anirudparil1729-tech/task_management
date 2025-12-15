'use client';

import * as React from 'react';
import { Download, RefreshCw, Bell, Palette, LogOut, Database } from 'lucide-react';

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Select,
  type SelectOption,
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

  const handleExportData = () => {
    const exportData = {
      version: '1.0',
      exportedAt: new Date().toISOString(),
      categories: Object.values(state.categories),
      tasks: Object.values(state.tasks),
      timeBlocks: Object.values(state.timeBlocks),
      reminders: state.reminders,
      focusTimer: state.focusTimer,
    };

    const blob = new Blob([JSON.stringify(exportData, null, 2)], {
      type: 'application/json',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `productivity-data-${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const notificationCadenceOptions: SelectOption[] = [
    { value: 'all', label: 'All notifications' },
    { value: 'important', label: 'Important only' },
    { value: 'none', label: 'None' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Settings</h1>
        <p className="text-sm text-foreground-tertiary">
          Customize your experience and manage your data
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Palette className="h-5 w-5" />
            Appearance
          </CardTitle>
          <CardDescription>Customize the look and feel of the app</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <label className="mb-2 block text-sm font-medium text-foreground-primary">
              Theme
            </label>
            <div className="flex flex-wrap gap-2">
              {themes.map((t) => (
                <Button
                  key={t}
                  variant={t === theme ? 'primary' : 'outline'}
                  size="sm"
                  onClick={() => setTheme(t)}
                  className="capitalize"
                >
                  {t}
                </Button>
              ))}
            </div>
            <p className="mt-2 text-xs text-foreground-tertiary">
              Choose between light, dark, or system default theme
            </p>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="h-5 w-5" />
            Notifications
          </CardTitle>
          <CardDescription>
            Manage browser notifications for reminders and focus timer
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between rounded-lg border border-border-primary bg-background-secondary p-4">
            <div>
              <p className="font-medium">Browser Notifications</p>
              <p className="text-sm text-foreground-tertiary">
                Current permission: <span className="font-medium">{permission}</span>
              </p>
            </div>
            <Button
              variant={permission === 'granted' ? 'success' : 'primary'}
              size="sm"
              onClick={() => void requestPermission()}
              disabled={permission === 'granted'}
            >
              {permission === 'granted' ? 'Enabled' : 'Enable'}
            </Button>
          </div>

          <div>
            <Select
              label="Notification Cadence"
              value="all"
              onChange={() => {}}
              options={notificationCadenceOptions}
            />
            <p className="mt-2 text-xs text-foreground-tertiary">
              Control how often you receive notifications
            </p>
          </div>

          <div className="rounded-lg border border-border-primary bg-background-secondary p-4">
            <h4 className="mb-2 font-medium">Notification Types</h4>
            <ul className="space-y-1 text-sm text-foreground-tertiary">
              <li>• Focus timer completion</li>
              <li>• Scheduled reminders</li>
              <li>• Overdue task alerts</li>
              <li>• Daily summary</li>
            </ul>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <RefreshCw className="h-5 w-5" />
            Sync & Data
          </CardTitle>
          <CardDescription>
            Manage offline data synchronization and manual sync
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between rounded-lg border border-border-primary bg-background-secondary p-4">
            <div>
              <p className="font-medium">Sync Status</p>
              <p className="text-sm text-foreground-tertiary">
                Status: <span className="font-medium capitalize">{syncStatus}</span>
              </p>
            </div>
            <Button
              onClick={() => void syncNow()}
              disabled={syncStatus === 'syncing'}
              size="sm"
            >
              <RefreshCw className={`mr-2 h-4 w-4 ${syncStatus === 'syncing' ? 'animate-spin' : ''}`} />
              {syncStatus === 'syncing' ? 'Syncing...' : 'Sync Now'}
            </Button>
          </div>

          <div className="space-y-2 rounded-lg border border-border-primary bg-background-secondary p-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-foreground-tertiary">Pending changes:</span>
              <span className="font-medium">{state.outbox.length}</span>
            </div>
            {lastSyncedAt && (
              <div className="flex items-center justify-between text-sm">
                <span className="text-foreground-tertiary">Last sync:</span>
                <span className="font-medium">
                  {new Date(lastSyncedAt).toLocaleString()}
                </span>
              </div>
            )}
            {lastError && (
              <div className="text-sm text-danger-600">
                <span className="font-medium">Error:</span> {lastError}
              </div>
            )}
          </div>

          <div className="rounded-lg border border-border-primary bg-background-secondary p-4">
            <h4 className="mb-2 font-medium">Data Summary</h4>
            <div className="space-y-1 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-foreground-tertiary">Tasks:</span>
                <span className="font-medium">{Object.keys(state.tasks).length}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-foreground-tertiary">Categories:</span>
                <span className="font-medium">{Object.keys(state.categories).length}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-foreground-tertiary">Time Blocks:</span>
                <span className="font-medium">{Object.keys(state.timeBlocks).length}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-foreground-tertiary">Reminders:</span>
                <span className="font-medium">{state.reminders.length}</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Database className="h-5 w-5" />
            Data Export
          </CardTitle>
          <CardDescription>
            Export all your data in JSON format for backup or migration
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="rounded-lg border border-border-primary bg-background-secondary p-4">
            <p className="mb-3 text-sm text-foreground-tertiary">
              Download a complete backup of your tasks, categories, time blocks, and settings.
              This file can be used to restore your data or migrate to another device.
            </p>
            <Button onClick={handleExportData} className="flex items-center gap-2">
              <Download className="h-4 w-4" />
              Export All Data
            </Button>
          </div>

          <div className="rounded-lg border border-warning-200 bg-warning-50 p-4">
            <h4 className="mb-2 font-medium text-warning-900">Export Information</h4>
            <ul className="space-y-1 text-sm text-warning-800">
              <li>• Exported data includes all categories, tasks, and time blocks</li>
              <li>• Reminders and focus timer state are included</li>
              <li>• File is in JSON format for easy processing</li>
              <li>• Contains local IDs and server sync information</li>
            </ul>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <LogOut className="h-5 w-5" />
            Account
          </CardTitle>
          <CardDescription>
            Manage your session and authentication
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="rounded-lg border border-border-primary bg-background-secondary p-4">
            <p className="mb-3 text-sm text-foreground-tertiary">
              This app uses a static password gate. Your authentication token is stored locally
              in browser storage. Logging out will clear your session but keep your data intact.
            </p>
            <Button
              variant="danger"
              onClick={() => {
                setLoggingOut(true);
                void logout();
              }}
              disabled={loggingOut}
              className="flex items-center gap-2"
            >
              <LogOut className="h-4 w-4" />
              {loggingOut ? 'Logging out...' : 'Log Out'}
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="rounded-lg border border-border-primary bg-background-secondary p-4">
        <div className="text-center text-sm text-foreground-tertiary">
          <p className="mb-1">Productivity App v1.0.0</p>
          <p>Offline-first task management with sync capabilities</p>
        </div>
      </div>
    </div>
  );
}
