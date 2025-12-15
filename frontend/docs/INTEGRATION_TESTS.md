# Integration Tests Documentation

This document outlines the integration tests for critical user flows in the productivity application.

## Test Setup

The application uses React Testing Library and Jest for integration testing. Tests focus on user behavior rather than implementation details.

## Critical Flows to Test

### 1. Task Management Flow

**Test: Create, Edit, and Complete Task**

```typescript
describe('Task Management Flow', () => {
  it('should create a task, edit it, and mark it complete', async () => {
    // 1. Navigate to dashboard
    // 2. Enter task title in quick-add input
    // 3. Click add button
    // 4. Verify task appears in list
    // 5. Click on task to open drawer
    // 6. Edit task details (description, due date, category)
    // 7. Save changes
    // 8. Verify drawer closes and changes are reflected
    // 9. Click task to reopen drawer
    // 10. Mark task as complete
    // 11. Verify task shows completed state
  });
});
```

**Expected Behavior**:
- Task is created and appears immediately
- Drawer opens when task is clicked
- All edits are saved locally
- Completed tasks show strikethrough and badge
- Changes persist across drawer open/close

---

### 2. Category Management Flow

**Test: Create Category and Assign to Task**

```typescript
describe('Category Management Flow', () => {
  it('should create a category and assign it to a task', async () => {
    // 1. Navigate to categories page
    // 2. Fill in category name
    // 3. Select a color from preset swatches
    // 4. Select an icon from the picker
    // 5. Click add button
    // 6. Verify category appears in list
    // 7. Navigate to dashboard
    // 8. Create a new task
    // 9. Open task detail drawer
    // 10. Assign the newly created category
    // 11. Save task
    // 12. Verify task shows category color indicator
  });
});
```

**Expected Behavior**:
- Category is created with selected color and icon
- Category appears in the list with stats
- Category is available in task assignment dropdown
- Task displays category color and name
- Category usage stats update automatically

---

### 3. Time Blocking Flow

**Test: Create Time Block Linked to Task**

```typescript
describe('Time Blocking Flow', () => {
  it('should create a time block and link it to a task', async () => {
    // 1. Create a task with due date
    // 2. Navigate to planner page
    // 3. Select today's date
    // 4. Click "Add Time Block" button
    // 5. Fill in block title
    // 6. Set start and end times
    // 7. Select the task from dropdown
    // 8. Click add button
    // 9. Verify time block appears in daily view
    // 10. Click linked task name
    // 11. Verify task detail drawer opens
  });
});
```

**Expected Behavior**:
- Time block is created with specified times
- Duration is calculated and displayed
- Linked task is shown in time block
- Time block inherits task category color
- Clicking linked task opens detail drawer

---

### 4. Offline-to-Online Sync Flow

**Test: Create Data Offline and Sync Online**

```typescript
describe('Offline-to-Online Sync Flow', () => {
  it('should queue changes offline and sync when online', async () => {
    // 1. Disable network (simulate offline)
    // 2. Create multiple tasks
    // 3. Create a category
    // 4. Verify "pending changes" count increases
    // 5. Verify sync status shows "offline" or "idle"
    // 6. Re-enable network (simulate online)
    // 7. Wait for automatic sync or trigger manual sync
    // 8. Verify sync status changes to "syncing" then "synced"
    // 9. Verify pending changes count decreases
    // 10. Verify entities have server IDs assigned
  });
});
```

**Expected Behavior**:
- All operations work offline
- Changes are queued in outbox
- Sync initiates automatically when online
- Server IDs replace local IDs
- No data loss during sync
- Conflicts handled gracefully

---

### 5. Focus Timer and Notifications Flow

**Test: Start Focus Timer and Receive Notification**

```typescript
describe('Focus Timer Flow', () => {
  it('should start timer and show notification on completion', async () => {
    // 1. Navigate to productivity page
    // 2. Grant notification permission (if needed)
    // 3. Enter focus duration (use 1 minute for testing)
    // 4. Click start button
    // 5. Verify timer shows countdown
    // 6. Verify start button changes to stop button
    // 7. Wait for timer to complete (mock/fast-forward time)
    // 8. Verify notification is shown
    // 9. Verify timer status changes to completed
    // 10. Verify focus session count increases
  });
});
```

**Expected Behavior**:
- Timer starts immediately
- Countdown updates every second
- Notification appears on completion
- Timer can be stopped early
- Focus session count tracked

---

### 6. Data Export Flow

**Test: Export All Data**

```typescript
describe('Data Export Flow', () => {
  it('should export all data as JSON file', async () => {
    // 1. Create sample data (tasks, categories, time blocks)
    // 2. Navigate to settings page
    // 3. Click "Export All Data" button
    // 4. Verify download is triggered
    // 5. Verify filename includes current date
    // 6. Parse downloaded JSON
    // 7. Verify all entities are included
    // 8. Verify structure matches expected format
    // 9. Verify timestamps are included
  });
});
```

**Expected Behavior**:
- Export includes all data types
- JSON is valid and well-formatted
- Filename includes timestamp
- Export includes metadata (version, exportedAt)
- No sensitive data included

