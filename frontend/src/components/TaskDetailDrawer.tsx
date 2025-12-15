'use client';

import * as React from 'react';
import {
  AlarmClock,
  Calendar,
  Flag,
  Repeat,
  Tag,
  Trash2,
  FileText,
  StickyNote,
} from 'lucide-react';

import {
  Button,
  Drawer,
  DrawerContent,
  DrawerFooter,
  Input,
  Textarea,
  Select,
  type SelectOption,
} from '@/design-system/components';
import { useAppState, type TaskEntity } from '@/lib/state/AppStateProvider';

export interface TaskDetailDrawerProps {
  taskId: string | null;
  open: boolean;
  onClose: () => void;
}

const priorityOptions: SelectOption[] = [
  { value: '0', label: 'None' },
  { value: '1', label: 'Low' },
  { value: '2', label: 'Medium' },
  { value: '3', label: 'High' },
  { value: '4', label: 'Urgent' },
];

export function TaskDetailDrawer({ taskId, open, onClose }: TaskDetailDrawerProps) {
  const { state, updateTask, deleteTask, toggleTaskCompleted } = useAppState();
  const task = taskId ? state.tasks[taskId] : null;

  const [title, setTitle] = React.useState('');
  const [description, setDescription] = React.useState('');
  const [notes, setNotes] = React.useState('');
  const [dueDate, setDueDate] = React.useState('');
  const [reminderTime, setReminderTime] = React.useState('');
  const [priority, setPriority] = React.useState('0');
  const [recurrenceRule, setRecurrenceRule] = React.useState('');
  const [categoryLocalId, setCategoryLocalId] = React.useState('');

  React.useEffect(() => {
    if (task) {
      setTitle(task.title);
      setDescription(task.description || '');
      setNotes(task.notes || '');
      setDueDate(task.dueDate ? task.dueDate.split('T')[0] : '');
      setReminderTime(task.reminderTime ? task.reminderTime.slice(0, 16) : '');
      setPriority(String(task.priority));
      setRecurrenceRule(task.recurrenceRule || '');
      setCategoryLocalId(task.categoryLocalId || '');
    }
  }, [task]);

  if (!task) {
    return null;
  }

  const categories = Object.values(state.categories).sort((a, b) =>
    a.name.localeCompare(b.name)
  );
  const categoryOptions: SelectOption[] = [
    { value: '', label: 'No category' },
    ...categories.map((c) => ({ value: c.localId, label: c.name })),
  ];

  const handleSave = () => {
    if (!taskId) return;

    updateTask(taskId, {
      title: title.trim() || task.title,
      description: description.trim() || null,
      notes: notes.trim() || null,
      dueDate: dueDate ? new Date(dueDate).toISOString() : null,
      reminderTime: reminderTime ? new Date(reminderTime).toISOString() : null,
      priority: Number(priority),
      recurrenceRule: recurrenceRule.trim() || null,
      categoryLocalId: categoryLocalId || null,
    });
    onClose();
  };

  const handleDelete = () => {
    if (!taskId) return;
    deleteTask(taskId);
    onClose();
  };

  const handleToggleComplete = () => {
    if (!taskId) return;
    toggleTaskCompleted(taskId);
  };

  const category = task.categoryLocalId ? state.categories[task.categoryLocalId] : null;

  return (
    <Drawer open={open} onClose={onClose} title="Task Details" size="lg">
      <DrawerContent>
        <div className="space-y-6">
          <Input
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Task title"
          />

          <div className="flex items-center gap-2">
            <FileText className="h-5 w-5 text-foreground-tertiary" />
            <Textarea
              label="Description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Add a description..."
              rows={3}
            />
          </div>

          <div className="flex items-center gap-2">
            <StickyNote className="h-5 w-5 text-foreground-tertiary" />
            <Textarea
              label="Notes"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Add notes..."
              rows={3}
            />
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="flex items-center gap-2">
              <Calendar className="h-5 w-5 text-foreground-tertiary" />
              <Input
                label="Due Date"
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
              />
            </div>

            <div className="flex items-center gap-2">
              <AlarmClock className="h-5 w-5 text-foreground-tertiary" />
              <Input
                label="Reminder"
                type="datetime-local"
                value={reminderTime}
                onChange={(e) => setReminderTime(e.target.value)}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="flex items-center gap-2">
              <Flag className="h-5 w-5 text-foreground-tertiary" />
              <Select
                label="Priority"
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
                options={priorityOptions}
              />
            </div>

            <div className="flex items-center gap-2">
              <Tag className="h-5 w-5 text-foreground-tertiary" />
              <Select
                label="Category"
                value={categoryLocalId}
                onChange={(e) => setCategoryLocalId(e.target.value)}
                options={categoryOptions}
              />
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Repeat className="h-5 w-5 text-foreground-tertiary" />
            <Input
              label="Recurrence Rule (e.g., FREQ=DAILY)"
              value={recurrenceRule}
              onChange={(e) => setRecurrenceRule(e.target.value)}
              placeholder="FREQ=DAILY;INTERVAL=1"
            />
          </div>

          <div className="rounded-lg border border-border-primary bg-background-secondary p-4">
            <h3 className="mb-2 text-sm font-medium">Task Info</h3>
            <div className="space-y-1 text-sm text-foreground-tertiary">
              <p>Status: {task.isCompleted ? 'Completed' : 'Active'}</p>
              {task.isCompleted && task.completedAt && (
                <p>Completed: {new Date(task.completedAt).toLocaleString()}</p>
              )}
              <p>Created: {new Date(task.createdAt).toLocaleString()}</p>
              <p>Updated: {new Date(task.updatedAt).toLocaleString()}</p>
              {category && (
                <p className="flex items-center gap-2">
                  Category:
                  <span
                    className="inline-block h-3 w-3 rounded-full"
                    style={{ backgroundColor: category.color }}
                  />
                  {category.name}
                </p>
              )}
            </div>
          </div>
        </div>
      </DrawerContent>

      <DrawerFooter>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Button variant="danger" onClick={handleDelete} className="flex items-center gap-2">
            <Trash2 className="h-4 w-4" />
            Delete
          </Button>
          <div className="flex flex-wrap gap-2">
            <Button
              variant={task.isCompleted ? 'outline' : 'success'}
              onClick={handleToggleComplete}
            >
              {task.isCompleted ? 'Mark Incomplete' : 'Mark Complete'}
            </Button>
            <Button variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleSave}>
              Save Changes
            </Button>
          </div>
        </div>
      </DrawerFooter>
    </Drawer>
  );
}
