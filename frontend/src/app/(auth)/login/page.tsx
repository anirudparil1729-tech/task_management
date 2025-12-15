'use client';

import * as React from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/design-system/components';
import { useAuth } from '@/lib/auth/AuthProvider';

export default function LoginPage() {
  const { status, login } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();

  const [password, setPassword] = React.useState('');
  const [error, setError] = React.useState<string | null>(null);
  const [submitting, setSubmitting] = React.useState(false);

  const next = searchParams.get('next') || '/dashboard';

  React.useEffect(() => {
    if (status === 'authenticated') {
      router.replace(next);
    }
  }, [next, router, status]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      const result = await login(password);
      if (!result.ok) {
        setError(result.error);
        setSubmitting(false);
        return;
      }

      router.replace(next);
    } catch {
      setError('Login failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-[80vh] w-full max-w-md items-center">
      <Card className="w-full" padding="lg">
        <CardHeader>
          <CardTitle>Sign in</CardTitle>
          <CardDescription>Enter the app password to continue.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={error || undefined}
              autoFocus
            />

            <Button type="submit" className="w-full" disabled={submitting}>
              {submitting ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
