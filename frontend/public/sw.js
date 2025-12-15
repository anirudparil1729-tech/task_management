self.addEventListener('install', (event) => {
  event.waitUntil(self.skipWaiting());
});

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim());
});

self.addEventListener('message', (event) => {
  const data = event.data;
  if (!data || data.type !== 'SHOW_NOTIFICATION') {
    return;
  }

  const title = data.title || 'Reminder';
  const body = data.body;
  const url = data.url;

  event.waitUntil(
    self.registration.showNotification(title, {
      body,
      data: { url },
    })
  );
});

self.addEventListener('notificationclick', (event) => {
  const url = event.notification?.data?.url;
  event.notification.close();

  if (!url) {
    return;
  }

  event.waitUntil(
    (async () => {
      const allClients = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
      for (const client of allClients) {
        if ('focus' in client) {
          await client.focus();
          client.navigate(url);
          return;
        }
      }

      await self.clients.openWindow(url);
    })()
  );
});
