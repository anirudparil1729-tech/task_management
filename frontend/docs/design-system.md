# Design System Documentation

## Overview

This design system provides a comprehensive set of design tokens, components, and guidelines to ensure consistency across the application. It is built with TypeScript, Tailwind CSS, Radix UI primitives, and Framer Motion for smooth animations.

## Table of Contents

1. [Design Tokens](#design-tokens)
2. [Components](#components)
3. [Iconography](#iconography)
4. [Category Colors](#category-colors)
5. [Animations](#animations)
6. [Responsive Design](#responsive-design)
7. [Android Synchronization](#android-synchronization)

---

## Design Tokens

Design tokens are the visual design atoms of the design system. They are defined in `/src/design-system/tokens/index.ts` and include:

### Color Palette

The color system supports both light and dark modes with semantic color scales.

#### Base Colors

- **Primary**: Blue scale (HSL 215) - Used for primary actions and key UI elements
- **Secondary**: Purple scale (HSL 270) - Used for secondary actions and highlights
- **Neutral**: Grayscale - Used for text, backgrounds, and borders

#### Semantic Colors

- **Success**: Green scale (HSL 142) - Positive feedback, success states
- **Warning**: Yellow/Orange scale (HSL 43/37) - Warnings, cautionary states
- **Danger**: Red scale (HSL 0) - Errors, destructive actions

#### Color Scales

Each color has 11 shades (50-950):

- `50-200`: Very light, backgrounds, hover states
- `300-400`: Light accents, disabled states
- `500-600`: Primary brand colors, default states
- `700-800`: Hover states, text on light backgrounds
- `900-950`: Dark text, high contrast elements

#### Usage in CSS

```css
/* Light mode */
.element {
  background: hsl(var(--primary-500));
  color: hsl(var(--foreground-primary));
}

/* Tailwind classes */
<div className="bg-primary-600 text-white">
```

#### Color Mode

The design system automatically switches between light and dark modes based on system preferences using `@media (prefers-color-scheme: dark)`.

### Typography

Typography tokens define the type scale and font families.

#### Font Families

- **Sans**: `Geist Sans` - Primary font for UI and content
- **Mono**: `Geist Mono` - Monospace font for code

#### Font Sizes

| Size | Value    | Line Height | Usage                |
| ---- | -------- | ----------- | -------------------- |
| xs   | 0.75rem  | 1rem        | Fine print, captions |
| sm   | 0.875rem | 1.25rem     | Small text, labels   |
| base | 1rem     | 1.5rem      | Body text            |
| lg   | 1.125rem | 1.75rem     | Emphasized text      |
| xl   | 1.25rem  | 1.875rem    | Small headings       |
| 2xl  | 1.5rem   | 2rem        | Section headings     |
| 3xl  | 1.875rem | 2.25rem     | Page headings        |
| 4xl+ | 2.25rem+ | Various     | Display text         |

#### Font Weights

- **thin** (100): Decorative only
- **light** (300): De-emphasized text
- **normal** (400): Body text
- **medium** (500): Slightly emphasized
- **semibold** (600): Headings, buttons
- **bold** (700): Strong emphasis
- **extrabold** (800-900): Display text

### Spacing

Spacing uses a consistent scale based on 0.25rem (4px) increments:

- **0-3**: Tight spacing (0-0.75rem)
- **4-8**: Standard spacing (1-2rem)
- **10-16**: Loose spacing (2.5-4rem)
- **20+**: Section spacing (5rem+)

### Border Radius

- **sm**: 0.125rem (2px) - Subtle rounding
- **base**: 0.25rem (4px) - Standard buttons, inputs
- **md**: 0.375rem (6px) - Cards, panels
- **lg**: 0.5rem (8px) - Large cards
- **xl**: 0.75rem (12px) - Feature cards
- **2xl**: 1rem (16px) - Prominent elements
- **3xl**: 1.5rem (24px) - Hero sections
- **full**: 9999px - Pills, badges, circular elements

### Elevation (Shadows)

Elevation creates depth and hierarchy:

- **xs**: Subtle depth for hover states
- **sm**: Small cards, dropdowns
- **base**: Standard cards
- **md**: Elevated panels, modals
- **lg**: Floating elements, popovers
- **xl-2xl**: High-level overlays
- **inner**: Inset elements, inputs

### Breakpoints

Responsive breakpoints for different screen sizes:

- **sm**: 640px - Mobile landscape, small tablets
- **md**: 768px - Tablets
- **lg**: 1024px - Small laptops
- **xl**: 1280px - Desktops
- **2xl**: 1536px - Large desktops

---

## Components

### Button

A versatile button component with multiple variants and sizes.

```tsx
import { Button } from '@/design-system';

// Primary button
<Button variant="primary" size="md">
  Click me
</Button>

// With animation (default)
<Button variant="primary" animated>
  Animated
</Button>

// As a link
<Button asChild>
  <Link href="/somewhere">Go</Link>
</Button>
```

**Variants:**

- `primary`: Main call-to-action
- `secondary`: Secondary actions
- `success`: Positive actions
- `warning`: Cautionary actions
- `danger`: Destructive actions
- `outline`: Bordered, transparent background
- `ghost`: No background, hover state only
- `link`: Text button with underline

**Sizes:** `sm`, `md`, `lg`, `xl`, `icon`

**Props:**

- `animated`: Enable hover/tap animations (default: true)
- `asChild`: Render as a child component (useful for links)

### Card

Flexible card component for content containers.

```tsx
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from '@/design-system';

<Card variant="elevated" animated>
  <CardHeader>
    <CardTitle>Card Title</CardTitle>
    <CardDescription>Card description text</CardDescription>
  </CardHeader>
  <CardContent>Main content here</CardContent>
  <CardFooter>
    <Button>Action</Button>
  </CardFooter>
</Card>;
```

**Variants:**

- `default`: Standard card with subtle shadow
- `elevated`: More prominent shadow
- `outlined`: Border only, no shadow
- `filled`: Filled background

**Padding:** `none`, `sm`, `md`, `lg`, `xl`

**Props:**

- `animated`: Enable entry animation and hover lift effect

### Badge

Small label component for tags, categories, and status indicators.

```tsx
import { Badge } from '@/design-system';

<Badge variant="work" size="md">
  Work
</Badge>

<Badge variant="primary" onRemove={() => console.log('removed')}>
  Removable
</Badge>
```

**Variants:**

- Standard: `default`, `secondary`, `success`, `warning`, `danger`, `outline`
- Category: `work`, `personal`, `health`, `finance`, `social`, `education`, `entertainment`, `other`

**Sizes:** `sm`, `md`, `lg`

**Props:**

- `animated`: Enable scale animation
- `onRemove`: Function to call when remove button is clicked (shows × button)

### Icon

Wrapper component for consistent icon sizing and coloring.

```tsx
import { Icon } from '@/design-system';
import { CheckIcon } from 'lucide-react';

<Icon icon={CheckIcon} size="md" color="success" />;
```

**Sizes:** `xs`, `sm`, `md`, `lg`, `xl`, `2xl`

**Colors:** `default`, `primary`, `secondary`, `success`, `warning`, `danger`, `muted`

**Props:**

- `icon`: React component for the icon
- `animated`: Enable scale animation

### Input & Textarea

Form input components with labels, helper text, and error states.

```tsx
import { Input, Textarea } from '@/design-system';

<Input
  id="email"
  type="email"
  label="Email"
  placeholder="you@example.com"
  helperText="We'll never share your email"
/>

<Input
  id="error-example"
  label="Field with error"
  error="This field is required"
/>

<Textarea
  id="description"
  label="Description"
  rows={4}
  placeholder="Enter description..."
/>
```

**Variants:** `default`, `error`, `success`

**Sizes (Input only):** `sm`, `md`, `lg`

**Props:**

- `label`: Label text above input
- `helperText`: Helper text below input
- `error`: Error message (overrides helperText and changes style)

### Layout Primitives

Utility components for common layout patterns.

#### Container

```tsx
import { Container } from '@/design-system';

<Container size="xl" padding="md">
  Content within a responsive container
</Container>;
```

**Sizes:** `sm`, `md`, `lg`, `xl`, `2xl`, `full`
**Padding:** `none`, `sm`, `md`, `lg`

#### Stack

Flexbox-based vertical or horizontal stack.

```tsx
import { Stack } from '@/design-system';

<Stack direction="column" gap={4} align="start">
  <div>Item 1</div>
  <div>Item 2</div>
</Stack>;
```

**Direction:** `row`, `column`, `row-reverse`, `column-reverse`
**Align:** `start`, `center`, `end`, `baseline`, `stretch`
**Justify:** `start`, `center`, `end`, `between`, `around`, `evenly`
**Gap:** `0` to `12`

#### Grid

CSS Grid layout.

```tsx
import { Grid } from '@/design-system';

<Grid cols={3} gap={4}>
  <div>Item 1</div>
  <div>Item 2</div>
  <div>Item 3</div>
</Grid>;
```

**Cols:** `1`, `2`, `3`, `4`, `5`, `6`, `12`
**Gap:** `0` to `12`

#### Box

Generic container with padding and margin utilities.

```tsx
import { Box } from '@/design-system';

<Box padding={4} margin={2}>
  Content
</Box>;
```

---

## Iconography

### Icon Library

The design system uses [Lucide React](https://lucide.dev/) as the primary icon library. Lucide provides a comprehensive set of consistent, open-source icons.

### Icon Guidelines

1. **Consistency**: Always use the `Icon` wrapper component for consistent sizing and styling
2. **Size**: Match icon size to surrounding text (typically `md` for body text)
3. **Color**: Use semantic colors to convey meaning (success, warning, danger)
4. **Spacing**: Maintain consistent spacing around icons (typically `gap-2` in flex layouts)

### Common Icons

```tsx
import { CheckIcon, XIcon, AlertCircleIcon, InfoIcon } from 'lucide-react';
import { Icon } from '@/design-system';

// Success
<Icon icon={CheckIcon} color="success" />

// Error
<Icon icon={XIcon} color="danger" />

// Warning
<Icon icon={AlertCircleIcon} color="warning" />

// Info
<Icon icon={InfoIcon} color="primary" />
```

### Adding Custom Icons

To add custom SVG icons:

1. Create a React component that returns the SVG
2. Ensure the component accepts a `className` prop
3. Use the `Icon` wrapper component

```tsx
const CustomIcon = ({ className }: { className?: string }) => (
  <svg className={className} viewBox="0 0 24 24">
    {/* SVG content */}
  </svg>
);

<Icon icon={CustomIcon} size="md" />;
```

---

## Category Colors

The design system includes predefined colors for different activity categories. These ensure visual consistency and help users quickly identify category types.

### Category Color Mapping

| Category      | Color               | Usage                             |
| ------------- | ------------------- | --------------------------------- |
| Work          | Blue (Primary)      | Professional tasks, meetings      |
| Personal      | Green (Success)     | Personal activities, self-care    |
| Health        | Red (Danger/Health) | Exercise, medical appointments    |
| Finance       | Yellow (Warning)    | Bills, budgeting, financial tasks |
| Social        | Purple (Secondary)  | Social events, gatherings         |
| Education     | Cyan                | Learning, courses, studying       |
| Entertainment | Pink                | Leisure, hobbies, entertainment   |
| Other         | Gray (Neutral)      | Miscellaneous activities          |

### Using Category Colors

```tsx
import { Badge } from '@/design-system';

// Badge with category variant
<Badge variant="work">Work</Badge>
<Badge variant="personal">Personal</Badge>
<Badge variant="health">Health</Badge>

// Custom elements with category colors
<div className="bg-category-work text-white p-4 rounded-lg">
  Work Task
</div>
```

### Category Color CSS Variables

```css
--category-work
--category-personal
--category-health
--category-finance
--category-social
--category-education
--category-entertainment
--category-other
```

---

## Animations

The design system uses [Framer Motion](https://www.framer.com/motion/) for smooth, performant animations.

### Animation Philosophy

1. **Subtle**: Animations should enhance, not distract
2. **Fast**: Keep animations under 300ms for responsiveness
3. **Purposeful**: Every animation should serve a purpose
4. **Accessible**: Respect `prefers-reduced-motion`

### Built-in Animations

#### Button Animations

- **Hover**: Scale up slightly (1.02x)
- **Tap**: Scale down slightly (0.98x)
- **Transition**: Spring physics for natural feel

#### Card Animations

- **Entry**: Fade in with slight upward motion
- **Hover**: Lift up with subtle shadow increase

#### Badge Animations

- **Entry**: Scale in with fade
- **Exit**: Scale out with fade

### Custom Animations

Use Framer Motion directly for custom animations:

```tsx
import { motion } from 'framer-motion';

<motion.div
  initial={{ opacity: 0, y: 20 }}
  animate={{ opacity: 1, y: 0 }}
  transition={{ duration: 0.3 }}
>
  Content
</motion.div>;
```

### Animation Tokens

```typescript
transitions: {
  duration: {
    fast: '150ms',
    base: '250ms',
    slow: '350ms',
    slower: '500ms',
  },
  easing: {
    ease: 'ease',
    linear: 'linear',
    easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
    easeOut: 'cubic-bezier(0, 0, 0.2, 1)',
    easeInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
    spring: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
  },
}
```

---

## Responsive Design

### Mobile-First Approach

The design system follows a mobile-first approach. Start with mobile styles and progressively enhance for larger screens.

```tsx
// Mobile-first classes
<div className="p-4 md:p-6 lg:p-8">
  <h1 className="text-2xl md:text-3xl lg:text-4xl">Responsive Heading</h1>
</div>
```

### Breakpoint Usage

```tsx
// Hide on mobile, show on tablet+
<div className="hidden md:block">Desktop content</div>

// Show on mobile only
<div className="block md:hidden">Mobile content</div>

// Responsive grid
<Grid cols={1} className="md:grid-cols-2 lg:grid-cols-3">
  <Card>Card 1</Card>
  <Card>Card 2</Card>
  <Card>Card 3</Card>
</Grid>
```

### Component Responsiveness

Components automatically adapt to container width. Use responsive utilities for fine-tuned control:

```tsx
<Button size="sm" className="md:hidden">Mobile</Button>
<Button size="md" className="hidden md:inline-flex">Desktop</Button>
```

---

## Android Synchronization

### Token Export for Android

The design tokens are structured to be easily synchronized with Android development. To export tokens for Android:

1. **Colors**: Convert HSL values to hex or Android color resources
2. **Spacing**: Convert rem values to dp (1rem = 16dp)
3. **Typography**: Map font sizes and weights to Android text appearances

### Color Conversion

```typescript
// TypeScript tokens
colors.light.primary[500] = 'hsl(215, 85%, 52%)'

// Android colors.xml
<color name="primary_500">#2563EB</color>
```

### Spacing Conversion

```typescript
// TypeScript tokens
spacing[4] = '1rem' // 16px

// Android dimens.xml
<dimen name="spacing_4">16dp</dimen>
```

### Typography Conversion

```typescript
// TypeScript tokens
fontSize.base = ['1rem', { lineHeight: '1.5rem' }]

// Android styles.xml
<style name="TextAppearance.Base">
  <item name="android:textSize">16sp</item>
  <item name="android:lineHeight">24sp</item>
</style>
```

### Automation Script

Consider creating a script to automatically export tokens:

```typescript
// scripts/export-tokens-android.ts
import { colors, spacing, typography } from '../src/design-system/tokens';

// Convert and export to Android XML format
// ... implementation
```

### Category Colors Sync

Ensure category colors match exactly between platforms:

```xml
<!-- Android colors.xml -->
<color name="category_work">#2563EB</color>
<color name="category_personal">#16A34A</color>
<color name="category_health">#DC2626</color>
<!-- ... more categories -->
```

### Maintaining Consistency

1. **Single Source of Truth**: Keep TypeScript tokens as the source
2. **Automated Export**: Use scripts to generate Android resources
3. **Documentation**: Document any platform-specific adjustments
4. **Regular Sync**: Sync tokens whenever design updates occur
5. **Visual Testing**: Compare components across platforms regularly

---

## Best Practices

### Do's

✅ Use design tokens instead of hardcoded values
✅ Leverage existing components before creating new ones
✅ Follow the established color semantics
✅ Use responsive utilities for mobile-first design
✅ Enable animations for better user experience
✅ Use semantic HTML elements
✅ Provide labels and helper text for form inputs

### Don'ts

❌ Don't override component styles excessively
❌ Don't create custom colors outside the token system
❌ Don't use arbitrary spacing values
❌ Don't forget dark mode considerations
❌ Don't overuse animations
❌ Don't ignore accessibility (color contrast, keyboard navigation)

---

## Migration Guide

### From Existing Components

If you have existing components, migrate them gradually:

1. Import design system components
2. Replace hardcoded values with tokens
3. Update styling to use Tailwind classes from the design system
4. Test in both light and dark modes
5. Verify responsive behavior

### Example Migration

Before:

```tsx
<button
  style={{
    backgroundColor: '#2563EB',
    padding: '10px 20px',
    borderRadius: '8px',
  }}
>
  Click
</button>
```

After:

```tsx
import { Button } from '@/design-system';

<Button variant="primary" size="md">
  Click
</Button>;
```

---

## Contributing

When contributing to the design system:

1. **Propose Changes**: Discuss major changes before implementing
2. **Follow Conventions**: Match existing code style and patterns
3. **Update Documentation**: Document all new tokens and components
4. **Test Thoroughly**: Test in light/dark mode and all breakpoints
5. **Consider Android**: Ensure tokens can be exported to Android

---

## Support

For questions or issues with the design system:

- Check this documentation first
- Review component source code in `/src/design-system/components`
- Check token definitions in `/src/design-system/tokens`
- Create an issue in the project repository

---

## Version History

### v1.0.0 (Current)

- Initial design system release
- Core components: Button, Card, Badge, Icon, Input, Textarea, Layout primitives
- Design tokens: colors, typography, spacing, radii, elevation, breakpoints
- Dark mode support
- Framer Motion animations
- Category color system
- Android synchronization guidelines
