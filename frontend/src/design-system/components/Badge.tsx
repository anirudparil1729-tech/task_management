import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { motion, type HTMLMotionProps } from 'framer-motion';
import { cn } from '../utils';

const badgeVariants = cva(
  'inline-flex items-center gap-1 rounded-full border font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary-100 text-primary-700 hover:bg-primary-200',
        secondary: 'border-transparent bg-secondary-100 text-secondary-700 hover:bg-secondary-200',
        success: 'border-transparent bg-success-100 text-success-700 hover:bg-success-200',
        warning: 'border-transparent bg-warning-100 text-warning-700 hover:bg-warning-200',
        danger: 'border-transparent bg-danger-100 text-danger-700 hover:bg-danger-200',
        outline: 'border-border-primary text-foreground-primary',
        work: 'border-transparent bg-primary-100 text-primary-700',
        personal: 'border-transparent bg-success-100 text-success-700',
        health: 'border-transparent bg-danger-100 text-danger-700',
        finance: 'border-transparent bg-warning-100 text-warning-700',
        social: 'border-transparent bg-secondary-100 text-secondary-700',
        education: 'border-transparent bg-blue-100 text-blue-700',
        entertainment: 'border-transparent bg-pink-100 text-pink-700',
        other: 'border-transparent bg-neutral-100 text-neutral-700',
      },
      size: {
        sm: 'px-2 py-0.5 text-xs',
        md: 'px-2.5 py-0.5 text-sm',
        lg: 'px-3 py-1 text-base',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>, VariantProps<typeof badgeVariants> {
  animated?: boolean;
  onRemove?: () => void;
}

const Badge = React.forwardRef<HTMLDivElement, BadgeProps>(
  ({ className, variant, size, animated = false, onRemove, children, ...props }, ref) => {
    const content = (
      <>
        {children}
        {onRemove && (
          <button
            type="button"
            onClick={onRemove}
            className="ml-1 rounded-full hover:bg-black/10 focus:outline-none focus:ring-2 focus:ring-offset-2"
            aria-label="Remove"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
              className="h-3 w-3"
            >
              <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
            </svg>
          </button>
        )}
      </>
    );

    if (animated) {
      return (
        <motion.div
          ref={ref}
          className={cn(badgeVariants({ variant, size, className }))}
          initial={{ scale: 0.8, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.8, opacity: 0 }}
          transition={{ duration: 0.2 }}
          {...(props as HTMLMotionProps<'div'>)}
        >
          {content}
        </motion.div>
      );
    }

    return (
      <div ref={ref} className={cn(badgeVariants({ variant, size, className }))} {...props}>
        {content}
      </div>
    );
  }
);

Badge.displayName = 'Badge';

export { Badge, badgeVariants };
