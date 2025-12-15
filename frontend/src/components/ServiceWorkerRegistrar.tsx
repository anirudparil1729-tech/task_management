'use client';

import * as React from 'react';

export function ServiceWorkerRegistrar() {
  React.useEffect(() => {
    if (!('serviceWorker' in navigator)) {
      return;
    }

    void navigator.serviceWorker.register('/sw.js');
  }, []);

  return null;
}
