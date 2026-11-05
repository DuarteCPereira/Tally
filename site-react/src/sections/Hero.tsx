import type { JsCallout, JsHero } from 'tally-site-shared';
import { DeviceFrame, SiteSection, StoreButtons, TallyMark } from '../components/Dui';
import { screens } from '../links';
import './hero.css';

/** The first section: the headline card on a hairline grid, next to the live run screen. */
export function Hero({ hero }: { hero: JsHero }) {
  const screen = screens[hero.screen];
  return (
    <SiteSection grid>
      <div className="two-col">
        <div className="panel hero-copy">
          <span className="hero-chip t-eyebrow secondary"><span className="live-dot" />{hero.chip}</span>
          <TallyMark size={72} animate />
          <h1 className="t-hero hero-title">{hero.title}</h1>
          <p className="secondary hero-body">{hero.body}</p>
          <StoreButtons stores={hero.stores} />
          <span className="t-eyebrow secondary">{hero.tagline}</span>
        </div>
        <div className="hero-phone">
          <div className="hero-frame">
            <DeviceFrame src={screen.src} alt={screen.alt} />
            {hero.callouts.map((callout) => <Callout key={callout.text} callout={callout} />)}
          </div>
        </div>
      </div>
    </SiteSection>
  );
}

/** A mono label with a leader line and a dot, pointing into the phone. Hidden below 980px. */
function Callout({ callout }: { callout: JsCallout }) {
  const start = callout.side === 'start';
  return (
    <div className={`callout ${start ? 'start' : 'end'}`} style={{ top: `${callout.topFraction * 100}%` }} aria-hidden="true">
      {start && <span className="callout-label t-counter">{callout.text}</span>}
      {!start && <span className="callout-dot" />}
      <span className="callout-line" />
      {start && <span className="callout-dot" />}
      {!start && <span className="callout-label t-counter">{callout.text}</span>}
    </div>
  );
}
