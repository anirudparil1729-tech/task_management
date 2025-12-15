# Frontend

Next.js frontend application with TypeScript and Tailwind CSS.

## 🚀 Getting Started

### Install dependencies

```bash
pnpm install
```

### Run development server

```bash
pnpm dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

## 📝 Scripts

- `pnpm dev` - Start development server
- `pnpm build` - Build for production
- `pnpm start` - Start production server
- `pnpm lint` - Run ESLint
- `pnpm format` - Format code with Prettier
- `pnpm format:check` - Check code formatting
- `pnpm test` - Run tests
- `pnpm typecheck` - Run TypeScript type checking
- `pnpm export-tokens:android` - Export design tokens for Android

## 🛠️ Tech Stack

- **Framework:** Next.js 14+ (App Router)
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **Design System:** Custom design system with Radix UI + Framer Motion
- **Icons:** Lucide React
- **Linting:** ESLint
- **Formatting:** Prettier

## 📁 Project Structure

```
frontend/
├── src/
│   ├── app/                    # App Router pages
│   │   ├── layout.tsx          # Root layout
│   │   ├── page.tsx            # Home page
│   │   └── design-system-demo/ # Design system demo page
│   ├── design-system/          # Design system
│   │   ├── components/         # UI components
│   │   ├── tokens/             # Design tokens
│   │   └── utils/              # Utilities
│   ├── components/             # App-specific components
│   └── lib/                    # Utilities and helpers
├── docs/                       # Documentation
│   └── design-system.md        # Design system docs
├── scripts/                    # Build scripts
│   └── export-tokens-android.ts # Android token export
├── public/                     # Static assets
├── next.config.ts              # Next.js configuration
├── tsconfig.json               # TypeScript configuration
└── package.json                # Dependencies and scripts
```

## 🔧 Environment Variables

Create a `.env.local` file in the frontend directory:

```env
NEXT_PUBLIC_API_URL=http://localhost:8000
```

## 🎨 Design System

This project includes a comprehensive design system. See:

- **Quick Start**: [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md)
- **Full Documentation**: [docs/design-system.md](./docs/design-system.md)
- **Component Source**: [src/design-system/](./src/design-system/)
- **Live Demo**: Run `pnpm dev` and visit `/design-system-demo`

## 📚 Learn More

- [Next.js Documentation](https://nextjs.org/docs)
- [TypeScript Documentation](https://www.typescriptlang.org/docs)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Radix UI Documentation](https://www.radix-ui.com/docs)
- [Framer Motion Documentation](https://www.framer.com/motion/)
