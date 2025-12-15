'use client';

import * as React from 'react';

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Grid,
  Input,
} from '@/design-system/components';
import { PageSkeleton } from '@/components/Skeleton';
import { useNotifications } from '@/lib/notifications/NotificationsProvider';
import { useAppState } from '@/lib/state/AppStateProvider';

function formatDuration(ms: number): string {
  const totalSeconds = Math.ceil(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

export default function ProductivityPage() {
  const { hydrated, state, startFocusTimer, cancelFocusTimer, completeFocusTimer, addReminder } =
    useAppState();
  const { permission, requestPermission, showNotification } = useNotifications();

  const [now, setNow] = React.useState(() => Date.now());
  const [focusMinutes, setFocusMinutes] = React.useState('25');

  const [reminderTitle, setReminderTitle] = React.useState('Take a break');
  const [reminderMinutes, setReminderMinutes] = React.useState('10');

  React.useEffect(() => {
    if (state.focusTimer.status !== 'running') {
      return;
    }

    const interval = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(interval);
  }, [state.focusTimer.status]);

  const remainingMs = React.useMemo(() => {
    if (state.focusTimer.status !== 'running' || !state.focusTimer.endsAt) {
      return 0;
    }

    return Math.max(0, new Date(state.focusTimer.endsAt).getTime() - now);
  }, [now, state.focusTimer.endsAt, state.focusTimer.status]);

  React.useEffect(() => {
    if (state.focusTimer.status !== 'running') {
      return;
    }

    if (remainingMs > 0) {
      return;
    }

    completeFocusTimer();
    void showNotification({ title: 'Focus session complete', body: 'Nice work. Time for a break!', url: '/productivity' });
  }, [completeFocusTimer, remainingMs, showNotification, state.focusTimer.status]);

  if (!hydrated) {
    return <PageSkeleton />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Productivity</h1>
        <p className="text-sm text-foreground-tertiary">
          Focus timer + browser notifications. Reminders fire while the app is open.
        </p>
      </div>

      <Grid cols={1} gap={4} className="md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Focus timer</CardTitle>
            <CardDescription>Start a timed focus session and get a notification at the end.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-4xl font-semibold tabular-nums">
              {state.focusTimer.status === 'running' ? formatDuration(remainingMs) : '—'}
            </div>

            <div className="flex flex-col gap-3 md:flex-row md:items-end">
              <div className="flex-1">
                <Input
                  label="Minutes"
                  inputMode="numeric"
                  value={focusMinutes}
                  onChange={(e) => setFocusMinutes(e.target.value)}
                  disabled={state.focusTimer.status === 'running'}
                />
              </div>

              {state.focusTimer.status === 'running' ? (
                <Button variant="outline" onClick={cancelFocusTimer}>
                  Stop
                </Button>
              ) : (
                <Button
                  onClick={() => {
                    const minutes = Number.parseInt(focusMinutes, 10);
                    startFocusTimer(Number.isFinite(minutes) ? minutes : 25);
                  }}
                >
                  Start
                </Button>
              )}
            </div>

            <div className="text-xs text-foreground-tertiary">Notifications: {permission}</div>
            {permission !== 'granted' ? (
              <Button variant="ghost" size="sm" onClick={() => void requestPermission()}>
                Enable notifications
              </Button>
            ) : null}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Reminders</CardTitle>
            <CardDescription>Schedule a one-off browser reminder.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-col gap-3 md:flex-row md:items-end">
              <div className="flex-1">
                <Input
                  label="Title"
                  value={reminderTitle}
                  onChange={(e) => setReminderTitle(e.target.value)}
                />
              </div>
              <div className="w-full md:w-36">
                <Input
                  label="In (min)"
                  inputMode="numeric"
                  value={reminderMinutes}
                  onChange={(e) => setReminderMinutes(e.target.value)}
                />
              </div>
              <Button
                onClick={() => {
                  const minutes = Number.parseInt(reminderMinutes, 10);
                  const fireAt = new Date(Date.now() + (Number.isFinite(minutes) ? minutes : 10) * 60 * 1000).toISOString();
                  addReminder({
                    title: reminderTitle.trim() || 'Reminder',
                    body: 'Scheduled reminder',
                    fireAt,
                  });
                }}
              >
                Schedule
              </Button>
            </div>

            <div className="space-y-2">
              {state.reminders
                .slice()
                .sort((a, b) => new Date(a.fireAt).getTime() - new Date(b.fireAt).getTime())
                .slice(0, 6)
                .map((r) => (
                  <div key={r.id} className="flex items-center justify-between text-sm">
                    <div className="min-w-0">
                      <div className="truncate">{r.title}</div>
                      <div className="text-xs text-foreground-tertiary">
                        {new Date(r.fireAt).toLocaleString()} {r.notifiedAt ? '• sent' : ''}
                      </div>
                    </div>
                  </div>
                ))}
            </div>
          </CardContent>
        </Card>
      </Grid>
    </div>
  );
}
