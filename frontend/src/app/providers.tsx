'use client';

import * as React from 'react';

import { ThemeProvider } from '@/lib/theme/ThemeProvider';
import { AuthProvider } from '@/lib/auth/AuthProvider';
import { AppStateProvider } from '@/lib/state/AppStateProvider';
import { SyncProvider } from '@/lib/sync/SyncProvider';
import { NotificationsProvider } from '@/lib/notifications/NotificationsProvider';

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <ThemeProvider>
      <AuthProvider>
        <AppStateProvider>
          <NotificationsProvider>
            <SyncProvider>{children}</SyncProvider>
          </NotificationsProvider>
        </AppStateProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}
