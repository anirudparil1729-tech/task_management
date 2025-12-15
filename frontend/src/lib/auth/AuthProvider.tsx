'use client';

import * as React from 'react';

import { createKVStore } from '@/lib/storage/kv';

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

export interface AuthSession {
  token: string;
  apiKey: string;
  createdAt: string;
}

interface AuthContextValue {
  status: AuthStatus;
  session: AuthSession | null;
  login: (password: string) => Promise<{ ok: true } | { ok: false; error: string }>;
  logout: () => Promise<void>;
}

const AuthContext = React.createContext<AuthContextValue | null>(null);

const AUTH_STORAGE_KEY = 'auth_session_v1';

function expectedPassword(): string {
  return process.env.NEXT_PUBLIC_APP_PASSWORD || 'secret-password';
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const kv = React.useMemo(() => createKVStore(), []);
  const [status, setStatus] = React.useState<AuthStatus>('loading');
  const [session, setSession] = React.useState<AuthSession | null>(null);

  React.useEffect(() => {
    let mounted = true;

    (async () => {
      try {
        const stored = await kv.get<AuthSession>(AUTH_STORAGE_KEY);
        if (!mounted) {
          return;
        }

        if (stored?.token && stored.apiKey) {
          setSession(stored);
          setStatus('authenticated');
          return;
        }

        setSession(null);
        setStatus('unauthenticated');
      } catch {
        setSession(null);
        setStatus('unauthenticated');
      }
    })();

    return () => {
      mounted = false;
    };
  }, [kv]);

  const login = React.useCallback(
    async (password: string) => {
      if (password !== expectedPassword()) {
        return { ok: false as const, error: 'Incorrect password' };
      }

      const nextSession: AuthSession = {
        token: crypto.randomUUID(),
        apiKey: password,
        createdAt: new Date().toISOString(),
      };

      await kv.set(AUTH_STORAGE_KEY, nextSession);
      setSession(nextSession);
      setStatus('authenticated');

      return { ok: true as const };
    },
    [kv]
  );

  const logout = React.useCallback(async () => {
    await kv.remove(AUTH_STORAGE_KEY);
    setSession(null);
    setStatus('unauthenticated');
  }, [kv]);

  const value = React.useMemo<AuthContextValue>(
    () => ({ status, session, login, logout }),
    [status, session, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = React.useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
