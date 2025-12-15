'use client';

import * as React from 'react';

import { Card, CardDescription, CardHeader, CardTitle, Grid } from '@/design-system/components';
import { PageSkeleton } from '@/components/Skeleton';
import { useAppState } from '@/lib/state/AppStateProvider';
import { useSync } from '@/lib/sync/SyncProvider';

export default function DashboardPage() {
  const { hydrated, state } = useAppState();
  const { status: syncStatus, lastSyncedAt } = useSync();

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const tasks = Object.values(state.tasks);
  const completed = tasks.filter((t) => t.isCompleted).length;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Dashboard</h1>
        <p className="text-sm text-foreground-tertiary">
          Offline-first state is enabled. Sync runs automatically when online.
        </p>
      </div>

      <Grid cols={1} gap={4} className="md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Tasks</CardTitle>
            <CardDescription>
              {tasks.length} total, {completed} completed
            </CardDescription>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Local changes</CardTitle>
            <CardDescription>
              {state.outbox.length} pending change{state.outbox.length === 1 ? '' : 's'}
            </CardDescription>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Sync status</CardTitle>
            <CardDescription>
              {syncStatus}
              {lastSyncedAt ? ` • last: ${new Date(lastSyncedAt).toLocaleString()}` : ''}
            </CardDescription>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Reminders</CardTitle>
            <CardDescription>
              {state.reminders.filter((r) => !r.notifiedAt).length} scheduled
            </CardDescription>
          </CardHeader>
        </Card>
      </Grid>
    </div>
  );
}
