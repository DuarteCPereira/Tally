import { useMemo, useState } from 'react';
import { getDefaultStatsRange, getStats, getStatsRanges, type JsStatsTeaser } from 'tally-site-shared';
import { Badge, SectionHeading, SiteSection } from '../components/Dui';

/**
 * The statistics teaser. The numbers are computed by the Kotlin `StatsPreview` from the npm
 * package; React only keeps the selected range in state and renders the result.
 */
export function Stats({ teaser }: { teaser: JsStatsTeaser }) {
  const ranges = useMemo(() => getStatsRanges(), []);
  const [range, setRange] = useState(getDefaultStatsRange);
  const summary = useMemo(() => getStats(range), [range]);
  const trendColor = summary.trend === 'up' ? 'var(--positive)' : 'var(--negative)';

  return (
    <SiteSection id="stats" grid divider labelledBy="stats-title">
      <div className="two-col">
        <SectionHeading
          id="stats-title"
          className="panel"
          eyebrow={teaser.eyebrow}
          title={teaser.title}
          body={teaser.body}
          trailing={<Badge>{teaser.badge}</Badge>}
        />
        <figure style={{
          display: 'flex', flexDirection: 'column', gap: 'var(--space-4)', padding: 'var(--space-5)',
          borderRadius: 'var(--radius-lg)', background: 'var(--bg)', outline: '1px solid var(--divider)',
        }}>
          <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: 'var(--space-2) var(--space-4)' }}>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span className="t-caption secondary">{summary.title}</span>
              <span className="t-display">{summary.volume}</span>
              <span className="t-label" style={{ color: trendColor }}>{summary.change}</span>
            </div>
            <span className="t-eyebrow secondary">{teaser.previewNote}</span>
          </div>
          <LineChart points={summary.points} color={trendColor} label={summary.chartDescription} />
          <div className="chips" role="tablist" aria-label="Range">
            {ranges.map((label) => (
              <button key={label} className="chip" role="tab" aria-selected={label === range} onClick={() => setRange(label)}>
                <span>{label}</span>
              </button>
            ))}
          </div>
          <div className="tiles">
            {summary.tiles.map((tile) => (
              <div key={tile.label} className="tile">
                <span className="t-caption secondary">{tile.label}</span>
                <span className="t-title">{tile.value}</span>
              </div>
            ))}
          </div>
        </figure>
      </div>
    </SiteSection>
  );
}

/** DuiLineChart: a thin line, no axes or fill, stretched to the card width. */
function LineChart({ points, color, label }: { points: number[]; color: string; label: string }) {
  const width = 220;
  const height = 80;
  const min = Math.min(...points);
  const range = Math.max(...points) - min || 1;
  const coords = points
    .map((p, i) => `${(i / (points.length - 1)) * width},${height - ((p - min) / range) * height}`)
    .join(' ');
  return (
    <svg viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none" role="img" aria-label={label}
      style={{ display: 'block', width: '100%', height: 120, overflow: 'visible' }}>
      <polyline points={coords} fill="none" stroke={color} strokeWidth="1.5" strokeLinejoin="round"
        strokeLinecap="round" vectorEffect="non-scaling-stroke" />
    </svg>
  );
}
