import { colors, spacing, typography } from '../src/design-system/tokens';

function hslToHex(hsl: string): string {
  const match = hsl.match(/hsl\((\d+),\s*(\d+)%,\s*(\d+)%\)/);
  if (!match) return '#000000';

  const h = parseInt(match[1]);
  const s = parseInt(match[2]) / 100;
  const l = parseInt(match[3]) / 100;

  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = l - c / 2;

  let r = 0,
    g = 0,
    b = 0;

  if (h >= 0 && h < 60) {
    r = c;
    g = x;
    b = 0;
  } else if (h >= 60 && h < 120) {
    r = x;
    g = c;
    b = 0;
  } else if (h >= 120 && h < 180) {
    r = 0;
    g = c;
    b = x;
  } else if (h >= 180 && h < 240) {
    r = 0;
    g = x;
    b = c;
  } else if (h >= 240 && h < 300) {
    r = x;
    g = 0;
    b = c;
  } else if (h >= 300 && h < 360) {
    r = c;
    g = 0;
    b = x;
  }

  const toHex = (n: number) => {
    const hex = Math.round((n + m) * 255).toString(16);
    return hex.length === 1 ? '0' + hex : hex;
  };

  return `#${toHex(r)}${toHex(g)}${toHex(b)}`.toUpperCase();
}

function remToDp(rem: string): string {
  const value = parseFloat(rem);
  return `${Math.round(value * 16)}dp`;
}

function generateColorXml() {
  console.log('<!-- colors.xml -->');
  console.log('<resources>');
  console.log('  <!-- Light Mode Colors -->');

  // Primary colors
  console.log('  <!-- Primary -->');
  Object.entries(colors.light.primary).forEach(([shade, color]) => {
    console.log(`  <color name="primary_${shade}">${hslToHex(color)}</color>`);
  });

  // Category colors
  console.log('\n  <!-- Category Colors -->');
  Object.entries(colors.light.category).forEach(([category, color]) => {
    console.log(`  <color name="category_${category}">${hslToHex(color)}</color>`);
  });

  console.log('</resources>\n');
}

function generateDimensXml() {
  console.log('<!-- dimens.xml -->');
  console.log('<resources>');
  console.log('  <!-- Spacing -->');

  Object.entries(spacing).forEach(([key, value]) => {
    if (typeof value === 'string' && value !== '0') {
      const dpValue = remToDp(value);
      const name = key.toString().replace('.', '_');
      console.log(`  <dimen name="spacing_${name}">${dpValue}</dimen>`);
    }
  });

  console.log('</resources>\n');
}

function generateStylesXml() {
  console.log('<!-- styles.xml -->');
  console.log('<resources>');
  console.log('  <!-- Typography -->');

  Object.entries(typography.fontSize).forEach(([size, value]) => {
    const [fontSize] = value as [string, { lineHeight: string }];
    const spValue = remToDp(fontSize).replace('dp', 'sp');
    console.log(`  <style name="TextAppearance.${size.replace('-', '_')}">`);
    console.log(`    <item name="android:textSize">${spValue}</item>`);
    console.log('  </style>');
  });

  console.log('</resources>\n');
}

console.log('='.repeat(80));
console.log('Android Design Token Export');
console.log('='.repeat(80));
console.log('\nCopy the following XML into your Android project:\n');

generateColorXml();
generateDimensXml();
generateStylesXml();

console.log('='.repeat(80));
console.log('Export complete!');
console.log('='.repeat(80));
