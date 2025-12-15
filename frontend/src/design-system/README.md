# Design System

A comprehensive design system for building consistent, accessible, and beautiful user interfaces.

## Quick Start

```tsx
import { Button, Card, Badge, Input } from '@/design-system';

function MyComponent() {
  return (
    <Card variant="elevated" animated>
      <h2>Welcome</h2>
      <Input label="Email" type="email" />
      <Button variant="primary">Submit</Button>
      <Badge variant="success">New</Badge>
    </Card>
  );
}
```

## Features

- 🎨 **Comprehensive Token System**: Colors, typography, spacing, and more
- 🌗 **Dark Mode**: Automatic dark mode support
- 📱 **Responsive**: Mobile-first, responsive components
- ♿ **Accessible**: Built with accessibility in mind
- 🎭 **Animated**: Smooth animations with Framer Motion
- 🧩 **Composable**: Flexible, composable components
- 🎯 **Type-Safe**: Full TypeScript support
- 🔄 **Cross-Platform**: Token sync with Android

## Documentation

Full documentation is available at `/docs/design-system.md`

## Demo

View all components in action:

```bash
npm run dev
# Navigate to /design-system-demo
```

## Components

- **Button**: Versatile button with 8 variants and 5 sizes
- **Card**: Flexible card component with header, content, and footer
- **Badge**: Small labels for tags and categories
- **Icon**: Consistent icon wrapper
- **Input/Textarea**: Form inputs with labels and validation
- **Layout**: Container, Stack, Grid, and Box primitives

## Design Tokens

All design tokens are centralized in `/tokens/index.ts`:

- Colors (light & dark modes)
- Typography (fonts, sizes, weights)
- Spacing (consistent scale)
- Border radius
- Elevation (shadows)
- Breakpoints
- Transitions
- Z-index

## Usage with Tailwind

The design system integrates seamlessly with Tailwind CSS:

```tsx
<div className="bg-primary-600 text-white p-4 rounded-lg">Using design tokens with Tailwind</div>
```

## Contributing

When adding new components:

1. Follow existing patterns and conventions
2. Use design tokens instead of hardcoded values
3. Support both light and dark modes
4. Add TypeScript types
5. Document the component
6. Add to the demo page

## License

Part of the main project license.
