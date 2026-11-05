import type { JsLink } from 'tally-site-shared';

/** Section targets become in-page anchors, so the browser does the scrolling; URLs pass through. */
export function hrefOf(target: JsLink.Target): string {
  return target.kind === 'section' ? `#${target.value}` : target.value;
}

/** Each app screen id from the shared content, mapped to its screenshot and description. */
export const screens: Record<string, { src: string; alt: string }> = {
  run: { src: '/screens/screen_run.jpg', alt: 'Tally run screen: round 1 of a superset, with the rep counter and the Complete set button' },
  rest: { src: '/screens/screen_rest.jpg', alt: 'Tally rest timer counting down between sets' },
  editor: { src: '/screens/screen_editor.jpg', alt: 'Tally workout editor with exercises and a superset' },
  summary: { src: '/screens/screen_summary.jpg', alt: 'Tally session summary after a finished workout' },
};
