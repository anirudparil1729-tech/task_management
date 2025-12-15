# UX Behavior Documentation

This document describes the user experience and behavior patterns across all core views of the productivity application.

## Overview

The application is an offline-first task management system with sync capabilities. All views are responsive and leverage the design system for consistent UI patterns.

## Core Views

### 1. Dashboard (`/dashboard`)

**Purpose**: Central hub for daily task management and overview

**Key Features**:
- **Quick Add Widget**: Inline form at the top for rapid task creation
- **Motivational Messages**: Random motivational message displayed on each visit to encourage users
- **Metric Cards**: Display total tasks, completed tasks, tasks due today, and overdue tasks
- **Completion Rate Progress Bar**: Visual representation of overall task completion percentage
- **Overdue Alerts**: Prominent section showing overdue tasks that need attention
- **Today's Tasks**: List of tasks due today with category indicators
- **Upcoming Tasks**: Preview of tasks due in the next 7 days
- **Sync Status**: Real-time sync status indicator with last sync timestamp

**Interactions**:
- Click any task to open the task detail drawer for full editing
- Quick add form submits on Enter key or button click
- Cards are responsive and reflow on smaller screens
- Overdue tasks are highlighted in red for urgency
- Completed tasks appear with strikethrough and reduced opacity

**Responsive Behavior**:
- Mobile: Single column layout
- Tablet: 2-column grid for metric cards
- Desktop: 4-column grid for metrics, 2-column for task lists

---

### 2. Categories (`/categories`)

**Purpose**: Manage task categories with custom colors and icons

**Key Features**:
- **Category Form**: Create or edit categories with name, color, and icon
- **Color Picker**: Preset color swatches plus custom color input
- **Icon Picker**: Visual grid of icon options to choose from
- **Category Cards**: Display categories with usage stats and progress bars
- **Task Statistics**: Show active and completed task counts per category
- **Visual Indicators**: Color-coded left border and icon representation
- **Edit/Delete Actions**: In-line editing and deletion (disabled for default categories)

**Interactions**:
- Click Edit button to populate form with category details
- Click color swatch to quickly change category color
- Icon picker allows visual selection from predefined set
- Progress bars show completion percentage for each category
- Cancel button clears form and exits edit mode

**Responsive Behavior**:
- Mobile: Single column card layout
- Tablet: 2-column grid
- Desktop: 3-column grid for category cards
- Icon picker adapts from 4 to 8 columns based on screen size

---

### 3. Task Detail Drawer (`TaskDetailDrawer` component)

**Purpose**: Comprehensive task editing interface

**Key Features**:
- **Slide-in Drawer**: Appears from right side of screen, overlay background
- **Full Task Editing**: Title, description, notes, due date, reminder time
- **Priority Selection**: 5-level priority dropdown (None, Low, Medium, High, Urgent)
- **Category Assignment**: Dropdown to assign task to a category
- **Recurrence Rule**: Text input for iCal RRULE format
- **Task Information Panel**: Read-only display of creation/update timestamps
- **Action Buttons**: Mark complete/incomplete, delete, save changes
- **Visual Icons**: Icons for each field (calendar, alarm, flag, tag, etc.)

**Interactions**:
- Drawer opens when clicking a task from any view
- Escape key or backdrop click closes drawer
- Save button updates task and closes drawer
- Delete button removes task and closes drawer
- Toggle complete button updates status immediately
- All form inputs are validated before saving

**Responsive Behavior**:
- Mobile: Full-width drawer (max-w-full)
- Tablet/Desktop: Fixed-width drawer (max-w-lg)
- Form fields stack vertically on mobile, 2-column grid on desktop
- Drawer scrolls independently of background content

---

### 4. Planner (`/planner`)

**Purpose**: Calendar-based time blocking and task scheduling

**Key Features**:
- **View Toggle**: Switch between daily and weekly views
- **Date Navigation**: Previous/next buttons and "Today" quick jump
- **Time Block Creation**: Add time blocks with start/end times
- **Task Linking**: Optionally link time blocks to existing tasks
- **Daily View**: Detailed list of time blocks with duration display
- **Weekly View**: 7-day grid overview with compact event previews
- **Time Formatting**: 12-hour format with AM/PM indicators
- **Duration Calculation**: Automatic duration display for blocks
- **Category Colors**: Time blocks inherit task category colors

**Interactions**:
- Click task to open detail drawer
- Add time block button reveals inline form
- Time pickers use native HTML5 time input
- Weekly view cards highlight current day with ring
- Delete button removes time block immediately
- Date navigation updates both daily and weekly views

**Responsive Behavior**:
- Mobile: Single column for weekly view
- Tablet: 2-column weekly grid
- Desktop: 3-4 column weekly grid
- Daily view always single column with responsive cards
- Navigation controls stack on mobile, inline on desktop

---

### 5. Productivity Insights (`/productivity`)

**Purpose**: Track productivity metrics and focus sessions

