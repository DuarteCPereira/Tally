import type { JsDownload, JsFeature, JsPrinciple, JsSection, JsStep } from 'tally-site-shared';
import { DeviceFrame, Eyebrow, SectionHeading, SiteSection, StoreButtons, TallyMark } from '../components/Dui';
import { Icon, isIconName } from '../components/Icons';
import { screens } from '../links';

/** "One screen. One action." next to a card of facts. */
export function Principle({ principle }: { principle: JsPrinciple }) {
  return (
    <SiteSection surface divider labelledBy="principle">
      <div className="two-col">
        <SectionHeading id="principle" eyebrow={principle.eyebrow} title={principle.title} body={principle.body} />
        <dl className="t-counter" style={{ padding: 'var(--space-2) var(--space-5)', borderRadius: 'var(--radius-lg)', background: 'var(--bg)' }}>
          {principle.facts.map((fact, i) => (
            <div key={fact.term} style={{
              display: 'flex', justifyContent: 'space-between', gap: 'var(--space-4)', padding: 'var(--space-4) 0',
              borderTop: i > 0 ? '1px solid var(--divider)' : undefined,
            }}>
              <dt className="secondary">{fact.term}</dt>
              <dd className={fact.accent ? 'accent' : undefined}>{fact.value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </SiteSection>
  );
}

/** Build, run, review: three cards, each with a real screen at the bottom. */
export function Steps({ steps }: { steps: JsSection<JsStep> }) {
  return (
    <SiteSection id="how" labelledBy="how-title">
      <SectionHeading id="how-title" eyebrow={steps.eyebrow} title={steps.title} />
      <div className="cards-3">
        {steps.items.map((step) => {
          const screen = screens[step.screen];
          return (
            <article key={step.title} className="card" style={{ gap: 'var(--space-5)' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                <Eyebrow eyebrow={step.eyebrow} />
                <h3 className="t-title">{step.title}</h3>
                <p className="secondary">{step.body}</p>
              </div>
              {/* margin-top: auto lines the three phones up along the bottom of the row. */}
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: 'auto' }}>
                <DeviceFrame src={screen.src} alt={screen.alt} scale={0.7} />
              </div>
            </article>
          );
        })}
      </div>
    </SiteSection>
  );
}

/** The six feature cards. */
export function Features({ features }: { features: JsSection<JsFeature> }) {
  return (
    <SiteSection id="features" divider labelledBy="features-title">
      <SectionHeading id="features-title" eyebrow={features.eyebrow} title={features.title} />
      <div className="cards-features">
        {features.items.map((feature) => (
          <article key={feature.index} className="card">
            <div className="card-top">
              <span className="icon-chip">{isIconName(feature.icon) && <Icon name={feature.icon} />}</span>
              <span className="t-eyebrow secondary">{feature.index}</span>
            </div>
            <h3 className="t-label">{feature.title}</h3>
            <p className="secondary">{feature.body}</p>
          </article>
        ))}
      </div>
    </SiteSection>
  );
}

/** The closing call to action, centred. */
export function Download({ download }: { download: JsDownload }) {
  return (
    <SiteSection id="download" surface labelledBy="download-title">
      <div style={{
        maxWidth: 720, margin: '0 auto', display: 'flex', flexDirection: 'column', alignItems: 'center',
        gap: 'var(--space-5)', textAlign: 'center',
      }}>
        <TallyMark size={72} animate />
        <h2 id="download-title" className="t-display">{download.title}</h2>
        <p className="secondary">{download.body}</p>
        <StoreButtons stores={download.stores} center />
      </div>
    </SiteSection>
  );
}