---

### 7. Theme Switching Flow

**Test: Switch Between Themes**

```typescript
describe('Theme Switching Flow', () => {
  it('should switch between light, dark, and system themes', async () => {
    // 1. Navigate to settings page
    // 2. Click "Light" theme button
    // 3. Verify theme updates (check CSS classes or styles)
    // 4. Verify preference is saved (localStorage)
    // 5. Click "Dark" theme button
    // 6. Verify theme updates to dark
    // 7. Refresh page
    // 8. Verify selected theme persists
    // 9. Click "System" theme button
    // 10. Verify theme matches system preference
  });
});
```

**Expected Behavior**:
- Theme applies immediately
- No flash of wrong theme on load
- Preference persists across sessions
- System theme respects OS setting
- All views respect theme

---

### 8. Task Detail Validation Flow

**Test: Form Validation in Task Drawer**

```typescript
describe('Task Detail Validation Flow', () => {
  it('should validate task details and show errors', async () => {
    // 1. Create a task
    // 2. Open task detail drawer
    // 3. Clear the title field
    // 4. Click save
    // 5. Verify error message (or title reverts)
    // 6. Enter valid title
    // 7. Set invalid date format (if applicable)
    // 8. Verify validation message
    // 9. Set end time before start time (recurrence)
    // 10. Verify appropriate feedback
  });
});
```

**Expected Behavior**:
- Required fields are validated
- Error messages are clear
- Form doesn't submit with errors
- Previous values preserved on error
- Validation happens on submit

---

## Test Utilities

### Mock Functions

```typescript
// Mock AppStateProvider
const mockCreateTask = jest.fn();
const mockUpdateTask = jest.fn();
const mockDeleteTask = jest.fn();

// Mock SyncProvider
const mockSyncNow = jest.fn();

// Mock NotificationsProvider
const mockShowNotification = jest.fn();
const mockRequestPermission = jest.fn();
```

### Test Helpers

```typescript
// Helper to render component with providers
function renderWithProviders(component) {
  return render(
    <AppStateProvider>
      <SyncProvider>
        <NotificationsProvider>
          <ThemeProvider>
            {component}
          </ThemeProvider>
        </NotificationsProvider>
      </SyncProvider>
    </AppStateProvider>
  );
}

// Helper to create test data
function createTestTask(overrides = {}) {
  return {
    localId: 'task:local:test-id',
    title: 'Test Task',
    description: null,
    notes: null,
    dueDate: null,
    reminderTime: null,
    priority: 0,
    recurrenceRule: null,
    isCompleted: false,
    completedAt: null,
    categoryLocalId: null,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    ...overrides,
  };
}

// Helper to wait for state updates
async function waitForStateUpdate() {
  await waitFor(() => {
    // Wait for any pending state updates
  });
}
```

---

## Running Tests

### Commands

```bash
# Run all tests
pnpm test

# Run tests in watch mode
pnpm test:watch

# Run tests with coverage
pnpm test:coverage

# Run specific test file
pnpm test TaskManagement.test.tsx

# Run tests matching pattern
pnpm test --testNamePattern="should create a task"
```

### Coverage Goals

- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Function Coverage**: > 80%
- **Statement Coverage**: > 80%

Focus on critical paths and user-facing features.

---

## Continuous Integration

Tests should run on:
- Every pull request
- Before merge to main
- On scheduled basis (nightly)
- Before deployment

### CI Configuration

```yaml
# .github/workflows/test.yml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: pnpm/action-setup@v2
      - uses: actions/setup-node@v3
      - run: pnpm install
      - run: pnpm test:coverage
      - uses: codecov/codecov-action@v3
```

---

## Manual Testing Checklist

In addition to automated tests, manual testing should cover:

- [ ] Visual appearance on different screen sizes
- [ ] Touch interactions on mobile devices
- [ ] Keyboard navigation and shortcuts
- [ ] Screen reader compatibility
- [ ] Performance with large datasets
- [ ] Network conditions (slow 3G, offline)
- [ ] Browser compatibility (Chrome, Firefox, Safari, Edge)
- [ ] PWA installation and offline functionality
- [ ] Notification delivery and click handling
- [ ] Data persistence across browser restarts

---

## Test Data Management

### Setup
- Use factory functions for test data
- Reset state between tests
- Mock external services (API, notifications)
- Use fake timers for time-dependent tests

### Cleanup
- Clear IndexedDB after each test
- Reset all mocks
- Remove event listeners
- Clear localStorage/sessionStorage

---

## Known Testing Challenges

1. **IndexedDB in tests**: Use fake-indexeddb or similar library
2. **Service Worker**: May need to mock or test separately
3. **Notifications API**: Mock browser APIs in Jest environment
4. **Time-dependent tests**: Use Jest fake timers
5. **File downloads**: Mock download functionality
6. **Date/timezone handling**: Use consistent timezone for tests

---

## Resources

- [React Testing Library](https://testing-library.com/react)
- [Jest Documentation](https://jestjs.io/)
- [Testing Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
- [Accessibility Testing](https://testing-library.com/docs/dom-testing-library/api-accessibility)
