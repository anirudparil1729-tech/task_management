'use client';

import * as React from 'react';
import { usePathname, useRouter } from 'next/navigation';

import { useAuth } from '@/lib/auth/AuthProvider';
import { PageSkeleton } from '@/components/Skeleton';

export function AuthGate({ children }: { children: React.ReactNode }) {
  const { status } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  React.useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
    }
  }, [pathname, router, status]);

  if (status === 'loading') {
    return (
      <div className="min-h-screen p-6">
        <PageSkeleton />
      </div>
    );
  }

  if (status === 'unauthenticated') {
    return null;
  }

  return children;
}
