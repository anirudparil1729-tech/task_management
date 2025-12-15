'use client';

import * as React from 'react';

import { useAppState } from '@/lib/state/AppStateProvider';

export type NotificationPermissionState = 'default' | 'granted' | 'denied' | 'unsupported';

export interface ShowNotificationArgs {
  title: string;
  body?: string;
  url?: string;
}

interface NotificationsContextValue {
  permission: NotificationPermissionState;
  requestPermission: () => Promise<NotificationPermissionState>;
  showNotification: (args: ShowNotificationArgs) => Promise<void>;
}

const NotificationsContext = React.createContext<NotificationsContextValue | null>(null);

function currentPermission(): NotificationPermissionState {
  if (typeof window === 'undefined') {
    return 'unsupported';
  }

  if (!('Notification' in window)) {
    return 'unsupported';
  }

  return Notification.permission;
}

async function tryShowViaServiceWorker(args: ShowNotificationArgs): Promise<boolean> {
  if (typeof navigator === 'undefined' || !('serviceWorker' in navigator)) {
    return false;
  }

  try {
    const registration = await navigator.serviceWorker.ready;
    if (!registration) {
      return false;
    }

    await registration.showNotification(args.title, {
      body: args.body,
      data: { url: args.url },
    });

    return true;
  } catch {
    return false;
  }
}

export function NotificationsProvider({ children }: { children: React.ReactNode }) {
  const { state, markReminderNotified } = useAppState();
  const [permission, setPermission] = React.useState<NotificationPermissionState>('unsupported');
  const timersRef = React.useRef<Map<string, number>>(new Map());

  React.useEffect(() => {
    setPermission(currentPermission());
  }, []);

  const requestPermission = React.useCallback(async (): Promise<NotificationPermissionState> => {
    if (typeof window === 'undefined' || !('Notification' in window)) {
      setPermission('unsupported');
      return 'unsupported';
    }

    const result = await Notification.requestPermission();
    setPermission(result);
    return result;
  }, []);

  const showNotification = React.useCallback(
    async (args: ShowNotificationArgs) => {
      if (typeof window === 'undefined' || !('Notification' in window)) {
        return;
      }

      if (Notification.permission !== 'granted') {
        return;
      }

      const shown = await tryShowViaServiceWorker(args);
      if (shown) {
        return;
      }

      const notification = new Notification(args.title, { body: args.body, data: { url: args.url } });
      notification.onclick = () => {
        if (args.url) {
          window.location.assign(args.url);
        }
      };
    },
    []
  );

  React.useEffect(() => {
    const now = Date.now();

    for (const reminder of state.reminders) {
      if (reminder.notifiedAt) {
        continue;
      }

      if (timersRef.current.has(reminder.id)) {
        continue;
      }

      const fireAtMs = new Date(reminder.fireAt).getTime();
      if (Number.isNaN(fireAtMs)) {
        continue;
      }

      if (fireAtMs <= now) {
        void showNotification({ title: reminder.title, body: reminder.body ?? undefined, url: '/productivity' });
        markReminderNotified(reminder.id, new Date().toISOString());
        continue;
      }

      const timeoutId = window.setTimeout(() => {
        void showNotification({ title: reminder.title, body: reminder.body ?? undefined, url: '/productivity' });
        markReminderNotified(reminder.id, new Date().toISOString());
        timersRef.current.delete(reminder.id);
      }, fireAtMs - now);

      timersRef.current.set(reminder.id, timeoutId);
    }

    return () => {
      for (const id of timersRef.current.values()) {
        window.clearTimeout(id);
      }
      timersRef.current.clear();
    };
  }, [markReminderNotified, showNotification, state.reminders]);

  const value = React.useMemo<NotificationsContextValue>(
    () => ({ permission, requestPermission, showNotification }),
    [permission, requestPermission, showNotification]
  );

  return <NotificationsContext.Provider value={value}>{children}</NotificationsContext.Provider>;
}

export function useNotifications(): NotificationsContextValue {
  const ctx = React.useContext(NotificationsContext);
  if (!ctx) {
    throw new Error('useNotifications must be used within NotificationsProvider');
  }
  return ctx;
}
