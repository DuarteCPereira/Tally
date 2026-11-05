/** The DUI outline icons used on the site: 24 grid, 1.5 round stroke, currentColor. */
const paths = {
  download: ['M12 4v12', 'M7 11l5 5 5-5', 'M5 20h14'],
  sets: ['M4 7h16', 'M4 12h16', 'M4 17h16'],
  search: ['M11 4a7 7 0 1 0 0 14a7 7 0 1 0 0 -14', 'M16 16l4 4'],
  timer: ['M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0', 'M12 7v5l3 2'],
  sound: ['M5 9v6h3l5 4V5L8 9z', 'M17 9a4 4 0 0 1 0 6'],
  trend: ['M4 18l5-6 4 3 7-9'],
  theme: ['M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0', 'M12 3v18', 'M12 8l5-4', 'M12 13l8-6', 'M12 18l8-7'],
} as const;

export type IconName = keyof typeof paths;

export function isIconName(name: string): name is IconName {
  return name in paths;
}

export function Icon({ name }: { name: IconName }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"
      strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {paths[name].map((d) => <path key={d} d={d} />)}
    </svg>
  );
}
