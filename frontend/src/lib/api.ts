const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';

export async function fetchFromAPI(
  endpoint: string,
  options: RequestInit = {},
  apiKey?: string
): Promise<unknown> {
  const headers = new Headers(options.headers);
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (apiKey) {
    headers.set('x-api-key', apiKey);
  }

  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status} ${response.statusText}`);
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type');
  if (contentType?.includes('application/json')) {
    return response.json();
  }

  return response.text();
}

export async function getHello() {
  return fetchFromAPI('/api/hello');
}

export async function getHealth() {
  return fetchFromAPI('/health');
}
