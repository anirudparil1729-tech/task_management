'use client';

import * as React from 'react';
import {
  Briefcase,
  Home,
  Heart,
  DollarSign,
  Users,
  GraduationCap,
  Tv,
  Package,
  Coffee,
  Dumbbell,
  ShoppingCart,
  Plane,
  Music,
  Camera,
  Book,
  Code,
} from 'lucide-react';
import { cn } from '@/design-system/utils';

const iconOptions = [
  { name: 'briefcase', icon: Briefcase },
  { name: 'home', icon: Home },
  { name: 'heart', icon: Heart },
  { name: 'dollar-sign', icon: DollarSign },
  { name: 'users', icon: Users },
  { name: 'graduation-cap', icon: GraduationCap },
  { name: 'tv', icon: Tv },
  { name: 'package', icon: Package },
  { name: 'coffee', icon: Coffee },
  { name: 'dumbbell', icon: Dumbbell },
  { name: 'shopping-cart', icon: ShoppingCart },
  { name: 'plane', icon: Plane },
  { name: 'music', icon: Music },
  { name: 'camera', icon: Camera },
  { name: 'book', icon: Book },
  { name: 'code', icon: Code },
];

export interface IconPickerProps {
  value: string | null;
  onChange: (iconName: string | null) => void;
  label?: string;
}

export function IconPicker({ value, onChange, label }: IconPickerProps) {
  return (
    <div className="space-y-2">
      {label && <label className="block text-sm font-medium text-foreground-primary">{label}</label>}
      <div className="grid grid-cols-4 gap-2 sm:grid-cols-6 md:grid-cols-8">
        <button
          type="button"
          onClick={() => onChange(null)}
          className={cn(
            'flex h-12 w-12 items-center justify-center rounded-lg border transition-colors hover:bg-background-secondary',
            value === null
              ? 'border-primary-500 bg-primary-50'
              : 'border-border-primary bg-background-primary'
          )}
          aria-label="No icon"
        >
          <span className="text-xs text-foreground-tertiary">None</span>
        </button>
        {iconOptions.map((option) => {
          const Icon = option.icon;
          return (
            <button
              key={option.name}
              type="button"
              onClick={() => onChange(option.name)}
              className={cn(
                'flex h-12 w-12 items-center justify-center rounded-lg border transition-colors hover:bg-background-secondary',
                value === option.name
                  ? 'border-primary-500 bg-primary-50'
                  : 'border-border-primary bg-background-primary'
              )}
              aria-label={option.name}
            >
              <Icon className="h-5 w-5" />
            </button>
          );
        })}
      </div>
    </div>
  );
}

export function getCategoryIcon(iconName: string | null) {
  if (!iconName) return null;
  const option = iconOptions.find((opt) => opt.name === iconName);
  return option?.icon ?? null;
}