**Key Features**:
- **Daily Score**: Calculated score (0-100) based on task completion and focus sessions
- **Score Visualization**: Large gradient header with progress bar
- **Metric Cards**: Tasks today, focus sessions, streak counter
- **Focus Timer**: Pomodoro-style timer with countdown display
- **Browser Notifications**: Integration for timer completion
- **Reminder Scheduler**: Quick schedule reminders in minutes
- **Category Progress**: Per-category completion progress bars
- **End of Day Summary**: Summary cards with motivational feedback
- **Visual Feedback**: Emoji and color-coded messages based on performance

**Interactions**:
- Start/stop focus timer with minute input
- Enable notifications button requests browser permission
- Schedule reminders with title and delay in minutes
- Category progress bars show completion percentage
- Summary updates dynamically as tasks are completed
- Motivational messages change based on score thresholds

**Responsive Behavior**:
- Mobile: Single column layout
- Tablet: 2-column for focus timer and reminders
- Desktop: 3-column for metric cards
- Score header remains full-width on all screens
- Category progress list scrolls independently if needed

---

### 6. Settings (`/settings`)

**Purpose**: Application configuration and data management

**Key Features**:
- **Theme Selector**: Choose between system, light, or dark theme
- **Notification Management**: Enable browser notifications and set cadence
- **Sync Controls**: Manual sync trigger and status display
- **Data Summary**: Count of all data entities
- **Data Export**: Download all data as JSON file
- **Account Management**: Logout functionality
- **Sync Status**: Real-time sync status with error display
- **Version Information**: App version and description

**Interactions**:
- Theme buttons apply immediately on click
- Enable notifications requests browser permission
- Sync Now button triggers manual sync operation
- Export button downloads JSON file with timestamp
- Logout button clears session and redirects to login
- All sections expandable with clear visual hierarchy

**Responsive Behavior**:
- Mobile: Single column, full-width cards
- Desktop: Consistent card layout with comfortable spacing
- Settings sections stack vertically on all screen sizes
- Buttons adapt to container width on mobile

---

## Common UX Patterns

### Offline-First Behavior
- All operations work immediately without waiting for server
- Changes are queued in outbox for later sync
- Visual indicators show sync status (pending, syncing, synced)
- Error messages displayed if sync fails
- Local IDs used until server assignment

### Loading States
- Skeleton screens shown during initial hydration
- Inline loading indicators for async operations
- Disabled buttons during processing
- Spinner animations for sync operations

### Feedback & Validation
- Form validation on submit
- Error messages displayed inline
- Success feedback through state changes
- Empty states with helpful guidance
- Confirmation for destructive actions (implicit through undo-able design)

### Accessibility
- Semantic HTML structure
- ARIA labels on interactive elements
- Keyboard navigation support (Tab, Enter, Escape)
- Focus management in modals/drawers
- Color contrast compliant with WCAG AA
- Icon buttons have text labels or aria-labels

### Responsive Design
- Mobile-first approach
- Breakpoints: mobile (<768px), tablet (768-1024px), desktop (>1024px)
- Touch-friendly tap targets (min 44x44px)
- Adaptive layouts using CSS Grid and Flexbox
- Consistent spacing scale from design tokens

---

## Data Flow

1. **User Action**: User interacts with UI (create, update, delete)
2. **State Update**: AppStateProvider updates local state immediately
3. **Outbox Queue**: Change added to outbox for sync
4. **UI Update**: UI reflects new state without delay
5. **Background Sync**: SyncProvider automatically syncs when online
6. **Server Response**: Server returns confirmation with server ID
7. **State Merge**: Local entity updated with server ID
8. **Outbox Cleanup**: Processed changes removed from outbox

---

## Error Handling

### Network Errors
- Operations continue to work offline
- Sync errors displayed in Settings
- Retry logic built into sync provider
- User can manually trigger sync

### Validation Errors
- Inline form validation before submit
- Required fields clearly marked
- Error messages specific and actionable
- Forms remain populated on error

### Edge Cases
- Empty states with guidance
- Long text truncated with ellipsis
- Date/time handling across timezones
- Handling of deleted items with dependencies

---

## Performance Considerations

- State persisted to IndexedDB via KV store
- Debounced save (250ms) to reduce writes
- Virtual scrolling not needed (lists typically small)
- Optimistic UI updates for instant feedback
- Lazy loading of views (Next.js App Router)
- Animation performance (GPU-accelerated transforms)

---

## Future Enhancements

Potential UX improvements for future iterations:

- Drag-and-drop task reordering
- Keyboard shortcuts for power users
- Task templates and quick actions
- Bulk operations (multi-select)
- Advanced filtering and search
- Customizable dashboard widgets
- Task dependencies and subtasks
- Collaborative features
- Mobile app (React Native)
- Calendar integrations
- Export to other formats (CSV, PDF)

---

## Testing Recommendations

### Manual Testing
- Test all views on mobile, tablet, desktop
- Verify offline functionality (network tab disabled)
- Test with screen reader
- Keyboard-only navigation
- Dark mode appearance
- Long text and edge cases

### Integration Tests
- Critical user flows (create task → edit → complete)
- Offline to online transition
- Data persistence across sessions
- Sync conflict resolution
- Form validation
- Navigation between views

### E2E Tests
- Complete user journeys
- Multi-device sync
- Performance under load
- Browser compatibility
- Progressive Web App features
