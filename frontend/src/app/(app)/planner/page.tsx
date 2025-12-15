'use client';

import * as React from 'react';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, Plus, Clock } from 'lucide-react';

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Select,
  type SelectOption,
} from '@/design-system/components';
import { PageSkeleton } from '@/components/Skeleton';
import { TaskDetailDrawer } from '@/components/TaskDetailDrawer';
import { useAppState } from '@/lib/state/AppStateProvider';
import { cn } from '@/design-system/utils';

type ViewMode = 'daily' | 'weekly';

export default function PlannerPage() {
  const {
    hydrated,
    state,
    createTimeBlock,
    updateTimeBlock,
    deleteTimeBlock,
    createTask,
  } = useAppState();

  const [viewMode, setViewMode] = React.useState<ViewMode>('daily');
  const [currentDate, setCurrentDate] = React.useState(new Date());
  const [selectedTaskId, setSelectedTaskId] = React.useState<string | null>(null);
  const [drawerOpen, setDrawerOpen] = React.useState(false);
  const [showAddBlock, setShowAddBlock] = React.useState(false);

  const [newBlockTitle, setNewBlockTitle] = React.useState('');
  const [newBlockStart, setNewBlockStart] = React.useState('09:00');
  const [newBlockEnd, setNewBlockEnd] = React.useState('10:00');
  const [newBlockTaskId, setNewBlockTaskId] = React.useState('');

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const tasks = Object.values(state.tasks);
  const timeBlocks = Object.values(state.timeBlocks);

  const taskOptions: SelectOption[] = [
    { value: '', label: 'No task linked' },
    ...tasks
      .filter((t) => !t.isCompleted)
      .sort((a, b) => a.title.localeCompare(b.title))
      .map((t) => ({ value: t.localId, label: t.title })),
  ];

  const navigatePrev = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'daily') {
      newDate.setDate(newDate.getDate() - 1);
    } else {
      newDate.setDate(newDate.getDate() - 7);
    }
    setCurrentDate(newDate);
  };

  const navigateNext = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'daily') {
      newDate.setDate(newDate.getDate() + 1);
    } else {
      newDate.setDate(newDate.getDate() + 7);
    }
    setCurrentDate(newDate);
  };

  const navigateToday = () => {
    setCurrentDate(new Date());
  };

  const getBlocksForDate = (date: Date) => {
    const dateStr = date.toISOString().split('T')[0];
    return timeBlocks.filter((block) => {
      const blockDate = new Date(block.startTime).toISOString().split('T')[0];
      return blockDate === dateStr;
    }).sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
  };

  const getTasksForDate = (date: Date) => {
    const dateStr = date.toISOString().split('T')[0];
    return tasks.filter((task) => {
      if (!task.dueDate) return false;
      const taskDate = new Date(task.dueDate).toISOString().split('T')[0];
      return taskDate === dateStr;
    });
  };

  const handleAddBlock = () => {
    const startDateTime = new Date(currentDate);
    const [startHour, startMinute] = newBlockStart.split(':').map(Number);
    startDateTime.setHours(startHour, startMinute, 0, 0);

    const endDateTime = new Date(currentDate);
    const [endHour, endMinute] = newBlockEnd.split(':').map(Number);
    endDateTime.setHours(endHour, endMinute, 0, 0);

    createTimeBlock({
      title: newBlockTitle.trim() || 'Time Block',
      startTime: startDateTime.toISOString(),
      endTime: endDateTime.toISOString(),
      taskLocalId: newBlockTaskId || null,
      description: null,
    });

    setNewBlockTitle('');
    setNewBlockStart('09:00');
    setNewBlockEnd('10:00');
    setNewBlockTaskId('');
    setShowAddBlock(false);
  };

  const formatTime = (dateStr: string) => {
    return new Date(dateStr).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getDuration = (start: string, end: string) => {
    const diff = new Date(end).getTime() - new Date(start).getTime();
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    return `${hours}h ${minutes}m`;
  };

  const handleTaskClick = (taskId: string) => {
    setSelectedTaskId(taskId);
    setDrawerOpen(true);
  };

  const renderDailyView = () => {
    const blocks = getBlocksForDate(currentDate);
    const tasksForDay = getTasksForDate(currentDate);

    return (
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Time Blocks</CardTitle>
            <CardDescription>
              {currentDate.toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {blocks.length === 0 ? (
              <p className="text-sm text-foreground-tertiary">No time blocks scheduled</p>
            ) : (
              <div className="space-y-3">
                {blocks.map((block) => {
                  const task = block.taskLocalId ? state.tasks[block.taskLocalId] : null;
                  const category = task?.categoryLocalId
                    ? state.categories[task.categoryLocalId]
                    : null;

                  return (
                    <Card key={block.localId} padding="sm">
                      <CardContent className="flex items-start justify-between gap-3">
                        <div
                          className="h-full w-1 rounded-full"
                          style={{
                            backgroundColor: category?.color || '#3B82F6',
                          }}
                        />
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <Clock className="h-4 w-4 text-foreground-tertiary" />
                            <span className="text-sm font-medium">
                              {formatTime(block.startTime)} - {formatTime(block.endTime)}
                            </span>
                            <span className="text-xs text-foreground-tertiary">
                              ({getDuration(block.startTime, block.endTime)})
                            </span>
                          </div>
                          <p className="mt-1 font-medium">{block.title}</p>
                          {task && (
                            <button
                              onClick={() => handleTaskClick(task.localId)}
                              className="mt-1 text-sm text-primary-600 hover:underline"
                            >
                              → {task.title}
                            </button>
                          )}
                          {block.description && (
                            <p className="mt-1 text-sm text-foreground-tertiary">
                              {block.description}
                            </p>
                          )}
                        </div>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => deleteTimeBlock(block.localId)}
                        >
                          Delete
                        </Button>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            )}

            <div className="mt-4">
              {showAddBlock ? (
                <Card className="border-2 border-dashed">
                  <CardContent className="space-y-3 py-4">
                    <Input
                      label="Title"
                      value={newBlockTitle}
                      onChange={(e) => setNewBlockTitle(e.target.value)}
                      placeholder="e.g., Team Meeting, Focus Time"
                    />
                    <div className="grid grid-cols-2 gap-3">
                      <Input
                        label="Start Time"
                        type="time"
                        value={newBlockStart}
                        onChange={(e) => setNewBlockStart(e.target.value)}
                      />
                      <Input
                        label="End Time"
                        type="time"
                        value={newBlockEnd}
                        onChange={(e) => setNewBlockEnd(e.target.value)}
                      />
                    </div>
                    <Select
                      label="Link to Task (Optional)"
                      value={newBlockTaskId}
                      onChange={(e) => setNewBlockTaskId(e.target.value)}
                      options={taskOptions}
                    />
                    <div className="flex gap-2">
                      <Button onClick={handleAddBlock}>Add Block</Button>
                      <Button variant="outline" onClick={() => setShowAddBlock(false)}>
                        Cancel
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ) : (
                <Button
                  variant="outline"
                  onClick={() => setShowAddBlock(true)}
                  className="w-full"
                >
                  <Plus className="mr-2 h-4 w-4" />
                  Add Time Block
                </Button>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Tasks Due Today</CardTitle>
            <CardDescription>Tasks scheduled for this day</CardDescription>
          </CardHeader>
          <CardContent>
            {tasksForDay.length === 0 ? (
              <p className="text-sm text-foreground-tertiary">No tasks due today</p>
            ) : (
              <div className="space-y-2">
                {tasksForDay.map((task) => {
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
                          <p
                            className={cn(
                              'truncate font-medium',
                              task.isCompleted && 'line-through'
                            )}
                          >
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
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    );
  };

  const renderWeeklyView = () => {
    const startOfWeek = new Date(currentDate);
    startOfWeek.setDate(currentDate.getDate() - currentDate.getDay());

    const weekDays = Array.from({ length: 7 }, (_, i) => {
      const day = new Date(startOfWeek);
      day.setDate(startOfWeek.getDate() + i);
      return day;
    });

    return (
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {weekDays.map((day) => {
          const blocks = getBlocksForDate(day);
          const tasksForDay = getTasksForDate(day);
          const isToday =
            day.toISOString().split('T')[0] === new Date().toISOString().split('T')[0];

          return (
            <Card
              key={day.toISOString()}
              className={cn(isToday && 'ring-2 ring-primary-500')}
            >
              <CardHeader>
                <CardTitle className="text-base">
                  {day.toLocaleDateString('en-US', { weekday: 'short' })}
                </CardTitle>
                <CardDescription>
                  {day.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {blocks.length > 0 && (
                    <div>
                      <p className="mb-1 text-xs font-medium text-foreground-tertiary">
                        Time Blocks ({blocks.length})
                      </p>
                      <div className="space-y-1">
                        {blocks.slice(0, 3).map((block) => (
                          <div
                            key={block.localId}
                            className="rounded border border-border-primary bg-background-secondary p-2 text-xs"
                          >
                            <p className="font-medium">{block.title}</p>
                            <p className="text-foreground-tertiary">
                              {formatTime(block.startTime)}
                            </p>
                          </div>
                        ))}
                        {blocks.length > 3 && (
                          <p className="text-xs text-foreground-tertiary">
                            +{blocks.length - 3} more
                          </p>
                        )}
                      </div>
                    </div>
                  )}
                  {tasksForDay.length > 0 && (
                    <div>
                      <p className="mb-1 text-xs font-medium text-foreground-tertiary">
                        Tasks ({tasksForDay.length})
                      </p>
                      <div className="space-y-1">
                        {tasksForDay.slice(0, 3).map((task) => (
                          <button
                            key={task.localId}
                            onClick={() => handleTaskClick(task.localId)}
                            className={cn(
                              'w-full rounded border border-border-primary bg-background-secondary p-2 text-left text-xs transition-colors hover:bg-background-primary',
                              task.isCompleted && 'opacity-60'
                            )}
                          >
                            <p
                              className={cn(
                                'truncate font-medium',
                                task.isCompleted && 'line-through'
                              )}
                            >
                              {task.title}
                            </p>
                          </button>
                        ))}
                        {tasksForDay.length > 3 && (
                          <p className="text-xs text-foreground-tertiary">
                            +{tasksForDay.length - 3} more
                          </p>
                        )}
                      </div>
                    </div>
                  )}
                  {blocks.length === 0 && tasksForDay.length === 0 && (
                    <p className="text-xs text-foreground-tertiary">No events</p>
                  )}
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>
    );
  };

  return (
    <>
      <div className="space-y-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-semibold">Planner</h1>
            <p className="text-sm text-foreground-tertiary">
              Organize your time with blocks and tasks
            </p>
          </div>

          <div className="flex flex-wrap gap-2">
            <Button
              variant={viewMode === 'daily' ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setViewMode('daily')}
            >
              Daily
            </Button>
            <Button
              variant={viewMode === 'weekly' ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setViewMode('weekly')}
            >
              Weekly
            </Button>
          </div>
        </div>

        <Card>
          <CardContent className="flex items-center justify-between py-4">
            <Button variant="outline" size="sm" onClick={navigatePrev}>
              <ChevronLeft className="h-4 w-4" />
            </Button>

            <div className="flex items-center gap-2">
              <CalendarIcon className="h-5 w-5 text-foreground-tertiary" />
              <span className="font-medium">
                {viewMode === 'daily'
                  ? currentDate.toLocaleDateString('en-US', {
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric',
                    })
                  : `Week of ${new Date(
                      currentDate.getTime() - currentDate.getDay() * 24 * 60 * 60 * 1000
                    ).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                    })}`}
              </span>
            </div>

            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={navigateToday}>
                Today
              </Button>
              <Button variant="outline" size="sm" onClick={navigateNext}>
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </CardContent>
        </Card>

        {viewMode === 'daily' ? renderDailyView() : renderWeeklyView()}
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
