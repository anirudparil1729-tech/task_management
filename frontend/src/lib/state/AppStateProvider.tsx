'use client';

import * as React from 'react';

import { createKVStore } from '@/lib/storage/kv';

export type ISODateString = string;

export type OutboxEntity = 'category' | 'task' | 'timeBlock';
export type OutboxOperation = 'create' | 'update' | 'delete';

export interface OutboxItem {
  id: string;
  entity: OutboxEntity;
  op: OutboxOperation;
  localId: string;
  serverId?: number;
  timestamp: ISODateString;
  payload?: unknown;
}

export interface CategoryEntity {
  localId: string;
  serverId?: number;
  name: string;
  color: string;
  icon: string | null;
  isDefault: boolean;
  createdAt: ISODateString;
  updatedAt: ISODateString;
  deleted?: boolean;
}

export interface TaskEntity {
  localId: string;
  serverId?: number;
  title: string;
  description: string | null;
  notes: string | null;
  dueDate: ISODateString | null;
  reminderTime: ISODateString | null;
  priority: number;
  recurrenceRule: string | null;
  isCompleted: boolean;
  completedAt: ISODateString | null;
  categoryLocalId: string | null;
  createdAt: ISODateString;
  updatedAt: ISODateString;
  deleted?: boolean;
}

export interface TimeBlockEntity {
  localId: string;
  serverId?: number;
  taskLocalId: string | null;
  startTime: ISODateString;
  endTime: ISODateString;
  title: string | null;
  description: string | null;
  createdAt: ISODateString;
  updatedAt: ISODateString;
  deleted?: boolean;
}

export interface ReminderEntity {
  id: string;
  title: string;
  body: string | null;
  fireAt: ISODateString;
  createdAt: ISODateString;
  notifiedAt: ISODateString | null;
}

export interface FocusTimerState {
  status: 'idle' | 'running' | 'completed';
  startedAt: ISODateString | null;
  endsAt: ISODateString | null;
  durationMs: number;
}

export interface AppState {
  categories: Record<string, CategoryEntity>;
  tasks: Record<string, TaskEntity>;
  timeBlocks: Record<string, TimeBlockEntity>;
  outbox: OutboxItem[];
  lastPulledAt: Partial<Record<'categories' | 'tasks' | 'timeBlocks', ISODateString>>;
  reminders: ReminderEntity[];
  focusTimer: FocusTimerState;
}

export interface RemoteCategory {
  id: number;
  name: string;
  color: string;
  icon: string | null;
  is_default: boolean;
  created_at: string;
  updated_at: string;
}

export interface RemoteTask {
  id: number;
  title: string;
  description: string | null;
  notes: string | null;
  due_date: string | null;
  reminder_time: string | null;
  priority: number;
  recurrence_rule: string | null;
  category_id: number | null;
  is_completed: boolean;
  completed_at: string | null;
  created_at: string;
  updated_at: string;
}

export interface RemoteTimeBlock {
  id: number;
  task_id: number | null;
  start_time: string;
  end_time: string;
  title: string | null;
  description: string | null;
  created_at: string;
  updated_at: string;
}

interface AppStateContextValue {
  hydrated: boolean;
  state: AppState;
  createCategory: (input: Pick<CategoryEntity, 'name' | 'color' | 'icon'>) => void;
  updateCategory: (localId: string, updates: Partial<Pick<CategoryEntity, 'name' | 'color' | 'icon'>>) => void;
  deleteCategory: (localId: string) => void;
  createTask: (input: Pick<TaskEntity, 'title'> & { categoryLocalId?: string | null }) => void;
  updateTask: (localId: string, updates: Partial<Omit<TaskEntity, 'localId' | 'serverId' | 'createdAt'>>) => void;
  toggleTaskCompleted: (localId: string) => void;
  deleteTask: (localId: string) => void;
  createTimeBlock: (input: Pick<TimeBlockEntity, 'startTime' | 'endTime' | 'title' | 'taskLocalId' | 'description'>) => void;
  updateTimeBlock: (localId: string, updates: Partial<Omit<TimeBlockEntity, 'localId' | 'serverId' | 'createdAt'>>) => void;
  deleteTimeBlock: (localId: string) => void;
  addReminder: (input: Pick<ReminderEntity, 'title' | 'body' | 'fireAt'>) => void;
  markReminderNotified: (reminderId: string, notifiedAt: ISODateString) => void;
  startFocusTimer: (durationMinutes: number) => void;
  cancelFocusTimer: () => void;
  completeFocusTimer: () => void;

