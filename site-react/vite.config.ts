import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { duiTokens } from './tokens';

export default defineConfig({
  // GitHub Pages serves a project site from /<repo>/, so assets must resolve below it.
  base: '/Tally/',
  plugins: [react(), duiTokens('../docs/dui-design-system/tokens.json')],
  // tally-site-shared is a symlink into the Gradle build output; pre-bundling it would cache a
  // stale copy after `npm run shared` rebuilds the Kotlin code.
  optimizeDeps: { exclude: ['tally-site-shared'] },
});
