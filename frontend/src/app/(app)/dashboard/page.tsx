'use client';

import * as React from 'react';
import { Plus, AlertCircle, CheckCircle2, Clock, TrendingUp } from 'lucide-react';

import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
  CardContent,
  Grid,
  Button,
  Input,
  Badge,
  Progress,
} from '@/design-system/components';
import { PageSkeleton } from '@/components/Skeleton';
import { TaskDetailDrawer } from '@/components/TaskDetailDrawer';
import { useAppState } from '@/lib/state/AppStateProvider';
import { useSync } from '@/lib/sync/SyncProvider';
import { cn } from '@/design-system/utils';

const motivationalMessages = [
  'Great work! Keep up the momentum! 💪',
  'You\'re making progress! 🌟',
  'Stay focused and achieve greatness! 🚀',
  'Every task completed is a step forward! ✨',
  'Believe in yourself! You\'ve got this! 🎯',
  'Small steps lead to big wins! 🏆',
];

export default function DashboardPage() {
  const { hydrated, state, createTask } = useAppState();
  const { status: syncStatus, lastSyncedAt } = useSync();

  const [quickAddTitle, setQuickAddTitle] = React.useState('');
  const [selectedTaskId, setSelectedTaskId] = React.useState<string | null>(null);
  const [drawerOpen, setDrawerOpen] = React.useState(false);

  const motivationalMessage = React.useMemo(
    () => motivationalMessages[Math.floor(Math.random() * motivationalMessages.length)],
    []
  );

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const tasks = Object.values(state.tasks);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  const todayTasks = tasks.filter((t) => {
    if (!t.dueDate) return false;
    const dueDate = new Date(t.dueDate);
    dueDate.setHours(0, 0, 0, 0);
    return dueDate.getTime() === today.getTime();
  });

  const overdueTasks = tasks.filter((t) => {
    if (!t.dueDate || t.isCompleted) return false;
    const dueDate = new Date(t.dueDate);
    return dueDate < today;
  });

  const upcomingTasks = tasks.filter((t) => {
    if (!t.dueDate || t.isCompleted) return false;
    const dueDate = new Date(t.dueDate);
    return dueDate >= tomorrow && dueDate <= new Date(tomorrow.getTime() + 7 * 24 * 60 * 60 * 1000);
  });

  const completedTasks = tasks.filter((t) => t.isCompleted);
  const completionRate = tasks.length > 0 ? (completedTasks.length / tasks.length) * 100 : 0;

  const handleQuickAdd = (e: React.FormEvent) => {
    e.preventDefault();
    if (!quickAddTitle.trim()) return;
    createTask({ title: quickAddTitle.trim() });
    setQuickAddTitle('');
  };

  const handleTaskClick = (taskId: string) => {
    setSelectedTaskId(taskId);
    setDrawerOpen(true);
  };

  return (
    <>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <p className="text-sm text-foreground-tertiary">
            {new Date().toLocaleDateString('en-US', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>

        <Card variant="elevated" className="bg-gradient-to-r from-primary-50 to-primary-100">
          <CardContent className="flex items-center gap-3 py-4">
            <TrendingUp className="h-6 w-6 text-primary-600" />
            <p className="text-base font-medium text-primary-900">{motivationalMessage}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Add Task</CardTitle>
            <CardDescription>Quickly add a new task to your list</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleQuickAdd} className="flex gap-2">
              <Input
                value={quickAddTitle}
                onChange={(e) => setQuickAddTitle(e.target.value)}
                placeholder="What needs to be done?"
                className="flex-1"
              />
              <Button type="submit" className="flex items-center gap-2">
                <Plus className="h-4 w-4" />
                Add
              </Button>
            </form>
          </CardContent>
        </Card>

        <Grid cols={1} gap={4} className="md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="py-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-foreground-tertiary">Total Tasks</p>
                  <p className="text-3xl font-bold">{tasks.length}</p>
                </div>
                <CheckCircle2 className="h-8 w-8 text-primary-600" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="py-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-foreground-tertiary">Completed</p>
                  <p className="text-3xl font-bold">{completedTasks.length}</p>
                </div>
                <CheckCircle2 className="h-8 w-8 text-success-600" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="py-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-foreground-tertiary">Due Today</p>
                  <p className="text-3xl font-bold">{todayTasks.length}</p>
                </div>
                <Clock className="h-8 w-8 text-warning-600" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="py-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-foreground-tertiary">Overdue</p>
                  <p className="text-3xl font-bold">{overdueTasks.length}</p>
                </div>
                <AlertCircle className="h-8 w-8 text-danger-600" />
              </div>
            </CardContent>
          </Card>
        </Grid>

        <Card>
          <CardHeader>
            <CardTitle>Completion Rate</CardTitle>
            <CardDescription>Your overall task completion progress</CardDescription>
          </CardHeader>
          <CardContent>
            <Progress value={completionRate} max={100} showLabel size="lg" />
            <p className="mt-2 text-sm text-foreground-tertiary">
              {completedTasks.length} of {tasks.length} tasks completed
            </p>
          </CardContent>
        </Card>

        <Grid cols={1} gap={4} className="lg:grid-cols-2">
          {overdueTasks.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <AlertCircle className="h-5 w-5 text-danger-600" />
                  Overdue Tasks
                </CardTitle>
                <CardDescription>These tasks need your attention</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {overdueTasks.slice(0, 5).map((task) => (
                    <button
                      key={task.localId}
                      onClick={() => handleTaskClick(task.localId)}
                      className="w-full rounded-lg border border-border-primary bg-background-primary p-3 text-left transition-colors hover:bg-background-secondary"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0 flex-1">
                          <p className="truncate font-medium">{task.title}</p>
                          {task.dueDate && (
                            <p className="text-xs text-danger-600">
                              Due: {new Date(task.dueDate).toLocaleDateString()}
                            </p>
                          )}
                        </div>
                        {task.priority > 0 && (
                          <Badge
                            variant={
                              task.priority === 4
                                ? 'danger'
                                : task.priority === 3
                                ? 'warning'
                                : 'default'
                            }
                            size="sm"
                          >
                            P{task.priority}
                          </Badge>
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader>
              <CardTitle>Today's Tasks</CardTitle>
              <CardDescription>Tasks due today</CardDescription>
            </CardHeader>
            <CardContent>
              {todayTasks.length === 0 ? (
                <p className="text-sm text-foreground-tertiary">No tasks due today</p>
              ) : (
                <div className="space-y-2">
                  {todayTasks.slice(0, 5).map((task) => {
                    const category = task.categoryLocalId
                      ? state.categories[task.categoryLocalId]
                      : null;
                    return (
                      <button
                        key={task.localId}
                        onClick={() => handleTaskClick(task.localId)}
                        className={cn(
                          'w-full rounded-lg border border-border-primary bg-background-primary p-3 text-left transition-colors hover:bg-background-secondary',
                          task.isCompleted && 'opacity-60'
                        )}
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div className="min-w-0 flex-1">
                            <p className={cn('truncate font-medium', task.isCompleted && 'line-through')}>
                              {task.title}
                            </p>
                            {category && (
                              <div className="mt-1 flex items-center gap-1">
                                <span
                                  className="inline-block h-2 w-2 rounded-full"
                                  style={{ backgroundColor: category.color }}
                                />
                                <span className="text-xs text-foreground-tertiary">
                                  {category.name}
                                </span>
                              </div>
                            )}
                          </div>
                          {task.isCompleted && (
                            <Badge variant="success" size="sm">
                              Done
                            </Badge>
                          )}
                        </div>
                      </button>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Upcoming This Week</CardTitle>
              <CardDescription>Tasks due in the next 7 days</CardDescription>
            </CardHeader>
            <CardContent>
              {upcomingTasks.length === 0 ? (
                <p className="text-sm text-foreground-tertiary">No upcoming tasks</p>
              ) : (
                <div className="space-y-2">
                  {upcomingTasks.slice(0, 5).map((task) => (
                    <button
                      key={task.localId}
                      onClick={() => handleTaskClick(task.localId)}
                      className="w-full rounded-lg border border-border-primary bg-background-primary p-3 text-left transition-colors hover:bg-background-secondary"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0 flex-1">
                          <p className="truncate font-medium">{task.title}</p>
                          {task.dueDate && (
                            <p className="text-xs text-foreground-tertiary">
                              Due: {new Date(task.dueDate).toLocaleDateString()}
                            </p>
                          )}
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Sync Status</CardTitle>
              <CardDescription>Offline-first data synchronization</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-foreground-tertiary">Status:</span>
                  <Badge
                    variant={syncStatus === 'syncing' ? 'warning' : syncStatus === 'synced' ? 'success' : 'default'}
                  >
                    {syncStatus}
                  </Badge>
                </div>
                {lastSyncedAt && (
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-foreground-tertiary">Last sync:</span>
                    <span className="text-sm">{new Date(lastSyncedAt).toLocaleString()}</span>
                  </div>
                )}
                <div className="flex items-center justify-between">
                  <span className="text-sm text-foreground-tertiary">Pending changes:</span>
                  <span className="text-sm font-medium">{state.outbox.length}</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </Grid>
      </div>

      <TaskDetailDrawer
        taskId={selectedTaskId}
        open={drawerOpen}
        onClose={() => {
          setDrawerOpen(false);
          setSelectedTaskId(null);
        }}
      />
    </>
  );
}
