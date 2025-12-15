import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { Check } from 'lucide-react';
import { cn } from '../utils';

const checkboxVariants = cva(
  'peer h-5 w-5 shrink-0 rounded border border-border-primary transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
  {
    variants: {
      variant: {
        default: 'data-[state=checked]:bg-primary-600 data-[state=checked]:border-primary-600',
        success: 'data-[state=checked]:bg-success-600 data-[state=checked]:border-success-600',
        danger: 'data-[state=checked]:bg-danger-600 data-[state=checked]:border-danger-600',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  }
);

export interface CheckboxProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'>,
    VariantProps<typeof checkboxVariants> {
  label?: string;
  description?: string;
}

const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  ({ className, variant, label, description, checked, ...props }, ref) => {
    const checkboxId = React.useId();

    return (
      <div className="flex items-start gap-3">
        <div className="relative flex items-center">
          <input
            id={checkboxId}
            ref={ref}
            type="checkbox"
            checked={checked}
            data-state={checked ? 'checked' : 'unchecked'}
            className={cn(checkboxVariants({ variant }), className)}
            {...props}
          />
          {checked && (
            <Check className="pointer-events-none absolute left-1/2 top-1/2 h-3 w-3 -translate-x-1/2 -translate-y-1/2 text-white" />
          )}
        </div>
        {(label || description) && (
          <div className="flex-1 space-y-0.5">
            {label && (
              <label
                htmlFor={checkboxId}
                className="cursor-pointer text-sm font-medium text-foreground-primary"
              >
                {label}
              </label>
            )}
            {description && <p className="text-xs text-foreground-tertiary">{description}</p>}
          </div>
        )}
      </div>
    );
  }
);

Checkbox.displayName = 'Checkbox';

export { Checkbox, checkboxVariants };
