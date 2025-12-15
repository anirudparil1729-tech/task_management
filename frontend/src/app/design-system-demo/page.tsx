'use client';

import {
  Button,
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
  Badge,
  Icon,
  Input,
  Textarea,
  Container,
  Stack,
  Grid,
} from '@/design-system';
import { CheckIcon, AlertCircleIcon, HeartIcon, StarIcon } from 'lucide-react';

export default function DesignSystemDemo() {
  return (
    <Container size="xl" padding="lg">
      <Stack gap={8}>
        <div>
          <h1 className="mb-2 text-4xl font-bold">Design System Demo</h1>
          <p className="text-foreground-secondary">
            A comprehensive showcase of all design system components and tokens.
          </p>
        </div>

        <section>
          <h2 className="mb-4 text-2xl font-semibold">Buttons</h2>
          <Grid cols={1} gap={4} className="md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Button Variants</CardTitle>
              </CardHeader>
              <CardContent>
                <Stack gap={3}>
                  <Button variant="primary">Primary Button</Button>
                  <Button variant="secondary">Secondary Button</Button>
                  <Button variant="success">Success Button</Button>
                  <Button variant="warning">Warning Button</Button>
                  <Button variant="danger">Danger Button</Button>
                  <Button variant="outline">Outline Button</Button>
                  <Button variant="ghost">Ghost Button</Button>
                  <Button variant="link">Link Button</Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Button Sizes</CardTitle>
              </CardHeader>
              <CardContent>
                <Stack gap={3}>
                  <Button size="sm">Small Button</Button>
                  <Button size="md">Medium Button</Button>
                  <Button size="lg">Large Button</Button>
                  <Button size="xl">Extra Large Button</Button>
                  <Button size="icon">
                    <Icon icon={HeartIcon} size="md" />
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        </section>

        <section>
          <h2 className="mb-4 text-2xl font-semibold">Cards</h2>
          <Grid cols={1} gap={4} className="md:grid-cols-2 lg:grid-cols-4">
            <Card variant="default" animated>
              <CardHeader>
                <CardTitle>Default Card</CardTitle>
                <CardDescription>Subtle shadow on hover</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm">This is a default card with standard styling.</p>
              </CardContent>
            </Card>

            <Card variant="elevated" animated>
              <CardHeader>
                <CardTitle>Elevated Card</CardTitle>
                <CardDescription>More prominent shadow</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm">This card has a more prominent elevation.</p>
              </CardContent>
            </Card>

            <Card variant="outlined" animated>
              <CardHeader>
                <CardTitle>Outlined Card</CardTitle>
                <CardDescription>Border only, no shadow</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm">This card uses only a border.</p>
              </CardContent>
            </Card>

            <Card variant="filled" animated>
              <CardHeader>
                <CardTitle>Filled Card</CardTitle>
                <CardDescription>Filled background</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm">This card has a filled background.</p>
              </CardContent>
            </Card>
          </Grid>
        </section>

        <section>
          <h2 className="mb-4 text-2xl font-semibold">Badges</h2>
          <Grid cols={1} gap={4} className="md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Standard Badges</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  <Badge variant="default">Default</Badge>
                  <Badge variant="secondary">Secondary</Badge>
                  <Badge variant="success">Success</Badge>
                  <Badge variant="warning">Warning</Badge>
                  <Badge variant="danger">Danger</Badge>
                  <Badge variant="outline">Outline</Badge>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Category Badges</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  <Badge variant="work">Work</Badge>
                  <Badge variant="personal">Personal</Badge>
                  <Badge variant="health">Health</Badge>
                  <Badge variant="finance">Finance</Badge>
                  <Badge variant="social">Social</Badge>
                  <Badge variant="education">Education</Badge>
                  <Badge variant="entertainment">Entertainment</Badge>
                  <Badge variant="other">Other</Badge>
                </div>
              </CardContent>
            </Card>
          </Grid>
        </section>

        <section>
          <h2 className="mb-4 text-2xl font-semibold">Icons</h2>
          <Card>
            <CardHeader>
              <CardTitle>Icon Sizes and Colors</CardTitle>
            </CardHeader>
            <CardContent>
              <Stack gap={4}>
                <div>
                  <h4 className="mb-2 text-sm font-medium">Sizes</h4>
                  <div className="flex items-center gap-3">
                    <Icon icon={StarIcon} size="xs" />
                    <Icon icon={StarIcon} size="sm" />
                    <Icon icon={StarIcon} size="md" />
                    <Icon icon={StarIcon} size="lg" />
                    <Icon icon={StarIcon} size="xl" />
                    <Icon icon={StarIcon} size="2xl" />
                  </div>
                </div>
                <div>
                  <h4 className="mb-2 text-sm font-medium">Colors</h4>
                  <div className="flex items-center gap-3">
                    <Icon icon={CheckIcon} color="default" size="lg" />
                    <Icon icon={CheckIcon} color="primary" size="lg" />
                    <Icon icon={CheckIcon} color="secondary" size="lg" />
                    <Icon icon={CheckIcon} color="success" size="lg" />
                    <Icon icon={CheckIcon} color="warning" size="lg" />
                    <Icon icon={CheckIcon} color="danger" size="lg" />
                    <Icon icon={CheckIcon} color="muted" size="lg" />
                  </div>
                </div>
              </Stack>
            </CardContent>
          </Card>
        </section>

        <section>
          <h2 className="mb-4 text-2xl font-semibold">Form Inputs</h2>
          <Grid cols={1} gap={4} className="md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Input Variants</CardTitle>
              </CardHeader>
              <CardContent>
                <Stack gap={4}>
                  <Input
                    id="default-input"
                    label="Default Input"
                    placeholder="Enter text..."
                    helperText="This is helper text"
                  />
                  <Input
                    id="success-input"
                    label="Success Input"
                    variant="success"
                    placeholder="Enter text..."
                    defaultValue="Valid input"
                  />
                  <Input
                    id="error-input"
                    label="Error Input"
                    placeholder="Enter text..."
                    error="This field is required"
                  />
                  <Input
                    id="disabled-input"
                    label="Disabled Input"
                    disabled
                    placeholder="Disabled"
                  />
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Textarea</CardTitle>
              </CardHeader>
              <CardContent>
                <Textarea
                  id="description"
                  label="Description"
                  placeholder="Enter a detailed description..."
                  rows={6}
                  helperText="Maximum 500 characters"
                />
              </CardContent>
            </Card>
          </Grid>
        </section>

        <section>
          <h2 className="mb-4 text-2xl font-semibold">Color Palette</h2>
          <Grid cols={1} gap={4}>
            <Card>
              <CardHeader>
                <CardTitle>Primary Colors</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex gap-2">
                  <div className="h-16 w-16 rounded-lg bg-primary-100" title="primary-100" />
                  <div className="h-16 w-16 rounded-lg bg-primary-200" title="primary-200" />
                  <div className="h-16 w-16 rounded-lg bg-primary-300" title="primary-300" />
                  <div className="h-16 w-16 rounded-lg bg-primary-400" title="primary-400" />
                  <div className="h-16 w-16 rounded-lg bg-primary-500" title="primary-500" />
                  <div className="h-16 w-16 rounded-lg bg-primary-600" title="primary-600" />
                  <div className="h-16 w-16 rounded-lg bg-primary-700" title="primary-700" />
                  <div className="h-16 w-16 rounded-lg bg-primary-800" title="primary-800" />
                  <div className="h-16 w-16 rounded-lg bg-primary-900" title="primary-900" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Category Colors</CardTitle>
              </CardHeader>
              <CardContent>
                <Grid cols={4} gap={3} className="md:grid-cols-8">
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-work" />
                    <span className="text-xs">Work</span>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-personal" />
                    <span className="text-xs">Personal</span>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-health" />
                    <span className="text-xs">Health</span>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-finance" />
                    <span className="text-xs">Finance</span>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-social" />
                    <span className="text-xs">Social</span>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-education" />
                    <span className="text-xs">Education</span>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-entertainment" />
                    <span className="text-xs">Entertainment</span>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <div className="h-12 w-12 rounded-lg bg-category-other" />
                    <span className="text-xs">Other</span>
                  </div>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        </section>

        <section>
          <h2 className="mb-4 text-2xl font-semibold">Interactive Example</h2>
          <Card variant="elevated" animated>
            <CardHeader>
              <CardTitle>Task Card Example</CardTitle>
              <CardDescription>A practical example combining multiple components</CardDescription>
            </CardHeader>
            <CardContent>
              <Stack gap={4}>
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <Icon icon={AlertCircleIcon} color="primary" size="lg" />
                    <div>
                      <h4 className="font-semibold">Complete quarterly report</h4>
                      <p className="text-sm text-foreground-secondary">Due in 3 days</p>
                    </div>
                  </div>
                  <Badge variant="work" size="sm">
                    Work
                  </Badge>
                </div>
                <p className="text-sm text-foreground-secondary">
                  Compile data from Q4 and prepare presentation for stakeholders. Include financial
                  metrics and performance analysis.
                </p>
              </Stack>
            </CardContent>
            <CardFooter>
              <div className="flex w-full gap-2">
                <Button variant="primary" className="flex-1">
                  <Icon icon={CheckIcon} size="sm" />
                  Complete
                </Button>
                <Button variant="outline" className="flex-1">
                  Snooze
                </Button>
              </div>
            </CardFooter>
          </Card>
        </section>
      </Stack>
    </Container>
  );
}
