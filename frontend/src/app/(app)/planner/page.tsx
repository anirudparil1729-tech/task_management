'use client';

import * as React from 'react';
import { Check, Trash2 } from 'lucide-react';

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
} from '@/design-system/components';
import { PageSkeleton } from '@/components/Skeleton';
import { cn } from '@/design-system/utils';
import { useAppState } from '@/lib/state/AppStateProvider';

export default function PlannerPage() {
  const { hydrated, state, createTask, toggleTaskCompleted, deleteTask } = useAppState();
  const [title, setTitle] = React.useState('');

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const tasks = Object.values(state.tasks).sort(
    (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Planner</h1>
        <p className="text-sm text-foreground-tertiary">
          Add tasks offline. Changes queue locally and sync when online.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Add task</CardTitle>
          <CardDescription>A minimal offline-first task list.</CardDescription>
        </CardHeader>
        <CardContent>
          <form
            className="flex flex-col gap-3 md:flex-row md:items-end"
            onSubmit={(e) => {
              e.preventDefault();
              if (!title.trim()) {
                return;
              }
              createTask({ title: title.trim() });
              setTitle('');
            }}
          >
            <div className="flex-1">
              <Input label="Title" value={title} onChange={(e) => setTitle(e.target.value)} />
            </div>
            <Button type="submit">Add</Button>
          </form>
        </CardContent>
      </Card>

      <div className="space-y-3">
        {tasks.length === 0 ? (
          <Card>
            <CardHeader>
              <CardTitle>No tasks yet</CardTitle>
              <CardDescription>Create one above to get started.</CardDescription>
            </CardHeader>
          </Card>
        ) : (
          tasks.map((t) => (
            <Card key={t.localId} padding="sm">
              <CardContent className="flex items-center justify-between gap-3">
                <button
                  className="flex min-w-0 flex-1 items-center gap-3 text-left"
                  onClick={() => toggleTaskCompleted(t.localId)}
                >
                  <span
                    className={cn(
                      'flex h-6 w-6 items-center justify-center rounded-full border border-border-primary',
                      t.isCompleted ? 'bg-success-600 text-white' : 'bg-background-primary'
                    )}
                  >
                    {t.isCompleted ? <Check className="h-4 w-4" /> : null}
                  </span>

                  <span className={cn('truncate', t.isCompleted && 'text-foreground-tertiary line-through')}>
                    {t.title}
                  </span>
                </button>

                <Button variant="ghost" size="sm" onClick={() => deleteTask(t.localId)}>
                  <Trash2 className="h-4 w-4" />
                </Button>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}
