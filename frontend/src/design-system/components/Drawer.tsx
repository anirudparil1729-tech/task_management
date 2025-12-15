import * as React from 'react';
import { X } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { cn } from '../utils';

export interface DrawerProps {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  title?: string;
  position?: 'left' | 'right';
  size?: 'sm' | 'md' | 'lg' | 'full';
}

const sizeClasses = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-lg',
  full: 'max-w-full',
};

export function Drawer({ open, onClose, children, title, position = 'right', size = 'md' }: DrawerProps) {
  React.useEffect(() => {
    if (open) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [open]);

  React.useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && open) {
        onClose();
      }
    };
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [open, onClose]);

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-50 bg-black/50"
            onClick={onClose}
          />
          <motion.div
            initial={{ x: position === 'right' ? '100%' : '-100%' }}
            animate={{ x: 0 }}
            exit={{ x: position === 'right' ? '100%' : '-100%' }}
            transition={{ type: 'spring', damping: 30, stiffness: 300 }}
            className={cn(
              'fixed inset-y-0 z-50 w-full bg-background-primary shadow-xl',
              position === 'right' ? 'right-0' : 'left-0',
              sizeClasses[size]
            )}
          >
            <div className="flex h-full flex-col">
              <div className="flex items-center justify-between border-b border-border-primary px-6 py-4">
                {title && <h2 className="text-lg font-semibold text-foreground-primary">{title}</h2>}
                <button
                  onClick={onClose}
                  className="ml-auto rounded-lg p-2 transition-colors hover:bg-background-secondary focus:outline-none focus:ring-2 focus:ring-primary-500"
                  aria-label="Close drawer"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>
              <div className="flex-1 overflow-y-auto">{children}</div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

export interface DrawerContentProps {
  children: React.ReactNode;
  className?: string;
}

export function DrawerContent({ children, className }: DrawerContentProps) {
  return <div className={cn('p-6', className)}>{children}</div>;
}

export interface DrawerFooterProps {
  children: React.ReactNode;
  className?: string;
}

export function DrawerFooter({ children, className }: DrawerFooterProps) {
  return (
    <div className={cn('border-t border-border-primary px-6 py-4', className)}>
      {children}
    </div>
  );
}
