import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { motion, type HTMLMotionProps } from 'framer-motion';
import { cn } from '../utils';

const iconVariants = cva('inline-flex items-center justify-center', {
  variants: {
    size: {
      xs: 'h-3 w-3',
      sm: 'h-4 w-4',
      md: 'h-5 w-5',
      lg: 'h-6 w-6',
      xl: 'h-8 w-8',
      '2xl': 'h-10 w-10',
    },
    color: {
      default: 'text-foreground-primary',
      primary: 'text-primary-600',
      secondary: 'text-secondary-600',
      success: 'text-success-600',
      warning: 'text-warning-600',
      danger: 'text-danger-600',
      muted: 'text-foreground-tertiary',
    },
  },
  defaultVariants: {
    size: 'md',
    color: 'default',
  },
});

export interface IconProps
  extends Omit<React.HTMLAttributes<HTMLSpanElement>, 'color'>, VariantProps<typeof iconVariants> {
  icon: React.ComponentType<{ className?: string }>;
  animated?: boolean;
}

const Icon = React.forwardRef<HTMLSpanElement, IconProps>(
  ({ className, size, color, icon: IconComponent, animated = false, ...props }, ref) => {
    if (animated) {
      return (
        <motion.span
          ref={ref}
          className={cn(iconVariants({ size, color, className }))}
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ type: 'spring', stiffness: 260, damping: 20 }}
          {...(props as HTMLMotionProps<'span'>)}
        >
          <IconComponent className="h-full w-full" />
        </motion.span>
      );
    }

    return (
      <span ref={ref} className={cn(iconVariants({ size, color, className }))} {...props}>
        <IconComponent className="h-full w-full" />
      </span>
    );
  }
);

Icon.displayName = 'Icon';

export { Icon, iconVariants };
