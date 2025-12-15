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
import { useAppState } from '@/lib/state/AppStateProvider';

export default function CategoriesPage() {
  const { hydrated, state, createCategory, deleteCategory } = useAppState();

  const [name, setName] = React.useState('');
  const [color, setColor] = React.useState('#3B82F6');

  if (!hydrated) {
    return <PageSkeleton />;
  }

  const categories = Object.values(state.categories).sort((a, b) => a.name.localeCompare(b.name));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Categories</h1>
        <p className="text-sm text-foreground-tertiary">Create and manage categories offline.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Add category</CardTitle>
          <CardDescription>This creates a local change and syncs when online.</CardDescription>
        </CardHeader>
        <CardContent>
          <form
            className="flex flex-col gap-3 md:flex-row md:items-end"
            onSubmit={(e) => {
              e.preventDefault();
              if (!name.trim()) {
                return;
              }
              createCategory({ name: name.trim(), color, icon: null });
              setName('');
            }}
          >
            <div className="flex-1">
              <Input label="Name" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="w-full md:w-40">
              <Input label="Color" value={color} onChange={(e) => setColor(e.target.value)} />
            </div>
            <Button type="submit">Add</Button>
          </form>
        </CardContent>
      </Card>

      <Grid cols={1} gap={4} className="md:grid-cols-2">
        {categories.length === 0 ? (
          <Card>
            <CardHeader>
              <CardTitle>No categories yet</CardTitle>
              <CardDescription>Create one above to get started.</CardDescription>
            </CardHeader>
          </Card>
        ) : (
          categories.map((c) => (
            <Card key={c.localId}>
              <CardHeader>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <span
                        className="inline-block h-3 w-3 rounded-full"
                        style={{ backgroundColor: c.color }}
                      />
                      {c.name}
                    </CardTitle>
                    <CardDescription>
                      {c.serverId ? `Server ID: ${c.serverId}` : 'Local-only'}
                    </CardDescription>
                  </div>

                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => deleteCategory(c.localId)}
                    disabled={c.isDefault}
                  >
                    Delete
                  </Button>
                </div>
              </CardHeader>
            </Card>
          ))
        )}
      </Grid>
    </div>
  );
}
