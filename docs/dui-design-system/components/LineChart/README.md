A thin trend line with no axes, gridlines or labels, plus optional range chips (1D, 1W, 1M, 1Y).

The line is 1.5px and takes `positive` when the period ended up and `negative` when it ended down. No fill under the line. The selected chip uses `surface-high` with `text-primary`; the others are `text-secondary`. Always give the chart an accessible description ("Value rising over the period") because the line itself carries no numbers.

**Planned Compose API**: `DuiLineChart(points: List<Float>, trend, modifier)` drawn with a Compose `Canvas`, and `DuiRangeChips(options, selected, onSelect)`. The consumer provides the data points.