  markOutboxItemProcessed: (outboxId: string) => void;
  linkCategoryToServer: (localId: string, remote: RemoteCategory) => string;
  linkTaskToServer: (localId: string, remote: RemoteTask) => string;
  linkTimeBlockToServer: (localId: string, remote: RemoteTimeBlock) => string;

  mergeRemoteCategories: (remotes: RemoteCategory[]) => void;
  mergeRemoteTasks: (remotes: RemoteTask[]) => void;
  mergeRemoteTimeBlocks: (remotes: RemoteTimeBlock[]) => void;
  setLastPulledAt: (key: 'categories' | 'tasks' | 'timeBlocks', value: ISODateString) => void;
}

const AppStateContext = React.createContext<AppStateContextValue | null>(null);

const APP_STATE_STORAGE_KEY = 'app_state_v1';

function nowIso(): ISODateString {
  return new Date().toISOString();
}

function createLocalId(prefix: string): string {
  return `${prefix}:local:${crypto.randomUUID()}`;
}

function stableServerLocalId(prefix: string, serverId: number): string {
  return `${prefix}:server:${serverId}`;
}

function isNewer(a: ISODateString, b: ISODateString): boolean {
  return new Date(a).getTime() > new Date(b).getTime();
}

function defaultState(): AppState {
  return {
    categories: {},
    tasks: {},
    timeBlocks: {},
    outbox: [],
    lastPulledAt: {},
    reminders: [],
    focusTimer: {
      status: 'idle',
      startedAt: null,
      endsAt: null,
      durationMs: 25 * 60 * 1000,
    },
  };
}

function ensureOutboxItem(
  outbox: OutboxItem[],
  item: OutboxItem
): { outbox: OutboxItem[]; replaced: boolean } {
  const idx = outbox.findIndex(
    (o) => o.entity === item.entity && o.op === 'create' && o.localId === item.localId
  );

  if (idx === -1 || item.op !== 'update') {
    return { outbox: [...outbox, item], replaced: false };
  }

  const existing = outbox[idx];

  const existingPayload = existing.payload;
  const nextPayload = item.payload;
  const mergedPayload =
    typeof existingPayload === 'object' &&
    existingPayload &&
    typeof nextPayload === 'object' &&
    nextPayload
      ? { ...(existingPayload as Record<string, unknown>), ...(nextPayload as Record<string, unknown>) }
      : nextPayload;

  const merged: OutboxItem = {
    ...existing,
    timestamp: item.timestamp,
    payload: mergedPayload,
  };

  const next = [...outbox];
  next[idx] = merged;
  return { outbox: next, replaced: true };
}

