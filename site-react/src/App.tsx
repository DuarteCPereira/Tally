import { getSiteContent } from 'tally-site-shared';
import { SiteFooter, SiteHeader } from './components/Dui';
import { Hero } from './sections/Hero';
import { Download, Features, Principle, Steps } from './sections/Content';
import { Stats } from './sections/Stats';

/** The page copy comes from the Kotlin `TallySite` through the `tally-site-shared` npm package. */
const content = getSiteContent();

export function App() {
  return (
    <>
      <SiteHeader brand={content.brand} links={content.nav} action={content.headerAction} />
      <main id="top">
        <Hero hero={content.hero} />
        <Principle principle={content.principle} />
        <Steps steps={content.steps} />
        <Features features={content.features} />
        <Stats teaser={content.stats} />
        <Download download={content.download} />
      </main>
      <SiteFooter note={content.footer.note} links={content.footer.links} />
    </>
  );
}
