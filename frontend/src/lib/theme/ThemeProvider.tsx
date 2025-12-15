'use client';

import * as React from 'react';

export type Theme = 'system' | 'light' | 'dark';

interface ThemeContextValue {
  theme: Theme;
  setTheme: (theme: Theme) => void;
}

const ThemeContext = React.createContext<ThemeContextValue | null>(null);

const THEME_STORAGE_KEY = 'app_theme_v1';

function applyThemeToDom(theme: Theme) {
  if (typeof document === 'undefined') {
    return;
  }

  if (theme === 'system') {
    document.documentElement.removeAttribute('data-theme');
    return;
  }

  document.documentElement.setAttribute('data-theme', theme);
}

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setThemeState] = React.useState<Theme>('system');

  React.useEffect(() => {
    const saved = window.localStorage.getItem(THEME_STORAGE_KEY) as Theme | null;
    if (saved === 'light' || saved === 'dark' || saved === 'system') {
      setThemeState(saved);
      applyThemeToDom(saved);
    } else {
      applyThemeToDom('system');
    }
  }, []);

  const setTheme = React.useCallback((nextTheme: Theme) => {
    setThemeState(nextTheme);
    window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
    applyThemeToDom(nextTheme);
  }, []);

  const value = React.useMemo<ThemeContextValue>(() => ({ theme, setTheme }), [theme, setTheme]);

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme(): ThemeContextValue {
  const ctx = React.useContext(ThemeContext);
  if (!ctx) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return ctx;
}