export function AppStateProvider({ children }: { children: React.ReactNode }) {
  const kv = React.useMemo(() => createKVStore(), []);
  const [hydrated, setHydrated] = React.useState(false);
  const [state, setState] = React.useState<AppState>(defaultState);

  React.useEffect(() => {
    let mounted = true;

    (async () => {
      try {
        const stored = await kv.get<AppState>(APP_STATE_STORAGE_KEY);
        if (!mounted) {
          return;
        }

        if (stored) {
          setState({ ...defaultState(), ...stored });
        }
      } finally {
        if (mounted) {
          setHydrated(true);
        }
      }
    })();

    return () => {
      mounted = false;
    };
  }, [kv]);

  React.useEffect(() => {
    if (!hydrated) {
      return;
    }

    const handle = window.setTimeout(() => {
      void kv.set(APP_STATE_STORAGE_KEY, state);
    }, 250);

    return () => window.clearTimeout(handle);
  }, [state, hydrated, kv]);

  const createCategory = React.useCallback(
    (input: Pick<CategoryEntity, 'name' | 'color' | 'icon'>) => {
      const localId = createLocalId('category');
      const timestamp = nowIso();

      const category: CategoryEntity = {
        localId,
        name: input.name,
        color: input.color,
        icon: input.icon ?? null,
        isDefault: false,
        createdAt: timestamp,
        updatedAt: timestamp,
      };

      const outboxItem: OutboxItem = {
        id: crypto.randomUUID(),
        entity: 'category',
        op: 'create',
        localId,
        timestamp,
        payload: {
          name: category.name,
          color: category.color,
          icon: category.icon,
          is_default: category.isDefault,
        },
      };

      setState((prev) => ({
        ...prev,
        categories: { ...prev.categories, [localId]: category },
        outbox: [...prev.outbox, outboxItem],
      }));
    },
    []
  );

  const updateCategory = React.useCallback(
    (localId: string, updates: Partial<Pick<CategoryEntity, 'name' | 'color' | 'icon'>>) => {
      setState((prev) => {
        const category = prev.categories[localId];
        if (!category) {
          return prev;
        }

        const timestamp = nowIso();
        const updated: CategoryEntity = {
          ...category,
          ...updates,
          updatedAt: timestamp,
        };

        const payload = {
          ...(updates.name !== undefined && { name: updates.name }),
          ...(updates.color !== undefined && { color: updates.color }),
          ...(updates.icon !== undefined && { icon: updates.icon }),
        };

        const outboxItem: OutboxItem = {
          id: crypto.randomUUID(),
          entity: 'category',
          op: category.serverId ? 'update' : 'update',
          localId,
          serverId: category.serverId,
          timestamp,
          payload,
        };

        const { outbox } = ensureOutboxItem(prev.outbox, outboxItem);

        return { ...prev, categories: { ...prev.categories, [localId]: updated }, outbox };
      });
    },
    []
  );

  const deleteCategory = React.useCallback((localId: string) => {
    setState((prev) => {
      const category = prev.categories[localId];
      if (!category) {
        return prev;
      }

      const nextCategories = { ...prev.categories };
      delete nextCategories[localId];

      const existingCreateIdx = prev.outbox.findIndex(
        (o) => o.entity === 'category' && o.op === 'create' && o.localId === localId
      );

      if (!category.serverId && existingCreateIdx !== -1) {
        const nextOutbox = [...prev.outbox];
        nextOutbox.splice(existingCreateIdx, 1);
        return { ...prev, categories: nextCategories, outbox: nextOutbox };
      }

      if (!category.serverId) {
        return { ...prev, categories: nextCategories };
      }

      const outboxItem: OutboxItem = {
        id: crypto.randomUUID(),
        entity: 'category',
        op: 'delete',
        localId,
        serverId: category.serverId,
        timestamp: nowIso(),
      };

      return { ...prev, categories: nextCategories, outbox: [...prev.outbox, outboxItem] };
    });
  }, []);

  const createTask = React.useCallback(
    (input: Pick<TaskEntity, 'title'> & { categoryLocalId?: string | null }) => {
      const localId = createLocalId('task');
      const timestamp = nowIso();

      const task: TaskEntity = {
        localId,
        title: input.title,
        description: null,
        notes: null,
        dueDate: null,
        reminderTime: null,
        priority: 0,
        recurrenceRule: null,
        isCompleted: false,
        completedAt: null,
        categoryLocalId: input.categoryLocalId ?? null,
        createdAt: timestamp,
        updatedAt: timestamp,
      };

      const outboxItem: OutboxItem = {
        id: crypto.randomUUID(),
        entity: 'task',
        op: 'create',
        localId,
        timestamp,
        payload: {
          title: task.title,
          description: task.description,
          notes: task.notes,
          due_date: task.dueDate,
          reminder_time: task.reminderTime,
          priority: task.priority,
          recurrence_rule: task.recurrenceRule,
          category_id: null,
        },
      };

      setState((prev) => ({
        ...prev,
        tasks: { ...prev.tasks, [localId]: task },
        outbox: [...prev.outbox, outboxItem],
      }));
    },
    []
  );

  const updateTask = React.useCallback(
    (localId: string, updates: Partial<Omit<TaskEntity, 'localId' | 'serverId' | 'createdAt'>>) => {
      setState((prev) => {
        const task = prev.tasks[localId];
        if (!task) {
          return prev;
        }

        const timestamp = nowIso();
        const updated: TaskEntity = {
          ...task,
          ...updates,
          updatedAt: timestamp,
        };

        const payload = {
          ...(updates.title !== undefined && { title: updates.title }),
          ...(updates.description !== undefined && { description: updates.description }),
          ...(updates.notes !== undefined && { notes: updates.notes }),
          ...(updates.dueDate !== undefined && { due_date: updates.dueDate }),
          ...(updates.reminderTime !== undefined && { reminder_time: updates.reminderTime }),
          ...(updates.priority !== undefined && { priority: updates.priority }),
          ...(updates.recurrenceRule !== undefined && { recurrence_rule: updates.recurrenceRule }),
          ...(updates.isCompleted !== undefined && { is_completed: updates.isCompleted }),
          ...(updates.categoryLocalId !== undefined && {
            category_id: updates.categoryLocalId
              ? prev.categories[updates.categoryLocalId]?.serverId ?? null
              : null,
          }),
        };

        const outboxItem: OutboxItem = {
          id: crypto.randomUUID(),
          entity: 'task',
          op: task.serverId ? 'update' : 'update',
          localId,
          serverId: task.serverId,
          timestamp,
          payload,
        };

        const { outbox } = ensureOutboxItem(prev.outbox, outboxItem);

        return { ...prev, tasks: { ...prev.tasks, [localId]: updated }, outbox };
      });
    },
    []
  );

  const toggleTaskCompleted = React.useCallback((localId: string) => {
    setState((prev) => {
      const task = prev.tasks[localId];
      if (!task) {
        return prev;
      }

      const timestamp = nowIso();
      const isCompleted = !task.isCompleted;

      const updated: TaskEntity = {
        ...task,
        isCompleted,
        completedAt: isCompleted ? timestamp : null,
        updatedAt: timestamp,
      };

      const payload = {
        is_completed: updated.isCompleted,
      };

      const outboxItem: OutboxItem = {
        id: crypto.randomUUID(),
        entity: 'task',
        op: task.serverId ? 'update' : 'update',
        localId,
        serverId: task.serverId,
        timestamp,
        payload,
      };

      const { outbox } = ensureOutboxItem(prev.outbox, outboxItem);

      return { ...prev, tasks: { ...prev.tasks, [localId]: updated }, outbox };
    });
  }, []);

  const deleteTask = React.useCallback((localId: string) => {
    setState((prev) => {
      const task = prev.tasks[localId];
      if (!task) {
        return prev;
      }

      const nextTasks = { ...prev.tasks };
      delete nextTasks[localId];

      const existingCreateIdx = prev.outbox.findIndex(
        (o) => o.entity === 'task' && o.op === 'create' && o.localId === localId
      );

      if (!task.serverId && existingCreateIdx !== -1) {
        const nextOutbox = [...prev.outbox];
        nextOutbox.splice(existingCreateIdx, 1);
        return { ...prev, tasks: nextTasks, outbox: nextOutbox };
      }

      if (!task.serverId) {
        return { ...prev, tasks: nextTasks };
      }

      const outboxItem: OutboxItem = {
        id: crypto.randomUUID(),
        entity: 'task',
        op: 'delete',
        localId,
        serverId: task.serverId,
        timestamp: nowIso(),
      };

      return { ...prev, tasks: nextTasks, outbox: [...prev.outbox, outboxItem] };
    });
  }, []);

  const createTimeBlock = React.useCallback(
    (input: Pick<TimeBlockEntity, 'startTime' | 'endTime' | 'title' | 'taskLocalId' | 'description'>) => {
      const localId = createLocalId('timeBlock');
      const timestamp = nowIso();

      const timeBlock: TimeBlockEntity = {
        localId,
        taskLocalId: input.taskLocalId ?? null,
        startTime: input.startTime,
        endTime: input.endTime,
        title: input.title ?? null,
        description: input.description ?? null,
        createdAt: timestamp,
        updatedAt: timestamp,
      };

      const outboxItem: OutboxItem = {
        id: crypto.randomUUID(),
        entity: 'timeBlock',
        op: 'create',
        localId,
        timestamp,
        payload: {
          task_id: null,
          start_time: timeBlock.startTime,
          end_time: timeBlock.endTime,
          title: timeBlock.title,
          description: timeBlock.description,
        },
      };

      setState((prev) => ({
        ...prev,
        timeBlocks: { ...prev.timeBlocks, [localId]: timeBlock },
        outbox: [...prev.outbox, outboxItem],
      }));
    },
    []
  );

  const updateTimeBlock = React.useCallback(
    (localId: string, updates: Partial<Omit<TimeBlockEntity, 'localId' | 'serverId' | 'createdAt'>>) => {
      setState((prev) => {
        const timeBlock = prev.timeBlocks[localId];
        if (!timeBlock) {
          return prev;
        }

        const timestamp = nowIso();
        const updated: TimeBlockEntity = {
          ...timeBlock,
          ...updates,
          updatedAt: timestamp,
        };

        const payload = {
          ...(updates.taskLocalId !== undefined && {
            task_id: updates.taskLocalId
              ? prev.tasks[updates.taskLocalId]?.serverId ?? null
              : null,
          }),
          ...(updates.startTime !== undefined && { start_time: updates.startTime }),
          ...(updates.endTime !== undefined && { end_time: updates.endTime }),
          ...(updates.title !== undefined && { title: updates.title }),
          ...(updates.description !== undefined && { description: updates.description }),
        };

        const outboxItem: OutboxItem = {
          id: crypto.randomUUID(),
          entity: 'timeBlock',
          op: timeBlock.serverId ? 'update' : 'update',
          localId,
          serverId: timeBlock.serverId,
          timestamp,
          payload,
        };

        const { outbox } = ensureOutboxItem(prev.outbox, outboxItem);

        return { ...prev, timeBlocks: { ...prev.timeBlocks, [localId]: updated }, outbox };
      });
    },
    []
  );

  const deleteTimeBlock = React.useCallback((localId: string) => {
    setState((prev) => {
      const timeBlock = prev.timeBlocks[localId];
      if (!timeBlock) {
        return prev;
      }

      const nextTimeBlocks = { ...prev.timeBlocks };
      delete nextTimeBlocks[localId];

      const existingCreateIdx = prev.outbox.findIndex(
        (o) => o.entity === 'timeBlock' && o.op === 'create' && o.localId === localId
      );

      if (!timeBlock.serverId && existingCreateIdx !== -1) {
        const nextOutbox = [...prev.outbox];
        nextOutbox.splice(existingCreateIdx, 1);
        return { ...prev, timeBlocks: nextTimeBlocks, outbox: nextOutbox };
      }

      if (!timeBlock.serverId) {
        return { ...prev, timeBlocks: nextTimeBlocks };
      }

      const outboxItem: OutboxItem = {
        id: crypto.randomUUID(),
        entity: 'timeBlock',
        op: 'delete',
        localId,
        serverId: timeBlock.serverId,
        timestamp: nowIso(),
      };

      return { ...prev, timeBlocks: nextTimeBlocks, outbox: [...prev.outbox, outboxItem] };
    });
  }, []);

  const addReminder = React.useCallback((input: Pick<ReminderEntity, 'title' | 'body' | 'fireAt'>) => {
    const reminder: ReminderEntity = {
      id: crypto.randomUUID(),
      title: input.title,
      body: input.body ?? null,
      fireAt: input.fireAt,
      createdAt: nowIso(),
      notifiedAt: null,
    };

    setState((prev) => ({ ...prev, reminders: [...prev.reminders, reminder] }));
  }, []);

  const markReminderNotified = React.useCallback((reminderId: string, notifiedAt: ISODateString) => {
    setState((prev) => ({
      ...prev,
      reminders: prev.reminders.map((r) => (r.id === reminderId ? { ...r, notifiedAt } : r)),
    }));
  }, []);

  const startFocusTimer = React.useCallback((durationMinutes: number) => {
    const durationMs = Math.max(1, durationMinutes) * 60 * 1000;
    const startedAt = nowIso();
    const endsAt = new Date(Date.now() + durationMs).toISOString();

    setState((prev) => ({
      ...prev,
      focusTimer: {
        status: 'running',
        startedAt,
        endsAt,
        durationMs,
      },
    }));
  }, []);

  const cancelFocusTimer = React.useCallback(() => {
    setState((prev) => ({
      ...prev,
      focusTimer: {
        ...prev.focusTimer,
        status: 'idle',
        startedAt: null,
        endsAt: null,
      },
    }));
  }, []);

  const completeFocusTimer = React.useCallback(() => {
    setState((prev) => ({
      ...prev,
      focusTimer: {
        ...prev.focusTimer,
        status: 'completed',
      },
    }));
  }, []);

  const markOutboxItemProcessed = React.useCallback((outboxId: string) => {
    setState((prev) => ({ ...prev, outbox: prev.outbox.filter((o) => o.id !== outboxId) }));
  }, []);

  const setLastPulledAt = React.useCallback(
    (key: 'categories' | 'tasks' | 'timeBlocks', value: ISODateString) => {
      setState((prev) => ({ ...prev, lastPulledAt: { ...prev.lastPulledAt, [key]: value } }));
    },
    []
  );

  const linkCategoryToServer = React.useCallback((localId: string, remote: RemoteCategory): string => {
    const nextLocalId = stableServerLocalId('category', remote.id);

    setState((prev) => {
      const current = prev.categories[localId];
      if (!current) {
        return prev;
      }

      const linked: CategoryEntity = {
        ...current,
        localId: nextLocalId,
        serverId: remote.id,
        name: remote.name,
        color: remote.color,
        icon: remote.icon,
        isDefault: remote.is_default,
        createdAt: remote.created_at,
        updatedAt: remote.updated_at,
      };

      const nextCategories = { ...prev.categories };
      delete nextCategories[localId];
      nextCategories[nextLocalId] = linked;

      const nextTasks: Record<string, TaskEntity> = {};
      for (const [tid, task] of Object.entries(prev.tasks)) {
        if (task.categoryLocalId === localId) {
          nextTasks[tid] = { ...task, categoryLocalId: nextLocalId };
        } else {
          nextTasks[tid] = task;
        }
      }

      return { ...prev, categories: nextCategories, tasks: nextTasks };
    });

    return nextLocalId;
  }, []);

  const linkTaskToServer = React.useCallback((localId: string, remote: RemoteTask): string => {
    const nextLocalId = stableServerLocalId('task', remote.id);

    setState((prev) => {
      const current = prev.tasks[localId];
      if (!current) {
        return prev;
      }

      const linked: TaskEntity = {
        ...current,
        localId: nextLocalId,
        serverId: remote.id,
        title: remote.title,
        description: remote.description,
        notes: remote.notes,
        dueDate: remote.due_date,
        reminderTime: remote.reminder_time,
        priority: remote.priority,
        recurrenceRule: remote.recurrence_rule,
        isCompleted: remote.is_completed,
        completedAt: remote.completed_at,
        createdAt: remote.created_at,
        updatedAt: remote.updated_at,
      };

      const nextTasks = { ...prev.tasks };
      delete nextTasks[localId];
      nextTasks[nextLocalId] = linked;

      const nextTimeBlocks: Record<string, TimeBlockEntity> = {};
      for (const [bid, block] of Object.entries(prev.timeBlocks)) {
        if (block.taskLocalId === localId) {
          nextTimeBlocks[bid] = { ...block, taskLocalId: nextLocalId };
        } else {
          nextTimeBlocks[bid] = block;
        }
      }

      return { ...prev, tasks: nextTasks, timeBlocks: nextTimeBlocks };
    });

    return nextLocalId;
  }, []);

  const linkTimeBlockToServer = React.useCallback((localId: string, remote: RemoteTimeBlock): string => {
    const nextLocalId = stableServerLocalId('timeBlock', remote.id);

    setState((prev) => {
      const current = prev.timeBlocks[localId];
      if (!current) {
        return prev;
      }

      const linked: TimeBlockEntity = {
        ...current,
        localId: nextLocalId,
        serverId: remote.id,
        startTime: remote.start_time,
        endTime: remote.end_time,
        title: remote.title,
        description: remote.description,
        createdAt: remote.created_at,
        updatedAt: remote.updated_at,
      };

      const nextBlocks = { ...prev.timeBlocks };
      delete nextBlocks[localId];
      nextBlocks[nextLocalId] = linked;

      return { ...prev, timeBlocks: nextBlocks };
    });

    return nextLocalId;
  }, []);

  const mergeRemoteCategories = React.useCallback((remotes: RemoteCategory[]) => {
    setState((prev) => {
      const nextCategories = { ...prev.categories };
      const replacedLocalIds = new Set<string>();

      for (const remote of remotes) {
        const serverKey = stableServerLocalId('category', remote.id);
        const existing = Object.values(nextCategories).find((c) => c.serverId === remote.id) ?? null;
        const existingKey = existing?.localId;
        const targetKey = existingKey ?? serverKey;
        const current = existingKey ? nextCategories[existingKey] : nextCategories[serverKey];

        const incoming: CategoryEntity = {
          localId: targetKey,
          serverId: remote.id,
          name: remote.name,
          color: remote.color,
          icon: remote.icon,
          isDefault: remote.is_default,
          createdAt: remote.created_at,
          updatedAt: remote.updated_at,
        };

        if (!current || isNewer(remote.updated_at, current.updatedAt)) {
          nextCategories[targetKey] = incoming;
          replacedLocalIds.add(targetKey);
          if (existingKey && existingKey !== targetKey) {
            delete nextCategories[existingKey];
          }
        }
      }

      const nextOutbox = replacedLocalIds.size
        ? prev.outbox.filter(
            (o) => !(o.entity === 'category' && o.op === 'update' && replacedLocalIds.has(o.localId))
          )
        : prev.outbox;

      return { ...prev, categories: nextCategories, outbox: nextOutbox };
    });
  }, []);

  const mergeRemoteTasks = React.useCallback((remotes: RemoteTask[]) => {
    setState((prev) => {
      const nextTasks = { ...prev.tasks };
      const replacedLocalIds = new Set<string>();

      for (const remote of remotes) {
        const serverKey = stableServerLocalId('task', remote.id);
        const existing = Object.values(nextTasks).find((t) => t.serverId === remote.id) ?? null;
        const targetKey = existing?.localId ?? serverKey;
        const current = existing ? nextTasks[existing.localId] : nextTasks[serverKey];

        const categoryLocalId = remote.category_id
          ? Object.values(prev.categories).find((c) => c.serverId === remote.category_id)?.localId ?? null
          : null;

        const incoming: TaskEntity = {
          localId: targetKey,
          serverId: remote.id,
          title: remote.title,
          description: remote.description,
          notes: remote.notes,
          dueDate: remote.due_date,
          reminderTime: remote.reminder_time,
          priority: remote.priority,
          recurrenceRule: remote.recurrence_rule,
          isCompleted: remote.is_completed,
          completedAt: remote.completed_at,
          categoryLocalId: categoryLocalId ?? current?.categoryLocalId ?? null,
          createdAt: remote.created_at,
          updatedAt: remote.updated_at,
        };

        if (!current || isNewer(remote.updated_at, current.updatedAt)) {
          nextTasks[targetKey] = incoming;
          replacedLocalIds.add(targetKey);
        }
      }

      const nextOutbox = replacedLocalIds.size
        ? prev.outbox.filter(
            (o) => !(o.entity === 'task' && o.op === 'update' && replacedLocalIds.has(o.localId))
          )
        : prev.outbox;

      return { ...prev, tasks: nextTasks, outbox: nextOutbox };
    });
  }, []);

  const mergeRemoteTimeBlocks = React.useCallback((remotes: RemoteTimeBlock[]) => {
    setState((prev) => {
      const nextTimeBlocks = { ...prev.timeBlocks };
      const replacedLocalIds = new Set<string>();

      for (const remote of remotes) {
        const serverKey = stableServerLocalId('timeBlock', remote.id);
        const existing = Object.values(nextTimeBlocks).find((b) => b.serverId === remote.id) ?? null;
        const targetKey = existing?.localId ?? serverKey;
        const current = existing ? nextTimeBlocks[existing.localId] : nextTimeBlocks[serverKey];

        const taskLocalId = remote.task_id
          ? Object.values(prev.tasks).find((t) => t.serverId === remote.task_id)?.localId ?? null
          : null;

        const incoming: TimeBlockEntity = {
          localId: targetKey,
          serverId: remote.id,
          taskLocalId: taskLocalId ?? current?.taskLocalId ?? null,
          startTime: remote.start_time,
          endTime: remote.end_time,
          title: remote.title,
          description: remote.description,
          createdAt: remote.created_at,
          updatedAt: remote.updated_at,
        };

        if (!current || isNewer(remote.updated_at, current.updatedAt)) {
          nextTimeBlocks[targetKey] = incoming;
          replacedLocalIds.add(targetKey);
        }
      }

      const nextOutbox = replacedLocalIds.size
        ? prev.outbox.filter(
            (o) => !(o.entity === 'timeBlock' && o.op === 'update' && replacedLocalIds.has(o.localId))
          )
        : prev.outbox;

      return { ...prev, timeBlocks: nextTimeBlocks, outbox: nextOutbox };
    });
  }, []);

  const value = React.useMemo<AppStateContextValue>(
    () => ({
      hydrated,
      state,
      createCategory,
      updateCategory,
      deleteCategory,
      createTask,
      updateTask,
      toggleTaskCompleted,
      deleteTask,
      createTimeBlock,
      updateTimeBlock,
      deleteTimeBlock,
      addReminder,
      markReminderNotified,
      startFocusTimer,
      cancelFocusTimer,
      completeFocusTimer,
      markOutboxItemProcessed,
      linkCategoryToServer,
      linkTaskToServer,
      linkTimeBlockToServer,
      mergeRemoteCategories,
      mergeRemoteTasks,
      mergeRemoteTimeBlocks,
      setLastPulledAt,
    }),
    [
      hydrated,
      state,
      createCategory,
      updateCategory,
      deleteCategory,
      createTask,
      updateTask,
      toggleTaskCompleted,
      deleteTask,
      createTimeBlock,
      updateTimeBlock,
      deleteTimeBlock,
      addReminder,
      markReminderNotified,
      startFocusTimer,
      cancelFocusTimer,
      completeFocusTimer,
      markOutboxItemProcessed,
      linkCategoryToServer,
      linkTaskToServer,
      linkTimeBlockToServer,
      mergeRemoteCategories,
      mergeRemoteTasks,
      mergeRemoteTimeBlocks,
      setLastPulledAt,
    ]
  );

  return <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>;
}

export function useAppState(): AppStateContextValue {
  const ctx = React.useContext(AppStateContext);
  if (!ctx) {
    throw new Error('useAppState must be used within AppStateProvider');
  }
  return ctx;
}
