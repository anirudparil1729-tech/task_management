# Design System

A comprehensive, production-ready design system for building consistent, accessible, and beautiful UIs.

## 🚀 Quick Links

- **Full Documentation**: [docs/design-system.md](./docs/design-system.md)
- **Component Source**: [src/design-system/](./src/design-system/)
- **Live Demo**: Run `pnpm dev` and visit `/design-system-demo`

## 📦 What's Included

### Design Tokens

Centralized design tokens in TypeScript + CSS variables:

- ✅ Colors (11-shade scales + semantic colors)
- ✅ Light/Dark mode support
- ✅ Typography (font families, sizes, weights)
- ✅ Spacing (consistent 4px-based scale)
- ✅ Border radius
- ✅ Elevation (shadows)
- ✅ Breakpoints (responsive design)
- ✅ Transitions & animations
- ✅ Z-index scale

### Components

Built with Radix UI primitives and Framer Motion animations:

- ✅ **Button** - 8 variants, 5 sizes, animated
- ✅ **Card** - Flexible container with header/content/footer
- ✅ **Badge** - Tags, categories, status indicators
- ✅ **Icon** - Consistent icon wrapper with sizing/coloring
- ✅ **Input/Textarea** - Form inputs with validation states
- ✅ **Layout** - Container, Stack, Grid, Box primitives

### Category Colors

Pre-defined colors for activity categories:

- Work (Blue), Personal (Green), Health (Red), Finance (Yellow)
- Social (Purple), Education (Cyan), Entertainment (Pink), Other (Gray)

## 🎨 Usage Example

```tsx
import {
  Button,
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  Badge,
  Input,
  Stack,
} from '@/design-system';

function MyComponent() {
  return (
    <Card variant="elevated" animated>
      <CardHeader>
        <CardTitle>Welcome</CardTitle>
      </CardHeader>
      <CardContent>
        <Stack gap={4}>
          <Input label="Email" type="email" placeholder="you@example.com" />
          <div className="flex gap-2">
            <Badge variant="work">Work</Badge>
            <Badge variant="personal">Personal</Badge>
          </div>
          <Button variant="primary">Submit</Button>
        </Stack>
      </CardContent>
    </Card>
  );
}
```

## 🎯 Key Features

- **Type-Safe**: Full TypeScript support with proper types
- **Accessible**: Built with accessibility in mind (ARIA, keyboard nav)
- **Responsive**: Mobile-first, responsive by default
- **Themeable**: Light/dark mode with CSS variables
- **Animated**: Smooth animations with Framer Motion
- **Composable**: Flexible, composable component APIs
- **Documented**: Comprehensive documentation and examples
- **Cross-Platform Ready**: Token sync guidelines for Android

## 🛠️ Tech Stack

- **TypeScript** - Type safety
- **Tailwind CSS** - Utility-first styling
- **Radix UI** - Accessible primitives
- **Framer Motion** - Smooth animations
- **class-variance-authority** - Type-safe variant management
- **Lucide React** - Icon library

## 📱 Android Synchronization

The design tokens are structured for easy export to Android:

- HSL colors → Hex/Android color resources
- Rem spacing → DP values (1rem = 16dp)
- Font sizes → Android text appearances

See [docs/design-system.md#android-synchronization](./docs/design-system.md#android-synchronization) for details.

## 📚 Documentation Structure

```
docs/
  design-system.md          # Complete documentation

src/design-system/
  tokens/
    index.ts                # All design tokens
  components/
    Button.tsx              # Button component
    Card.tsx                # Card components
    Badge.tsx               # Badge component
    Icon.tsx                # Icon wrapper
    Input.tsx               # Input/Textarea
    Layout.tsx              # Layout primitives
    index.ts                # Component exports
  utils/
    cn.ts                   # Class name utility
    index.ts                # Utility exports
  index.ts                  # Main exports
  README.md                 # Quick start guide
```

## 🎓 Getting Started

1. **Import components**:

   ```tsx
   import { Button, Card, Badge } from '@/design-system';
   ```

2. **Use design tokens in Tailwind**:

   ```tsx
   <div className="bg-primary-600 text-white p-4 rounded-lg">Using design tokens</div>
   ```

3. **Access tokens in TypeScript**:

   ```tsx
   import { colors, spacing, typography } from '@/design-system/tokens';
   ```

4. **View demo**:
   ```bash
   pnpm dev
   # Navigate to http://localhost:3000/design-system-demo
   ```

## 🔄 Updates

When adding or modifying components:

1. Follow existing patterns and conventions
2. Use design tokens (no hardcoded values)
3. Support light/dark modes
4. Add TypeScript types
5. Update documentation
6. Add to demo page

## 📄 License

Part of the main project license.
