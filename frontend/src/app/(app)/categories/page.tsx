'use client';

import * as React from 'react';
import { Trash2, Edit2, Plus } from 'lucide-react';

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
import { IconPicker, getCategoryIcon } from '@/components/IconPicker';
import { useAppState } from '@/lib/state/AppStateProvider';

const colorPresets = [
  { name: 'Blue', value: '#3B82F6' },
  { name: 'Green', value: '#10B981' },
  { name: 'Red', value: '#EF4444' },
  { name: 'Yellow', value: '#F59E0B' },
  { name: 'Purple', value: '#8B5CF6' },
  { name: 'Pink', value: '#EC4899' },
  { name: 'Cyan', value: '#06B6D4' },
  { name: 'Orange', value: '#F97316' },
  { name: 'Teal', value: '#14B8A6' },
  { name: 'Indigo', value: '#6366F1' },
];

export default function CategoriesPage() {
  const { hydrated, state, createCategory, updateCategory, deleteCategory } = useAppState();

  const [name, setName] = React.useState('');
  const [color, setColor] = React.useState('#3B82F6');
  const [icon, setIcon] = React.useState<string | null>(null);
  const [editingId, setEditingId] = React.useState<string | null>(null);

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const categories = Object.values(state.categories).sort((a, b) => a.name.localeCompare(b.name));
  const tasks = Object.values(state.tasks);

  const getCategoryTaskCount = (categoryId: string) => {
    return tasks.filter((t) => t.categoryLocalId === categoryId && !t.isCompleted).length;
  };

  const getCategoryCompletedCount = (categoryId: string) => {
    return tasks.filter((t) => t.categoryLocalId === categoryId && t.isCompleted).length;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    if (editingId) {
      updateCategory(editingId, { name: name.trim(), color, icon });
      setEditingId(null);
    } else {
      createCategory({ name: name.trim(), color, icon });
    }

    setName('');
    setColor('#3B82F6');
    setIcon(null);
  };

  const handleEdit = (categoryId: string) => {
    const category = state.categories[categoryId];
    if (!category) return;

    setName(category.name);
    setColor(category.color);
    setIcon(category.icon);
    setEditingId(categoryId);
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setName('');
    setColor('#3B82F6');
    setIcon(null);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Categories</h1>
        <p className="text-sm text-foreground-tertiary">
          Organize your tasks with custom categories
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{editingId ? 'Edit Category' : 'Add Category'}</CardTitle>
          <CardDescription>
            {editingId
              ? 'Update the category details below'
              : 'Create a new category with custom colors and icons'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., Work, Personal, Health"
            />

            <div className="space-y-2">
              <label className="block text-sm font-medium text-foreground-primary">Color</label>
              <div className="flex flex-wrap gap-2">
                {colorPresets.map((preset) => (
                  <button
                    key={preset.value}
                    type="button"
                    onClick={() => setColor(preset.value)}
                    className="group relative flex flex-col items-center gap-1"
                    aria-label={preset.name}
                  >
                    <div
                      className="h-10 w-10 rounded-full border-2 transition-all"
                      style={{
                        backgroundColor: preset.value,
                        borderColor: color === preset.value ? preset.value : 'transparent',
                        transform: color === preset.value ? 'scale(1.1)' : 'scale(1)',
                      }}
                    />
                    <span className="text-xs text-foreground-tertiary group-hover:text-foreground-primary">
                      {preset.name}
                    </span>
                  </button>
                ))}
              </div>
              <Input
                type="color"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                label="Custom Color"
                className="h-12 w-full"
              />
            </div>

            <IconPicker value={icon} onChange={setIcon} label="Icon" />

            <div className="flex gap-2">
              <Button type="submit" className="flex items-center gap-2">
                {editingId ? (
                  <>
                    <Edit2 className="h-4 w-4" />
                    Update
                  </>
                ) : (
                  <>
                    <Plus className="h-4 w-4" />
                    Add
                  </>
                )}
              </Button>
              {editingId && (
                <Button type="button" variant="outline" onClick={handleCancelEdit}>
                  Cancel
                </Button>
              )}
            </div>
          </form>
        </CardContent>
      </Card>

      <div>
        <h2 className="mb-4 text-lg font-semibold">Your Categories</h2>
        {categories.length === 0 ? (
          <Card>
            <CardHeader>
              <CardTitle>No categories yet</CardTitle>
              <CardDescription>Create one above to get started.</CardDescription>
            </CardHeader>
          </Card>
        ) : (
          <Grid cols={1} gap={4} className="md:grid-cols-2 lg:grid-cols-3">
            {categories.map((category) => {
              const Icon = getCategoryIcon(category.icon);
              const activeCount = getCategoryTaskCount(category.localId);
              const completedCount = getCategoryCompletedCount(category.localId);
              const totalCount = activeCount + completedCount;

              return (
                <Card key={category.localId} className="relative overflow-hidden">
                  <div
                    className="absolute left-0 top-0 h-full w-1"
                    style={{ backgroundColor: category.color }}
                  />
                  <CardHeader className="pl-5">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-center gap-3">
                        <div
                          className="flex h-12 w-12 items-center justify-center rounded-full"
                          style={{ backgroundColor: `${category.color}20` }}
                        >
                          {Icon ? (
                            <Icon className="h-6 w-6" style={{ color: category.color }} />
                          ) : (
                            <div
                              className="h-6 w-6 rounded-full"
                              style={{ backgroundColor: category.color }}
                            />
                          )}
                        </div>
                        <div>
                          <CardTitle>{category.name}</CardTitle>
                          <CardDescription>
                            {totalCount} task{totalCount !== 1 ? 's' : ''}
                            {totalCount > 0 &&
                              ` • ${completedCount} completed`}
                          </CardDescription>
                        </div>
                      </div>
                      <div className="flex gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEdit(category.localId)}
                          disabled={category.isDefault}
                        >
                          <Edit2 className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => deleteCategory(category.localId)}
                          disabled={category.isDefault}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pl-5">
                    <div className="space-y-2">
                      {totalCount > 0 && (
                        <div className="flex items-center gap-2">
                          <div className="h-2 flex-1 overflow-hidden rounded-full bg-background-secondary">
                            <div
                              className="h-full transition-all"
                              style={{
                                width: `${(completedCount / totalCount) * 100}%`,
                                backgroundColor: category.color,
                              }}
                            />
                          </div>
                          <span className="text-xs text-foreground-tertiary">
                            {Math.round((completedCount / totalCount) * 100)}%
                          </span>
                        </div>
                      )}
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-foreground-tertiary">Active:</span>
                        <span className="font-medium">{activeCount}</span>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-foreground-tertiary">Completed:</span>
                        <span className="font-medium">{completedCount}</span>
                      </div>
                      {category.serverId && (
                        <p className="text-xs text-foreground-tertiary">
                          Synced • ID: {category.serverId}
                        </p>
                      )}
                      {!category.serverId && (
                        <p className="text-xs text-warning-600">Local only • Not synced</p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </Grid>
        )}
      </div>
    </div>
  );
}
