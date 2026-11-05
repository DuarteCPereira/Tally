/**
 * DUI-lib web components in React. Each mirrors the Kotlin component of the same name in
 * `:dui-lib`; the styling lives in `dui.css` on top of the token variables.
 */
import type { ReactNode } from 'react';
import type { JsEyebrow, JsLink, JsStoreLink } from 'tally-site-shared';
import { Icon } from './Icons';
import { hrefOf } from '../links';

/** The Tally app icon. `animate` draws it in once; CSS stops that for reduced motion. */
export function TallyMark({ size = 32, animate = false }: { size?: number; animate?: boolean }) {
  const strokes = [15, 21, 27, 33];
  return (
    <svg className={animate ? 'tally-draw' : undefined} width={size} height={size} viewBox="0 0 48 48" aria-hidden="true">
      <rect width="48" height="48" rx="11" fill="var(--fill-primary)" />
      <g fill="none" stroke="var(--on-primary)" strokeWidth="3" strokeLinecap="round">
        {strokes.map((x, i) => (
          <path key={x} className="v" pathLength={1} d={`M${x} 13v22`} style={{ animationDelay: `${i * 0.1}s` }} />
        ))}
      </g>
      <g className="bb" transform="rotate(-25 24 24)">
        <path d="M8 24h32" fill="none" stroke="var(--fill-primary)" strokeWidth="7" strokeLinecap="round" />
        <path d="M8 24h32" fill="none" stroke="var(--on-primary)" strokeWidth="3" strokeLinecap="round" />
        {[10, 34].map((x) => (
          <rect key={x} x={x} y="17" width="4" height="14" rx="1.5" fill="var(--on-primary)"
            stroke="var(--fill-primary)" strokeWidth="2.5" paintOrder="stroke" />
        ))}
      </g>
    </svg>
  );
}

/** "01 · Build": index in accent, text in text-secondary, mono. */
export function Eyebrow({ eyebrow }: { eyebrow: JsEyebrow }) {
  return (
    <span className="t-eyebrow secondary">
      <span className="accent">{eyebrow.index}</span> · {eyebrow.text}
    </span>
  );
}

export function Badge({ children }: { children: ReactNode }) {
  return <span className="badge">{children}</span>;
}

/** A page section: tone, optional hairline grid and top divider, 1200px column. */
export function SiteSection(props: {
  id?: string;
  labelledBy?: string;
  surface?: boolean;
  grid?: boolean;
  divider?: boolean;
  children: ReactNode;
}) {
  const classes = ['section', props.surface && 'surface', props.grid && 'grid', props.divider && 'divider'];
  return (
    <section id={props.id} aria-labelledby={props.labelledBy} className={classes.filter(Boolean).join(' ')}>
      <div className="column">{props.children}</div>
    </section>
  );
}

/** Eyebrow, title and optional body: how every section opens. */
export function SectionHeading(props: {
  id: string;
  eyebrow: JsEyebrow;
  title: string;
  body?: string;
  trailing?: ReactNode;
  className?: string;
}) {
  return (
    <div className={['heading', props.className].filter(Boolean).join(' ')}>
      <div className="row">
        <Eyebrow eyebrow={props.eyebrow} />
        {props.trailing}
      </div>
      <h2 id={props.id} className="t-display">{props.title}</h2>
      {props.body && <p className="secondary">{props.body}</p>}
    </div>
  );
}

export function SiteHeader({ brand, links, action }: { brand: string; links: JsLink[]; action: JsLink }) {
  return (
    <header className="site-header">
      <a className="brand" href="#top"><TallyMark size={32} />{brand}</a>
      <nav aria-label="Main">
        {links.map((link) => (
          <a key={link.label} className="nav-link" href={hrefOf(link.target)}>
            {link.label}
            {link.badge && <Badge>{link.badge}</Badge>}
          </a>
        ))}
      </nav>
      <span className="spacer" />
      <a className="button-small" href={hrefOf(action.target)}>{action.label}</a>
    </header>
  );
}

export function SiteFooter({ note, links }: { note: string; links: JsLink[] }) {
  return (
    <footer className="site-footer">
      <span className="row" style={{ gap: 'var(--space-2)' }}><TallyMark size={24} />{note}</span>
      <nav aria-label="Footer">
        {links.map((link) => <a key={link.label} href={hrefOf(link.target)}>{link.label}</a>)}
      </nav>
    </footer>
  );
}

export function StoreButtons({ stores, center = false }: { stores: JsStoreLink[]; center?: boolean }) {
  return (
    <div className="store-buttons" style={center ? { justifyContent: 'center' } : undefined}>
      {stores.map((store) => (
        <a key={store.platform} className={store.primary ? 'store-button primary' : 'store-button'} href={hrefOf(store.target)}>
          <Icon name="download" />
          <span><span className="t-caption">Get it for</span><span className="t-label">{store.platform}</span></span>
        </a>
      ))}
    </div>
  );
}

/** A real app screenshot in a phone frame. */
export function DeviceFrame({ src, alt, scale = 0.8 }: { src: string; alt: string; scale?: number }) {
  return (
    <div className="device" style={{ ['--scale' as string]: scale }}>
      <div className="screen"><img src={src} alt={alt} loading="lazy" /></div>
    </div>
  );
}
