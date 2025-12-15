'use client';

import * as React from 'react';
import { Target, TrendingUp, Clock, Award, Zap } from 'lucide-react';

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Grid,
  Input,
  Progress,
} from '@/design-system/components';
import { PageSkeleton } from '@/components/Skeleton';
import { useNotifications } from '@/lib/notifications/NotificationsProvider';
import { useAppState } from '@/lib/state/AppStateProvider';
import { getCategoryIcon } from '@/components/IconPicker';

function formatDuration(ms: number): string {
  const totalSeconds = Math.ceil(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

function calculateProductivityScore(
  completedTasks: number,
  totalTasks: number,
  focusSessions: number
): number {
  if (totalTasks === 0) return 0;

  const completionRate = (completedTasks / totalTasks) * 70;
  const focusBonus = Math.min(focusSessions * 5, 30);

  return Math.min(Math.round(completionRate + focusBonus), 100);
}

export default function ProductivityPage() {
  const {
    hydrated,
    state,
    startFocusTimer,
    cancelFocusTimer,
    completeFocusTimer,
    addReminder,
  } = useAppState();
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
    void showNotification({
      title: 'Focus session complete',
      body: 'Nice work. Time for a break!',
      url: '/productivity',
    });
  }, [completeFocusTimer, remainingMs, showNotification, state.focusTimer.status]);

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const tasks = Object.values(state.tasks);
  const categories = Object.values(state.categories);

  const completedToday = tasks.filter((t) => {
    if (!t.isCompleted || !t.completedAt) return false;
    const completedDate = new Date(t.completedAt);
    const today = new Date();
    return (
      completedDate.getDate() === today.getDate() &&
      completedDate.getMonth() === today.getMonth() &&
      completedDate.getFullYear() === today.getFullYear()
    );
  });

  const focusSessionsToday = 1;
  const dailyScore = calculateProductivityScore(
    completedToday.length,
    Math.max(completedToday.length, 5),
    focusSessionsToday
  );

  const categoryProgress = categories.map((category) => {
    const categoryTasks = tasks.filter((t) => t.categoryLocalId === category.localId);
    const completed = categoryTasks.filter((t) => t.isCompleted).length;
    const total = categoryTasks.length;
    const percentage = total > 0 ? (completed / total) * 100 : 0;

    return {
      category,
      completed,
      total,
      percentage,
    };
  }).filter((item) => item.total > 0);

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const tasksTodayCount = tasks.filter((t) => {
    if (!t.dueDate) return false;
    const dueDate = new Date(t.dueDate);
    dueDate.setHours(0, 0, 0, 0);
    return dueDate.getTime() === today.getTime();
  }).length;

  const completedTodayCount = completedToday.length;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Productivity Insights</h1>
        <p className="text-sm text-foreground-tertiary">
          Track your progress and stay motivated
        </p>
      </div>

      <div className="rounded-lg bg-gradient-to-r from-primary-500 to-primary-700 p-6 text-white">
        <div className="flex items-center gap-3">
          <Award className="h-12 w-12" />
          <div>
            <h2 className="text-3xl font-bold">Daily Score: {dailyScore}/100</h2>
            <p className="text-primary-100">
              You've completed {completedTodayCount} task{completedTodayCount !== 1 ? 's' : ''} today
            </p>
          </div>
        </div>
        <Progress
          value={dailyScore}
          max={100}
          className="mt-4"
          size="lg"
        />
      </div>

      <Grid cols={1} gap={4} className="md:grid-cols-3">
        <Card>
          <CardContent className="py-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-foreground-tertiary">Tasks Today</p>
                <p className="text-3xl font-bold">{completedTodayCount}/{tasksTodayCount}</p>
              </div>
              <Target className="h-8 w-8 text-primary-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="py-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-foreground-tertiary">Focus Sessions</p>
                <p className="text-3xl font-bold">{focusSessionsToday}</p>
              </div>
              <Zap className="h-8 w-8 text-warning-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="py-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-foreground-tertiary">Streak</p>
                <p className="text-3xl font-bold">3</p>
              </div>
              <TrendingUp className="h-8 w-8 text-success-600" />
            </div>
          </CardContent>
        </Card>
      </Grid>

      <Grid cols={1} gap={4} className="md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Focus Timer</CardTitle>
            <CardDescription>
              Start a timed focus session and get a notification at the end.
            </CardDescription>
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
                  const fireAt = new Date(
                    Date.now() + (Number.isFinite(minutes) ? minutes : 10) * 60 * 1000
                  ).toISOString();
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

            <div className="max-h-40 space-y-2 overflow-y-auto">
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

      <Card>
        <CardHeader>
          <CardTitle>Progress by Category</CardTitle>
          <CardDescription>See how you're doing across different areas</CardDescription>
        </CardHeader>
        <CardContent>
          {categoryProgress.length === 0 ? (
            <p className="text-sm text-foreground-tertiary">No tasks with categories yet</p>
          ) : (
            <div className="space-y-4">
              {categoryProgress.map(({ category, completed, total, percentage }) => {
                const Icon = getCategoryIcon(category.icon);
                return (
                  <div key={category.localId}>
                    <div className="mb-2 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        {Icon ? (
                          <Icon className="h-5 w-5" style={{ color: category.color }} />
                        ) : (
                          <div
                            className="h-5 w-5 rounded-full"
                            style={{ backgroundColor: category.color }}
                          />
                        )}
                        <span className="font-medium">{category.name}</span>
                      </div>
                      <span className="text-sm text-foreground-tertiary">
                        {completed}/{total} tasks
                      </span>
                    </div>
                    <Progress
                      value={percentage}
                      max={100}
                      size="md"
                    />
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Clock className="h-5 w-5" />
            End of Day Summary
          </CardTitle>
          <CardDescription>Your productivity at a glance</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex items-center justify-between rounded-lg bg-background-secondary p-3">
              <span className="text-sm font-medium">Tasks Completed</span>
              <span className="text-lg font-bold">{completedTodayCount}</span>
            </div>
            <div className="flex items-center justify-between rounded-lg bg-background-secondary p-3">
              <span className="text-sm font-medium">Focus Sessions</span>
              <span className="text-lg font-bold">{focusSessionsToday}</span>
            </div>
            <div className="flex items-center justify-between rounded-lg bg-background-secondary p-3">
              <span className="text-sm font-medium">Productivity Score</span>
              <span className="text-lg font-bold text-primary-600">{dailyScore}/100</span>
            </div>
            <div className="rounded-lg border border-primary-200 bg-primary-50 p-3">
              <p className="text-sm text-primary-900">
                {dailyScore >= 80
                  ? '🎉 Outstanding work today! You're crushing it!'
                  : dailyScore >= 60
                  ? '👍 Great progress! Keep up the good work.'
                  : dailyScore >= 40
                  ? '💪 You're making progress. Stay focused!'
                  : '🌱 Every journey starts somewhere. Tomorrow is a new day!'}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
