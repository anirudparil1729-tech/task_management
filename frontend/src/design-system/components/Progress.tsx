import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '../utils';

const progressVariants = cva('h-full transition-all duration-300 ease-in-out', {
  variants: {
    variant: {
      default: 'bg-primary-600',
      success: 'bg-success-600',
      warning: 'bg-warning-600',
      danger: 'bg-danger-600',
    },
  },
  defaultVariants: {
    variant: 'default',
  },
});

export interface ProgressProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof progressVariants> {
  value: number;
  max?: number;
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
}

const sizeClasses = {
  sm: 'h-1',
  md: 'h-2',
  lg: 'h-3',
};

const Progress = React.forwardRef<HTMLDivElement, ProgressProps>(
  ({ className, variant, value, max = 100, size = 'md', showLabel = false, ...props }, ref) => {
    const percentage = Math.min(100, Math.max(0, (value / max) * 100));

    return (
      <div className="w-full space-y-1">
        <div
          ref={ref}
          role="progressbar"
          aria-valuenow={value}
          aria-valuemin={0}
          aria-valuemax={max}
          className={cn(
            'w-full overflow-hidden rounded-full bg-background-secondary',
            sizeClasses[size],
            className
          )}
          {...props}
        >
          <div
            className={cn(progressVariants({ variant }))}
            style={{ width: `${percentage}%` }}
          />
        </div>
        {showLabel && (
          <p className="text-xs text-foreground-tertiary">
            {Math.round(percentage)}%
          </p>
        )}
      </div>
    );
  }
);

Progress.displayName = 'Progress';

export { Progress, progressVariants };
