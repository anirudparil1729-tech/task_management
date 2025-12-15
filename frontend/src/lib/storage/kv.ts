export interface KVStore {
  get<T>(key: string): Promise<T | null>;
  set<T>(key: string, value: T): Promise<void>;
  remove(key: string): Promise<void>;
}

const DB_NAME = 'app-shell';
const DB_VERSION = 1;
const STORE_NAME = 'kv';

function isBrowser(): boolean {
  return typeof window !== 'undefined';
}

function canUseIndexedDB(): boolean {
  return isBrowser() && typeof indexedDB !== 'undefined';
}

let dbPromise: Promise<IDBDatabase> | null = null;

async function openDb(): Promise<IDBDatabase> {
  if (!canUseIndexedDB()) {
    throw new Error('IndexedDB is not available');
  }

  if (dbPromise) {
    return dbPromise;
  }

  dbPromise = new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME);
      }
    };

    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });

  return dbPromise;
}

function wrapRequest<T>(request: IDBRequest<T>): Promise<T> {
  return new Promise((resolve, reject) => {
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

const indexedDbKV: KVStore = {
  async get<T>(key: string): Promise<T | null> {
    const db = await openDb();
    const tx = db.transaction(STORE_NAME, 'readonly');
    const store = tx.objectStore(STORE_NAME);
    const value = await wrapRequest(store.get(key));
    return (value ?? null) as T | null;
  },

  async set<T>(key: string, value: T): Promise<void> {
    const db = await openDb();
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    await wrapRequest(store.put(value, key));
  },

  async remove(key: string): Promise<void> {
    const db = await openDb();
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    await wrapRequest(store.delete(key));
  },
};

const localStorageKV: KVStore = {
  async get<T>(key: string): Promise<T | null> {
    if (!isBrowser()) {
      return null;
    }

    const raw = window.localStorage.getItem(key);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  },

  async set<T>(key: string, value: T): Promise<void> {
    if (!isBrowser()) {
      return;
    }
    window.localStorage.setItem(key, JSON.stringify(value));
  },

  async remove(key: string): Promise<void> {
    if (!isBrowser()) {
      return;
    }
    window.localStorage.removeItem(key);
  },
};

export function createKVStore(): KVStore {
  if (canUseIndexedDB()) {
    return indexedDbKV;
  }
  return localStorageKV;
}
