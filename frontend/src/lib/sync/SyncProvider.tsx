'use client';

import * as React from 'react';

import { fetchFromAPI } from '@/lib/api';
import { useAuth } from '@/lib/auth/AuthProvider';
import {
  type OutboxItem,
  type RemoteCategory,
  type RemoteTask,
  type RemoteTimeBlock,
  useAppState,
} from '@/lib/state/AppStateProvider';

export type SyncStatus = 'idle' | 'syncing' | 'offline' | 'error';

interface SyncContextValue {
  status: SyncStatus;
  lastSyncedAt: string | null;
  lastError: string | null;
  syncNow: () => Promise<void>;
}

const SyncContext = React.createContext<SyncContextValue | null>(null);

function isOnline(): boolean {
  return typeof navigator === 'undefined' ? true : navigator.onLine;
}

export function SyncProvider({ children }: { children: React.ReactNode }) {
  const { status: authStatus, session } = useAuth();
  const {
    hydrated,
    state,
    markOutboxItemProcessed,
    linkCategoryToServer,
    linkTaskToServer,
    linkTimeBlockToServer,
    mergeRemoteCategories,
    mergeRemoteTasks,
    mergeRemoteTimeBlocks,
    setLastPulledAt,
  } = useAppState();

  const [status, setStatus] = React.useState<SyncStatus>('idle');
  const [lastSyncedAt, setLastSyncedAt] = React.useState<string | null>(null);
  const [lastError, setLastError] = React.useState<string | null>(null);

  const pushOutboxItem = React.useCallback(
    async (item: OutboxItem) => {
      if (!session?.apiKey) {
        throw new Error('Missing API key');
      }

      if (item.entity === 'category') {
        const category = state.categories[item.localId];
        if (item.op === 'create') {
          if (!category) {
            return;
          }

          const created = await fetchFromAPI(
            '/api/categories',
            {
              method: 'POST',
              body: JSON.stringify({
                name: category.name,
                color: category.color,
                icon: category.icon,
                is_default: category.isDefault,
              }),
            },
            session.apiKey
          );

          linkCategoryToServer(item.localId, created as RemoteCategory);
          return;
        }

        if (item.op === 'update') {
          if (!category?.serverId) {
            return;
          }

          await fetchFromAPI(
            `/api/categories/${category.serverId}`,
            {
              method: 'PUT',
              body: JSON.stringify(item.payload ?? {}),
            },
            session.apiKey
          );
          return;
        }

        if (item.op === 'delete') {
          if (!item.serverId) {
            return;
          }

          await fetchFromAPI(
            `/api/categories/${item.serverId}`,
            {
              method: 'DELETE',
            },
            session.apiKey
          );
          return;
        }
      }

      if (item.entity === 'task') {
        const task = state.tasks[item.localId];

        const categoryServerId =
          task?.categoryLocalId ? state.categories[task.categoryLocalId]?.serverId ?? null : null;

        if (item.op === 'create') {
          if (!task) {
            return;
          }

          const created = await fetchFromAPI(
            '/api/tasks',
            {
              method: 'POST',
              body: JSON.stringify({
                title: task.title,
                description: task.description,
                notes: task.notes,
                due_date: task.dueDate,
                reminder_time: task.reminderTime,
                priority: task.priority,
                recurrence_rule: task.recurrenceRule,
                category_id: categoryServerId,
              }),
            },
            session.apiKey
          );

          const remoteTask = created as RemoteTask;
          linkTaskToServer(item.localId, remoteTask);

          if (task.isCompleted) {
            await fetchFromAPI(
              `/api/tasks/${remoteTask.id}`,
              {
                method: 'PUT',
                body: JSON.stringify({ is_completed: true }),
              },
              session.apiKey
            );
          }

          return;
        }

        if (item.op === 'update') {
          const serverId = task?.serverId ?? item.serverId;
          if (!serverId) {
            return;
          }

          const payload = {
            ...(typeof item.payload === 'object' && item.payload ? (item.payload as object) : {}),
            ...(task?.categoryLocalId ? { category_id: categoryServerId } : {}),
          };

          await fetchFromAPI(
            `/api/tasks/${serverId}`,
            {
              method: 'PUT',
              body: JSON.stringify(payload),
            },
            session.apiKey
          );
          return;
        }

        if (item.op === 'delete') {
          const serverId = task?.serverId ?? item.serverId;
          if (!serverId) {
            return;
          }

          await fetchFromAPI(
            `/api/tasks/${serverId}`,
            {
              method: 'DELETE',
            },
            session.apiKey
          );
          return;
        }
      }

      if (item.entity === 'timeBlock') {
        const block = state.timeBlocks[item.localId];
        const taskServerId =
          block?.taskLocalId ? state.tasks[block.taskLocalId]?.serverId ?? null : null;

        if (item.op === 'create') {
          if (!block) {
            return;
          }

          const created = await fetchFromAPI(
            '/api/time-blocks',
            {
              method: 'POST',
              body: JSON.stringify({
                task_id: taskServerId,
                start_time: block.startTime,
                end_time: block.endTime,
                title: block.title,
                description: block.description,
              }),
            },
            session.apiKey
          );

          linkTimeBlockToServer(item.localId, created as RemoteTimeBlock);
          return;
        }

        if (item.op === 'update') {
          const serverId = block?.serverId ?? item.serverId;
          if (!serverId) {
            return;
          }

          await fetchFromAPI(
            `/api/time-blocks/${serverId}`,
            {
              method: 'PUT',
              body: JSON.stringify(item.payload ?? {}),
            },
            session.apiKey
          );
          return;
        }

        if (item.op === 'delete') {
          const serverId = block?.serverId ?? item.serverId;
          if (!serverId) {
            return;
          }

          await fetchFromAPI(
            `/api/time-blocks/${serverId}`,
            {
              method: 'DELETE',
            },
            session.apiKey
          );
        }
      }
    },
    [
      session?.apiKey,
      state.categories,
      state.tasks,
      state.timeBlocks,
      linkCategoryToServer,
      linkTaskToServer,
      linkTimeBlockToServer,
    ]
  );

  const pullRemoteChanges = React.useCallback(async () => {
    if (!session?.apiKey) {
      throw new Error('Missing API key');
    }

    const now = new Date().toISOString();

    const categories = (await fetchFromAPI(
      `/api/sync/categories${
        state.lastPulledAt.categories
          ? `?modified_since=${encodeURIComponent(state.lastPulledAt.categories)}`
          : ''
      }`,
      {},
      session.apiKey
    )) as RemoteCategory[];

    const tasks = (await fetchFromAPI(
      `/api/sync/tasks${
        state.lastPulledAt.tasks ? `?modified_since=${encodeURIComponent(state.lastPulledAt.tasks)}` : ''
      }`,
      {},
      session.apiKey
    )) as RemoteTask[];

    const timeBlocks = (await fetchFromAPI(
      `/api/sync/time-blocks${
        state.lastPulledAt.timeBlocks ? `?modified_since=${encodeURIComponent(state.lastPulledAt.timeBlocks)}` : ''
      }`,
      {},
      session.apiKey
    )) as RemoteTimeBlock[];

    mergeRemoteCategories(categories);
    mergeRemoteTasks(tasks);
    mergeRemoteTimeBlocks(timeBlocks);

    setLastPulledAt('categories', now);
    setLastPulledAt('tasks', now);
    setLastPulledAt('timeBlocks', now);
  }, [
    mergeRemoteCategories,
    mergeRemoteTasks,
    mergeRemoteTimeBlocks,
    session?.apiKey,
    setLastPulledAt,
    state.lastPulledAt.categories,
    state.lastPulledAt.tasks,
    state.lastPulledAt.timeBlocks,
  ]);

  const syncNow = React.useCallback(async () => {
    if (!hydrated || authStatus !== 'authenticated' || !session?.apiKey) {
      return;
    }

    if (!isOnline()) {
      setStatus('offline');
      return;
    }

    setStatus('syncing');
    setLastError(null);

    try {
      for (const item of state.outbox) {
        await pushOutboxItem(item);
        markOutboxItemProcessed(item.id);
      }

      await pullRemoteChanges();

      const stamp = new Date().toISOString();
      setLastSyncedAt(stamp);
      setStatus('idle');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Sync failed';
      setLastError(message);
      setStatus('error');
    }
  }, [
    authStatus,
    hydrated,
    markOutboxItemProcessed,
    pullRemoteChanges,
    pushOutboxItem,
    session?.apiKey,
    state.outbox,
  ]);

  React.useEffect(() => {
    if (!hydrated || authStatus !== 'authenticated') {
      return;
    }

    void syncNow();
  }, [authStatus, hydrated, syncNow]);

  React.useEffect(() => {
    if (!hydrated || authStatus !== 'authenticated') {
      return;
    }

    const onOnline = () => {
      setStatus('idle');
      void syncNow();
    };

    const onOffline = () => {
      setStatus('offline');
    };

    window.addEventListener('online', onOnline);
    window.addEventListener('offline', onOffline);

    return () => {
      window.removeEventListener('online', onOnline);
      window.removeEventListener('offline', onOffline);
    };
  }, [authStatus, hydrated, syncNow]);

  React.useEffect(() => {
    if (!hydrated || authStatus !== 'authenticated') {
      return;
    }

    const interval = window.setInterval(() => {
      void syncNow();
    }, 30_000);

    return () => window.clearInterval(interval);
  }, [authStatus, hydrated, syncNow]);

  const value = React.useMemo<SyncContextValue>(
    () => ({ status, lastSyncedAt, lastError, syncNow }),
    [status, lastSyncedAt, lastError, syncNow]
  );

  return <SyncContext.Provider value={value}>{children}</SyncContext.Provider>;
}

export function useSync(): SyncContextValue {
  const ctx = React.useContext(SyncContext);
  if (!ctx) {
    throw new Error('useSync must be used within SyncProvider');
  }
  return ctx;
}
