/**
 * Turns DUI-lib's `tokens.json` into CSS custom properties at build time, as the virtual module
 * `virtual:dui-tokens.css`. The repo's tokens.json stays the single source of truth: the Kotlin
 * theme mirrors it by hand, the React site reads it directly.
 *
 * Output:
 *   :root { --bg: #fff; ... }                                light colours, type, spacing, radius
 *   @media (prefers-color-scheme: dark) { :root { ... } }    dark colours
 */
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import type { Plugin } from 'vite';

interface ColorToken { name: string; value: string | { light: string; dark: string } }
interface TypeStyle { name: string; fontSize: string; lineHeight: string; fontWeight: number; letterSpacing?: string }
interface Tokens {
  color: { tokens: ColorToken[] };
  type: { families: Record<string, string>; groups: { family: string; styles: TypeStyle[] }[] };
  spacing: { tokens: { name: string; value: string }[] };
  radius: { tokens: { name: string; value: string }[] };
}

const VIRTUAL_ID = 'virtual:dui-tokens.css';
const RESOLVED_ID = '\0' + VIRTUAL_ID;

export function duiTokens(path: string): Plugin {
  const file = resolve(path);
  return {
    name: 'dui-tokens',
    resolveId: (id) => (id === VIRTUAL_ID ? RESOLVED_ID : undefined),
    load(id) {
      if (id !== RESOLVED_ID) return undefined;
      this.addWatchFile(file);
      return toCss(JSON.parse(readFileSync(file, 'utf8')) as Tokens);
    },
  };
}

function toCss(tokens: Tokens): string {
  const colors = tokens.color.tokens;
  const resolveColor = (value: ColorToken['value'], theme: 'light' | 'dark'): string => {
    if (typeof value !== 'string') return value[theme];
    // An alias such as "{accent}" points at another colour token.
    const alias = /^\{(.+)}$/.exec(value)?.[1];
    return alias ? `var(--${alias})` : value;
  };
  const colorVars = (theme: 'light' | 'dark') =>
    colors.map((t) => `  --${t.name}: ${resolveColor(t.value, theme)};`);

  const families = Object.entries(tokens.type.families).map(([name, stack]) => `  --font-${name}: ${stack};`);
  const type = tokens.type.groups.flatMap((group) =>
    group.styles.map((s) =>
      // One shorthand per style, used as `font: var(--type-label)`.
      `  --type-${s.name}: ${s.fontWeight} ${s.fontSize}/${s.lineHeight} var(--font-${group.family});` +
      (s.letterSpacing ? `\n  --tracking-${s.name}: ${s.letterSpacing};` : ''),
    ),
  );
  const spacing = tokens.spacing.tokens.map((t) => `  --${t.name}: ${t.value};`);
  const radius = tokens.radius.tokens.map((t) => `  --${t.name}: ${t.value};`);

  return [
    ':root {',
    '  color-scheme: light dark;',
    ...colorVars('light'),
    ...families,
    ...type,
    ...spacing,
    ...radius,
    '}',
    '@media (prefers-color-scheme: dark) {',
    '  :root {',
    ...colorVars('dark').map((line) => '  ' + line),
    '  }',
    '}',
  ].join('\n');
}
