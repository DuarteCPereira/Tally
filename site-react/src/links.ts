import type { JsLink } from 'tally-site-shared';

/** Section targets become in-page anchors, so the browser does the scrolling; URLs pass through. */
export function hrefOf(target: JsLink.Target): string {
  return target.kind === 'section' ? `#${target.value}` : target.value;
}

/** Where the site is served from: `/` in dev, `/Tally/` on GitHub Pages (see `base` in vite.config.ts). */
const BASE = import.meta.env.BASE_URL;

/** Each app screen id from the shared content, mapped to its screenshot and description. */
export const screens: Record<string, { src: string; alt: string }> = {
  run: { src: `${BASE}screens/screen_run.jpg`, alt: 'Tally run screen: round 1 of a superset, with the rep counter and the Complete set button' },
  rest: { src: `${BASE}screens/screen_rest.jpg`, alt: 'Tally rest timer counting down between sets' },
  editor: { src: `${BASE}screens/screen_editor.jpg`, alt: 'Tally workout editor with exercises and a superset' },
  summary: { src: `${BASE}screens/screen_summary.jpg`, alt: 'Tally session summary after a finished workout' },
};
